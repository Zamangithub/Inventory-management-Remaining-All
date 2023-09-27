package com.example.InventoryManagementSystem.serviceImpl;

import com.example.InventoryManagementSystem.Constants.UserConstant;
import com.example.InventoryManagementSystem.JWT.JwtFilter;
import com.example.InventoryManagementSystem.Models.Category;
import com.example.InventoryManagementSystem.Models.Product;
import com.example.InventoryManagementSystem.Repsitory.ProductRepository;
import com.example.InventoryManagementSystem.services.ProductService;
import com.example.InventoryManagementSystem.utils.UserUtils;
import com.example.InventoryManagementSystem.wrapper.ProductWrapper;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateProductMap(requestMap, false)) {
                    productRepository.save(getProductFromMap(requestMap, false));
                    return UserUtils.getResponseEntity("Product Added Successfully.", HttpStatus.OK);
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


    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if (requestMap.containsKey("name")) {
            if (requestMap.containsKey("id") && validateId) {
                return true;
            } else if (!validateId) {
                return true;
            }
        }
        return false;
    }
    private Product getProductFromMap(Map<String, String> requestMap, boolean isAdd) {
        Category category = new Category();
        category.setId(Integer.parseInt(requestMap.get("categoryId")));

        Product product= new Product();
        if(isAdd){
            product.setId(Integer.parseInt(requestMap.get("id")));
        }else{
            product.setStatus("true");
        }
        product.setCategory(category);
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Integer.parseInt(requestMap.get("price")));
        return product;

    }

    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {

            try {
                if (jwtFilter.isAdmin()) {
                    if (validateProductMap(requestMap, true)) {
                        Optional<Product> optional = productRepository.findById(Integer.parseInt(requestMap.get("id")));
                        if (!optional.isEmpty()) {
                         Product product =   getProductFromMap(requestMap, true);
                         product.setStatus(optional.get().getStatus());
                         productRepository.save(product);
                            return UserUtils.getResponseEntity("Product Updated Successfully", HttpStatus.OK);

                        } else {
                            return UserUtils.getResponseEntity("Product Id does not Exist", HttpStatus.OK);
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

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProduct() {

        try{
            return new ResponseEntity<>(productRepository.getAllProduct(),HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<> (new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {
        try{
            if(jwtFilter.isAdmin()){
                Optional optional = productRepository.findById(id);
                if (!optional.isEmpty()) {
                    productRepository.deleteById(id);
                 return UserUtils.getResponseEntity("Product Deleted Successfully", HttpStatus.OK);
                }else {
                    return UserUtils.getResponseEntity("Product does not Exist", HttpStatus.OK);
                }
            }else{
             return UserUtils.getResponseEntity(UserConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);

            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                Optional optional = productRepository.findById(Integer.parseInt(requestMap.get("id")));
                if (!optional.isEmpty()) {
                    productRepository.updateProductStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    return UserUtils.getResponseEntity("Product Status Updated Successfully", HttpStatus.OK);
                }
                    return UserUtils.getResponseEntity("Product ID does not Exist", HttpStatus.OK);
            }else{
                return UserUtils.getResponseEntity(UserConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);

            }


        }catch (Exception ex){
            ex.printStackTrace();
        }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
        try{

            return new ResponseEntity<>(productRepository.getProductByCategory(id),HttpStatus.OK);

        }catch (Exception ex){
            ex.printStackTrace();
        }
         return new ResponseEntity<> (new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<ProductWrapper> getProductById(Integer id) {
        try {

            return new ResponseEntity<>(productRepository.getProductById(id), HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ProductWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

