package com.example.catalogservice.repository;

import com.example.catalogservice.domain.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogRepository extends JpaRepository<Catalog,Long> {
    Catalog findByProductId(String productId);
}
