package com.example.repositories;

import com.example.domain.ProjectOrder;
import org.springframework.data.jpa.repository.JpaRepository;

interface ProjectOrderRepository extends JpaRepository<ProjectOrder, Long> {
}
