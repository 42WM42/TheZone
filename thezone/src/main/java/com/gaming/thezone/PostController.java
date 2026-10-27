package com.gaming.thezone;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class PostController {

    private final PostRepository repo;

    public PostController(PostRepository repo) {
        this.repo = repo;
    }

    // VIEW all posts (The Zone Feed)
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("posts", repo.findAll());
        return "index";
    }

    // SHOW the make post page
    @GetMapping("/make-post")
    public String showMakePostPage(Model model) {
        model.addAttribute("newPost", new Post());
        return "make-post";
    }

    // ADD a new post
    @PostMapping("/post")
    public String createPost(@ModelAttribute Post post, @RequestParam("imageFile") MultipartFile file) {
        saveImageFile(post, file);
        repo.save(post);
        return "redirect:/"; 
    }

    // VIEW a single post fully
    @GetMapping("/post/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        Post post = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post Id: " + id));
        model.addAttribute("post", post);
        return "post-view";
    }

    // SHOW the edit post page
    @GetMapping("/post/{id}/edit")
    public String showEditPostPage(@PathVariable Long id, Model model) {
        Post post = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post Id: " + id));
        model.addAttribute("post", post);
        return "edit-post";
    }

    // UPDATE an existing post
    @PostMapping("/post/{id}/edit")
    public String editPost(@PathVariable Long id, @ModelAttribute Post updatedPost, @RequestParam("imageFile") MultipartFile file) {
        Post existingPost = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post Id: " + id));
        
        // Update content
        existingPost.setContent(updatedPost.getContent());
        
        // If a new image was uploaded, overwrite the old image URL
        if (!file.isEmpty()) {
            saveImageFile(existingPost, file);
        }
        
        repo.save(existingPost);
        return "redirect:/post/" + id; // Redirect back to the full post view
    }

    // DELETE a post
    @PostMapping("/post/{id}/delete")
    public String deletePost(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/";
    }

    // Helper method to handle file saving logic
    private void saveImageFile(Post post, MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                post.setImageUrl("/uploads/" + fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}