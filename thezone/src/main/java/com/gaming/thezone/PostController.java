package com.gaming.thezone;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    // ADD a post
    @PostMapping("/post")
    public String createPost(@ModelAttribute Post post) {
        repo.save(post);
        return "redirect:/"; // Go back to the feed after posting
    }
}