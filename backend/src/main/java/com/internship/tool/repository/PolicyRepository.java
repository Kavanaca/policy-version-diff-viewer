package com.internship.tool.repository;

import com.internship.tool.entity.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Page<Policy> findAll(Pageable pageable);
    

    List<Policy> findByTitleContainingIgnoreCase(String title);

    List<Policy> findByStatus(String status);

    Optional<Policy> findByIdAndDeletedFalse(Long id);
}
