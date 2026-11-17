package com.gaming.thezone;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final String UPLOAD_DIR = "uploads/";

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getAllPosts() { return postRepository.findAll(); }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post Id: " + id));
    }

    public void savePost(Post post) { postRepository.save(post); }

    public void deletePost(Long id) { postRepository.deleteById(id); }

    public void updateUsernamesOnPosts(String email, String newUsername) {
        List<Post> allPosts = postRepository.findAll();
        for (Post post : allPosts) {
            if (email.equals(post.getAuthorEmail())) {
                post.setAuthorUsername(newUsername);
                postRepository.save(post);
            }
        }
    }

    public void saveImageFile(Post post, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) { Files.createDirectories(uploadPath); }
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                post.setImageUrl("/uploads/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}