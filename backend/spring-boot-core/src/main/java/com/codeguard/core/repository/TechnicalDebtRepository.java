package com.codeguard.core.repository;

import com.codeguard.core.model.TechnicalDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TechnicalDebtRepository extends JpaRepository<TechnicalDebt, Long> {
    Optional<TechnicalDebt> findByRepositoryName(String repositoryName);
}
