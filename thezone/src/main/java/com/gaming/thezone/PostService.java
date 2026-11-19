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
    private final String UPLOAD_DIR = "uploads/";

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getAllPosts() {
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
    
    public void updatePfpsOnPosts(String email, String newPfpUrl) {
        List<Post> posts = postRepository.findAll();
        for (Post post : posts) {
            if (post.getAuthorEmail().equals(email)) {
                post.setAuthorProfilePictureUrl(newPfpUrl);
                postRepository.save(post);
            }
        }
    }

    public void saveImageFile(Post post, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + filename);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                post.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveUserProfilePicture(User user, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                String filename = UUID.randomUUID().toString() + "_pfp_" + file.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + filename);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                user.setProfilePictureUrl("/uploads/" + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}