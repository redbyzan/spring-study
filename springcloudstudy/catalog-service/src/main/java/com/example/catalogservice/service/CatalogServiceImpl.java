package com.example.catalogservice.service;

import com.example.catalogservice.domain.Catalog;
import com.example.catalogservice.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService{

    private final CatalogRepository repository;

    @Override
    public List<Catalog> getAllCatalogs() {
        return repository.findAll();
    }
}
