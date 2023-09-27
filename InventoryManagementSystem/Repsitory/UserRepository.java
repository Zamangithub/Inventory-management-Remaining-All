package com.example.InventoryManagementSystem.Repsitory;

import com.example.InventoryManagementSystem.Models.User;
import com.example.InventoryManagementSystem.wrapper.UserWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.NamedQuery;
import javax.transaction.Transactional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {



    @Query("select u from User u where u.email =:email")
    User findByEmailId (@Param("email")String email);

    @Query("select u.email from User u where u.role = 'admin'")
    List<String> getAllAdmin();

    @Query("select new com.example.InventoryManagementSystem.wrapper.UserWrapper(u.id,u.name,u.email,u.contactNumber,u.status) from User u where u.role = 'user'")
    List<UserWrapper> getAllUser();

    @Transactional
    @Modifying
    @Query("Update User u set u.status=:status where u.id=:id")
    Integer updateStatus(@Param("status") String status,@Param("id") Integer id);

    User findByEmail(String email);

    User findByOtp(String otp); // Add a method to find a user by OTP



}
