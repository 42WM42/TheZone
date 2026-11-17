package com.gaming.thezone;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(Long postId);
    List<Comment> findByAuthorEmail(String email);
}
