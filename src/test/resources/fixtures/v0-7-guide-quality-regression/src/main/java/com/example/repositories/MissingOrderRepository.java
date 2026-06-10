package com.example.repositories;

import com.example.domain.MissingOrder;
import org.springframework.data.jpa.repository.JpaRepository;

interface MissingOrderRepository extends JpaRepository<MissingOrder, Long> {
}
