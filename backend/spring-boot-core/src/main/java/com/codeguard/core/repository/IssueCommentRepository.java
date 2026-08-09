package com.codeguard.core.repository;

import com.codeguard.core.model.IssueComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {
    List<IssueComment> findByIssueId(Long issueId);
}
