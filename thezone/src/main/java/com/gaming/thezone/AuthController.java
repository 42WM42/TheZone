package com.gaming.thezone;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.List;

@Controller
public class AuthController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostService postService;
    private final CommentRepository commentRepository;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, PostService postService, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.postService = postService;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @GetMapping("/settings")
    public String settingsPage(Model model, Principal principal) {
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("currentUsername", user.getUsername());
                model.addAttribute("currentPfp", user.getProfilePictureUrl());
            }
        }
        return "settings";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String email, @RequestParam String username, @RequestParam String password, Model model) {
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already exists!");
            return "register";
        }
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username is already taken!");
            return "register";
        }
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        user.setBanned(false);
        user.setProfilePictureUrl("/images/default-pfp.png"); // Default Image
        userRepository.save(user);
        return "redirect:/login?registered";
    }

    @PostMapping("/change-username")
    public String changeUsername(@RequestParam String newUsername, Principal principal) {
        if (userRepository.findByUsername(newUsername).isPresent()) {
            return "redirect:/settings?error=UsernameTaken";
        }
        User user = userRepository.findByEmail(principal.getName()).get();
        user.setUsername(newUsername);
        userRepository.save(user);
        
        postService.updateUsernamesOnPosts(principal.getName(), newUsername);
        
        List<Comment> userComments = commentRepository.findByAuthorEmail(principal.getName());
        for(Comment c : userComments) {
            c.setAuthorUsername(newUsername);
            commentRepository.save(c);
        }
        return "redirect:/settings?success=UsernameChanged";
    }

    @PostMapping("/change-pfp")
    public String changePfp(@RequestParam("pfpFile") MultipartFile file, Principal principal) {
        if (file.isEmpty()) return "redirect:/settings";
        
        User user = userRepository.findByEmail(principal.getName()).get();
        postService.saveUserProfilePicture(user, file);
        userRepository.save(user);
        
        postService.updatePfpsOnPosts(principal.getName(), user.getProfilePictureUrl());
        
        List<Comment> userComments = commentRepository.findByAuthorEmail(principal.getName());
        for(Comment c : userComments) {
            c.setAuthorProfilePictureUrl(user.getProfilePictureUrl());
            commentRepository.save(c);
        }
        return "redirect:/settings?success=PfpChanged";
    }
    
    @PostMapping("/admin/ban")
    public String banUser(@RequestParam String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.getRole().equals("ROLE_ADMIN")) {
                user.setBanned(!user.isBanned());
                userRepository.save(user);
            }
        });
        return "redirect:/";
    }
}