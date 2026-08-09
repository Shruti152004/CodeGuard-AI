package com.codeguard.core.repository;

import com.codeguard.core.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findBySlug(String slug);
}
