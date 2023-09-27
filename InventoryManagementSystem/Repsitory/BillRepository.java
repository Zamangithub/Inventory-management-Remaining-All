package com.example.InventoryManagementSystem.Repsitory;

import com.example.InventoryManagementSystem.Models.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill,Integer> {


}
