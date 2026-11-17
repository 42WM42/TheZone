package com.gaming.thezone;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.security.Principal;

@Controller
public class CommentController {
    
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentController(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/post/{id}/comment")
    public String addComment(@PathVariable Long id, @RequestParam String content, @RequestParam(required = false) Long parentId, Principal principal) {
        Post post = postRepository.findById(id).orElseThrow();
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthorEmail(user.getEmail());
        comment.setAuthorUsername(user.getUsername());
        comment.setPost(post);
        
        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId).orElseThrow();
            comment.setParentComment(parent);
        }
        
        commentRepository.save(comment);
        return "redirect:/post/" + id;
    }

    @PostMapping("/comment/{id}/edit")
    public String editComment(@PathVariable Long id, @RequestParam String content, Principal principal) {
        Comment comment = commentRepository.findById(id).orElseThrow();
        if (comment.getAuthorEmail().equals(principal.getName())) {
            comment.setContent(content);
            comment.setEdited(true);
            commentRepository.save(comment);
        }
        return "redirect:/post/" + comment.getPost().getId();
    }

    @PostMapping("/comment/{id}/delete")
    public String deleteComment(@PathVariable Long id, Principal principal) {
        Comment comment = commentRepository.findById(id).orElseThrow();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (comment.getAuthorEmail().equals(principal.getName()) || isAdmin) {
            Long postId = comment.getPost().getId();
            commentRepository.delete(comment);
            return "redirect:/post/" + postId;
        }
        return "redirect:/";
    }
}