package com.example.catalogservice.service;

import com.example.catalogservice.domain.Catalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CatalogService {
    List<Catalog> getAllCatalogs();
}
