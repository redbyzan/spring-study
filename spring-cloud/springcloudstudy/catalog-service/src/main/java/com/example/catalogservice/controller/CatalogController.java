package com.example.catalogservice.controller;


import com.example.catalogservice.domain.Catalog;
import com.example.catalogservice.service.CatalogService;
import com.example.catalogservice.vo.ResponseCatalog;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/catalog-service")
@RequiredArgsConstructor
public class CatalogController {

    private final Environment env;
    private final CatalogService service;
    private final ModelMapper modelMapper;

    @GetMapping("/health_check")
    public String status(){
        return "its working in user service port "+ env.getProperty("local.server.port");
    }


    @GetMapping("/catalogs")
    public ResponseEntity getUsers(){
        List<Catalog> allCatalogs = service.getAllCatalogs();

        List<ResponseCatalog> result = new ArrayList<>();

        allCatalogs.forEach(c ->{
            result.add(modelMapper.map(c,ResponseCatalog.class));
        });
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
