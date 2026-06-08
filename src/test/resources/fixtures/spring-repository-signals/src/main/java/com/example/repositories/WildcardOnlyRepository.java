package com.example.repositories;

import org.springframework.data.repository.*;

interface WildcardOnlyRepository extends CrudRepository<ProjectOrder, Long> {
}
