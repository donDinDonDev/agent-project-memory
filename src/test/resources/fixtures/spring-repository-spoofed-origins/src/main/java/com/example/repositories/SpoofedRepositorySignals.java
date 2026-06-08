package com.example.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
class SourceDeclaredFqcnRepository {
}

interface SourceDeclaredImportedSpringDataRepository extends JpaRepository<ProjectOrder, Long> {
}

interface SourceDeclaredFqcnSpringDataRepository extends org.springframework.data.jpa.repository.JpaRepository<ProjectOrder, Long> {
}

class ProjectOrder {
}
