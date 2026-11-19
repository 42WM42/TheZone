package com.gaming.thezone;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // This tells Spring to fetch all posts and sort them by the createdAt field, newest first
    List<Post> findAllByOrderByCreatedAtDesc(); 
}