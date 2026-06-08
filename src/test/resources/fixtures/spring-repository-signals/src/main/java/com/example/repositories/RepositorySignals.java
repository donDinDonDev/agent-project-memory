package com.example.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;

@org.springframework.stereotype.Repository
class DirectOrderRepository {
}

interface OrderRepository extends JpaRepository<ProjectOrder, Long> {
}

@org.springframework.stereotype.Repository
interface AnnotatedSpringDataRepository extends org.springframework.data.repository.CrudRepository<ProjectOrder, Long> {
}

interface CoreRepositorySignal extends Repository<ProjectOrder, Long> {
}

interface PagedOrderRepository extends PagingAndSortingRepository<ProjectOrder, Long> {
}

interface MongoOrderRepository extends MongoRepository<ProjectOrder, String> {
}

interface FullyQualifiedCrudRepository extends org.springframework.data.repository.CrudRepository<ProjectOrder, Long> {
}

class NotARepository extends org.springframework.data.repository.CrudRepositorySupport {
}

interface LocalBaseRepository extends LocalRepositoryBase<ProjectOrder> {
}

interface LocalRepositoryBase<T> {
}

class ProjectOrder {
}
