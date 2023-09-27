package com.example.InventoryManagementSystem.Controller;

import com.example.InventoryManagementSystem.Constants.UserConstant;
import com.example.InventoryManagementSystem.Models.Category;
import com.example.InventoryManagementSystem.controllerImp.CategoryRest;
import com.example.InventoryManagementSystem.services.CategoryService;
import com.example.InventoryManagementSystem.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class CategoryController implements CategoryRest {

    @Autowired
    CategoryService categoryService;

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        try {
            return categoryService.addNewCategory(requestMap);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
       try{
           return categoryService.getAllCategory(filterValue);
       }catch (Exception ex)
       {
           ex.printStackTrace();
       }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<String> updateAllCategory(Map<String, String> requestMap) {
        try{
            return categoryService.updateCategory(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
