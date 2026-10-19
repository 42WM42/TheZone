package com.gaming.thezone;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    // Automatically gives us save(), findAll(), etc.
}