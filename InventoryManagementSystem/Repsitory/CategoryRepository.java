package com.example.InventoryManagementSystem.Repsitory;

import com.example.InventoryManagementSystem.Models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Integer > {

    List<Category> getAllCategory();
}
