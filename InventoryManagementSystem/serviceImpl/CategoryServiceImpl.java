package com.example.InventoryManagementSystem.serviceImpl;

import com.example.InventoryManagementSystem.Constants.UserConstant;
import com.example.InventoryManagementSystem.JWT.JwtFilter;
import com.example.InventoryManagementSystem.Models.Category;
import com.example.InventoryManagementSystem.Repsitory.CategoryRepository;
import com.example.InventoryManagementSystem.services.CategoryService;
import com.example.InventoryManagementSystem.utils.UserUtils;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateCategoryMap(requestMap, false)) {
                    categoryRepository.save(getCategoryFromMap(requestMap, false));
                    return UserUtils.getResponseEntity("Category Added Successfully.", HttpStatus.OK);
                }

            } else {
                return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private boolean validateCategoryMap(Map<String, String> requestMap, boolean validateId) {
        if (requestMap.containsKey("name")) {
            if (requestMap.containsKey("id") && validateId) {
                return true;
            } else if (!validateId) {
                return true;
            }
        }
        return false;
    }

    private Category getCategoryFromMap(Map<String, String> requestMap, Boolean isAdd) {
        Category category = new Category();
        if (isAdd) {
            category.setId(Integer.parseInt(requestMap.get("id")));
        }
        category.setName(requestMap.get("name"));
        return category;
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        try {
            if (!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true")) {
                log.info("Inside if");
                return new ResponseEntity<List<Category>>(categoryRepository.getAllCategory(), HttpStatus.OK);
            }
            return new ResponseEntity<>(categoryRepository.findAll(), HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<List<Category>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateCategoryMap(requestMap, true)) {
                    Optional optional = categoryRepository.findById(Integer.parseInt(requestMap.get("id")));
                    if (!optional.isEmpty()) {

                        categoryRepository.save(getCategoryFromMap(requestMap, true));
                        return UserUtils.getResponseEntity("Category Updated Successfully", HttpStatus.OK);

                    } else {
                        return UserUtils.getResponseEntity("Category Id does not Exist", HttpStatus.OK);
                    }
                }
                return UserUtils.getResponseEntity(UserConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);


            } else {
                return UserUtils.getResponseEntity(UserConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);

            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }
}