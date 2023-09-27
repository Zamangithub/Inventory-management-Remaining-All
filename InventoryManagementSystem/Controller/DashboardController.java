package com.example.InventoryManagementSystem.Controller;


import com.example.InventoryManagementSystem.Repsitory.CategoryRepository;
import com.example.InventoryManagementSystem.Repsitory.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/dashboard")
public class DashboardController {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;

    @GetMapping("/details")
    public ResponseEntity<Map<String, Integer>> getDashboardDetails() {
        Integer categoryCount = Math.toIntExact(categoryRepository.count());
        Integer productCount = Math.toIntExact(productRepository.count());

        Map<String, Integer> details = new HashMap<>();
        details.put("category", categoryCount);
        details.put("product", productCount);

        return ResponseEntity.ok(details);
    }

}
