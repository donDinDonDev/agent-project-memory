package com.example.repositories;

import com.example.domain.SharedOrder;
import com.example.missing.MissingOrder;
import com.example.unique.UniqueOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface UniqueOrderRepository extends JpaRepository<UniqueOrder, Long> {
}

interface FqcnUniqueOrderRepository extends JpaRepository<com.example.unique.UniqueOrder, Long> {
}

interface MissingOrderRepository extends JpaRepository<MissingOrder, Long> {
}

interface AmbiguousSharedOrderRepository extends JpaRepository<SharedOrder, Long> {
}

interface NestedGenericOrderRepository extends JpaRepository<List<UniqueOrder>, Long> {
}

interface WildcardGenericOrderRepository extends JpaRepository<? extends UniqueOrder, Long> {
}

interface RawOrderRepository extends JpaRepository {
}
