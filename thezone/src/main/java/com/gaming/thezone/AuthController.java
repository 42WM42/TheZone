package com.gaming.thezone;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String registerUser(@RequestParam String email, @RequestParam String password, Model model) {
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already exists!");
            return "register";
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        user.setBanned(false);
        userRepository.save(user);
        return "redirect:/login?registered";
    }
    
    @PostMapping("/admin/ban")
    public String banUser(@RequestParam String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.getRole().equals("ROLE_ADMIN")) {
                user.setBanned(!user.isBanned()); // Toggles the ban
                userRepository.save(user);
            }
        });
        return "redirect:/";
    }
}