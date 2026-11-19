package com.gaming.thezone;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    
    // Defines where the images are saved. Make sure this matches your setup.
    private final String UPLOAD_DIR = "uploads/";

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getAllPosts() {
        // Changed: Now fetches posts ordered by newest first
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    public void savePost(Post post) {
        if (post.getCreatedAt() == null) {
            post.setCreatedAt(LocalDateTime.now());
        }
        postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public void updateUsernamesOnPosts(String email, String newUsername) {
        List<Post> posts = postRepository.findAll();
        for (Post post : posts) {
            if (post.getAuthorEmail().equals(email)) {
                post.setAuthorUsername(newUsername);
                postRepository.save(post);
            }
        }
    }

    public void saveImageFile(Post post, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                // Generate a unique file name to prevent overwriting
                String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + filename);
                
                // Create the uploads directory if it doesn't exist
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                
                // Save the path to the post object
                post.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}