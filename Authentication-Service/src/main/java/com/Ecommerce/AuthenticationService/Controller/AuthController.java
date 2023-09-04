package com.Ecommerce.AuthenticationService.Controller;

import com.Ecommerce.AuthenticationService.Exception.LoginException;
import com.Ecommerce.AuthenticationService.Exception.LogoutException;
import com.Ecommerce.AuthenticationService.Request.Login;
import com.Ecommerce.AuthenticationService.Service.Auth_Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final Auth_Service authService;

    public AuthController(Auth_Service authService) {
        this.authService = authService;
    }

    //Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login login) {
        try {
            return ResponseEntity.ok( authService.CustomerLogin(login));
        }catch (LoginException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Logout
    @PostMapping("/logout")
    public ResponseEntity<?> CustomerLogout(@RequestParam String refresh_token) {
        try {
            return ResponseEntity.ok(authService.CustomerLogout(refresh_token));
        } catch (LogoutException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
