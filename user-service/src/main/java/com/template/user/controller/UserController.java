package com.template.user.controller;

import com.template.user.entity.UserProfile;
import com.template.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping
    public ResponseEntity<UserProfile> createUser(@RequestBody UserProfile userProfile) {
        return ResponseEntity.ok(service.saveUser(userProfile));
    }

    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfile> getUserByUsername(@PathVariable String username) {
        return service.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{username}")
    public ResponseEntity<UserProfile> updateUser(@PathVariable String username, @RequestBody UserProfile userProfile) {
        try {
            return ResponseEntity.ok(service.updateUser(username, userProfile));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
