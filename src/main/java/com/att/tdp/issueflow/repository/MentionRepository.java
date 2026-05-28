package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.Mention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MentionRepository extends JpaRepository<Mention, Long> {
    List<Mention> findAllByCommentId(Long commentId);

    @Modifying
    @Query("DELETE FROM Mention m WHERE m.commentId = :commentId")
    void deleteAllByCommentId(@Param("commentId") Long commentId);

    @Query("SELECT m.commentId FROM Mention m WHERE m.userId = :userId ORDER BY m.commentId DESC")
    List<Long> findCommentIdsByUserId(@Param("userId") Long userId);
}
