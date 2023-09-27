package com.example.InventoryManagementSystem.Controller;


import com.example.InventoryManagementSystem.Constants.UserConstant;
import com.example.InventoryManagementSystem.controllerImp.BillRest;
import com.example.InventoryManagementSystem.serviceImpl.BillServiceImp;
import com.example.InventoryManagementSystem.services.BillService;
import com.example.InventoryManagementSystem.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class BillController implements BillRest {

    @Autowired
    BillService billService;

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        try {
            return billService.generateReport(requestMap);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
