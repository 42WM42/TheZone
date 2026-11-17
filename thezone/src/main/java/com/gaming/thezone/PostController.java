package com.gaming.thezone;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class PostController {

    private final PostService postService; 
    private final UserRepository userRepository; 

    public PostController(PostService postService, UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        model.addAttribute("posts", postService.getAllPosts());
        model.addAttribute("currentUserEmail", principal.getName());
        
        User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
        if (currentUser != null) {
            model.addAttribute("currentUsername", currentUser.getUsername());
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("isAdmin", true);
            model.addAttribute("users", userRepository.findAll());
        } else {
            model.addAttribute("isAdmin", false);
        }
        return "index";
    }

    @GetMapping("/make-post")
    public String showMakePostPage(Model model) {
        model.addAttribute("newPost", new Post());
        return "make-post";
    }

    @PostMapping("/post")
    public String createPost(@ModelAttribute Post post, @RequestParam("imageFile") MultipartFile file, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).get();
        post.setAuthorEmail(principal.getName());
        post.setAuthorUsername(currentUser.getUsername());
        postService.saveImageFile(post, file);
        postService.savePost(post);
        return "redirect:/"; 
    }

    @GetMapping("/post/{id}")
    public String viewPost(@PathVariable Long id, Model model, Principal principal) {
        model.addAttribute("post", postService.getPostById(id));
        model.addAttribute("currentUserEmail", principal.getName());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        
        return "post-view";
    }

    @GetMapping("/post/{id}/edit")
    public String showEditPostPage(@PathVariable Long id, Model model, Principal principal) {
        Post post = postService.getPostById(id);
        if (!post.getAuthorEmail().equals(principal.getName())) {
            return "redirect:/post/" + id; 
        }
        model.addAttribute("post", post);
        return "edit-post";
    }

    @PostMapping("/post/{id}/edit")
    public String editPost(@PathVariable Long id, @ModelAttribute Post updatedPost, @RequestParam("imageFile") MultipartFile file, Principal principal) {
        Post existingPost = postService.getPostById(id);
        if (!existingPost.getAuthorEmail().equals(principal.getName())) {
            return "redirect:/post/" + id;
        }
        existingPost.setContent(updatedPost.getContent());
        if (!file.isEmpty()) {
            postService.saveImageFile(existingPost, file);
        }
        postService.savePost(existingPost);
        return "redirect:/post/" + id; 
    }

    @PostMapping("/post/{id}/delete")
    public String deletePost(@PathVariable Long id, Principal principal) {
        Post post = postService.getPostById(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (post.getAuthorEmail().equals(principal.getName()) || isAdmin) {
            postService.deletePost(id);
        }
        return "redirect:/";
    }
}