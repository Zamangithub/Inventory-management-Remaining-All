package com.example.InventoryManagementSystem.serviceImpl;

import com.example.InventoryManagementSystem.Constants.UserConstant;
import com.example.InventoryManagementSystem.JWT.CustomerUsersDetailService;
import com.example.InventoryManagementSystem.JWT.JwtFilter;
import com.example.InventoryManagementSystem.JWT.JwtUtil;
import com.example.InventoryManagementSystem.Models.User;
import com.example.InventoryManagementSystem.Repsitory.UserRepository;
import com.example.InventoryManagementSystem.services.UserService;
import com.example.InventoryManagementSystem.utils.EmailUtil;
import com.example.InventoryManagementSystem.utils.UserUtils;
import com.example.InventoryManagementSystem.wrapper.UserWrapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class UserServiceImpl  implements UserService {

    @Autowired
    UserRepository userRepository ;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    CustomerUsersDetailService customerUsersDetailService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtil emailUtil;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public ResponseEntity<String> signup(Map<String, String> requestMap) {

        log.info("Inside signUp {}", requestMap);
        try {
            if (validateSignUpMap(requestMap)) {
                User user = userRepository.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {

                    // Generate OTP
                    String otp = generateRandomOTP();
                    user = getUserFromMap(requestMap);
                    user.setOtp(otp);
                    user.setOtpUsed(false);
                    user.setOtpExpirationTime(LocalDateTime.now().plusMinutes(10)); // OTP expires in 10 minutes
                    userRepository.save(user);

                    // Send the OTP to the user via email (you need to implement this)
                    sendEmailOTP(user.getEmail(), otp); // Implement your own method

                    return UserUtils.getResponseEntity("Successfully Registered", HttpStatus.OK);

                } else {
                    return UserUtils.getResponseEntity("Email is Already Exist.", HttpStatus.BAD_REQUEST);
                }

            } else {
                return UserUtils.getResponseEntity(UserConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendEmailOTP(String userEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(userEmail);
            message.setSubject("OTP Verification");
            message.setText("Your OTP for verification: " + otp);
            javaMailSender.send(message);

            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Email sending failed: " + e.getMessage());
        }
    }


    private boolean validateSignUpMap(Map<String,String> requestMap) {

      if(requestMap.containsKey("name")&& requestMap.containsKey("contactNumber")
                && requestMap.containsKey("email") && requestMap.containsKey("password")){
          return true;
      }
        return false;
    }
    private User getUserFromMap(Map<String,String> requestMap){
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber (requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");
        return user ;
    }
    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
       log.info("Inside Login");
       try{
           User user = userRepository.findByEmailId(requestMap.get("email"));
           if (Objects.isNull(user)) {
               return new ResponseEntity<>("{\"message\":\"User not found.\"}", HttpStatus.NOT_FOUND);
           }
           if (user.isOtpUsed() || LocalDateTime.now().isAfter(user.getOtpExpirationTime())) {
               return new ResponseEntity<>("{\"message\":\"OTP has expired or already used.\"}", HttpStatus.BAD_REQUEST);
           }
           if (!requestMap.get("otp").equals(user.getOtp())) {
               return new ResponseEntity<>("{\"message\":\"Invalid OTP.\"}", HttpStatus.BAD_REQUEST);
           }
           // Mark OTP as used
           user.setOtpUsed(true);
           userRepository.save(user);
           Authentication auth = authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(requestMap.get("email"),requestMap.get("password")));
           if(auth.isAuthenticated()){
               if(customerUsersDetailService.getUserDetail()
                       .getStatus().equalsIgnoreCase("true")){
                   return new ResponseEntity<String>("{\"token\":\""+
                  jwtUtil.generateToken(customerUsersDetailService.getUserDetail().getEmail(),
                          customerUsersDetailService.getUserDetail().getRole()) + "\"}",
                           HttpStatus.OK);
               }
               else{
                   return new ResponseEntity<String>("{\"message\":\""+"Wait for Admin approval."+"\"}",
                           HttpStatus.BAD_REQUEST);
               }
           }
       }catch (Exception ex){
            log.error("{}",ex);
       }
        return new ResponseEntity<String>("{\"message\":\""+"Bad Credentials."+"\"}",
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try{
            if(jwtFilter.isAdmin()){
                return new ResponseEntity<>(userRepository.getAllUser(), HttpStatus.OK);

            }
            else {
                return new ResponseEntity<>(new ArrayList(),HttpStatus.UNAUTHORIZED);
            }

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                Optional<User> optional = userRepository.findById(Integer.parseInt(requestMap.get("id")));
                if(!optional.isEmpty()){
                    userRepository.updateStatus(requestMap.get("status"),
                            Integer.parseInt(requestMap.get("id")));
                    sendMaiToAllAdmin(requestMap.get("status"),
                            optional.get().getEmail(), userRepository.getAllAdmin());

                    return UserUtils.getResponseEntity("User status updates successfully",HttpStatus.OK);

                }
                else{
                   return UserUtils.getResponseEntity("User ID doesn't  exist",HttpStatus.NOT_FOUND);
                }

            }
            else {
                return UserUtils.getResponseEntity(UserConstant.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);

            }

        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }



    private void sendMaiToAllAdmin(String status, String user, List<String> allAdmin){
        allAdmin.remove(jwtFilter.getCurrentUser());

        if(status!=null && status.equalsIgnoreCase("true")){
            emailUtil.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account approved", "USER:- "+user+" \n is approved by \nADMIN:- "+ jwtFilter.getCurrentUser(),allAdmin);
        }
        else
        {
            emailUtil.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Disabled", "USER:- " +user+" \n  is disabled by \nADMIN:- "+ jwtFilter.getCurrentUser(),allAdmin);
        }
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return UserUtils.getResponseEntity("true",HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try{
            User userObj = userRepository.findByEmail(jwtFilter.getCurrentUser());
            if(!userObj.equals(null)){
                if(userObj.getPassword().equals(requestMap.get("oldPassword"))) {

                    userObj.setPassword(requestMap.get("newPassword"));
                    userRepository.save(userObj);
                    return UserUtils.getResponseEntity("Password Updated Successfully",HttpStatus.OK);

                }
                    return UserUtils.getResponseEntity("Incorrect Old Password",HttpStatus.BAD_REQUEST);
                }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
            User user = userRepository.findByEmail(requestMap.get("email"));
            if (!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())) {
                emailUtil.forgotMail(user.getEmail(), "Credentials through Cafe Management System", user.getPassword());
                return UserUtils.getResponseEntity("Check Your mail for Credentials.", HttpStatus.OK);
            }
        }catch (Exception ex){
            ex.printStackTrace();

        }
        return UserUtils.getResponseEntity(UserConstant.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);

    }
    public String generateRandomOTP() {
        // Generate a 6-digit random OTP
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

}
