package com.template.user.service;

import com.template.user.entity.UserProfile;
import com.template.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public UserProfile saveUser(UserProfile userProfile) {
        return repository.save(userProfile);
    }

    public List<UserProfile> getAllUsers() {
        return repository.findAll();
    }

    public Optional<UserProfile> getUserByUsername(String username) {
        return repository.findByUsername(username);
    }

    public UserProfile updateUser(String username, UserProfile newProfile) {
        return repository.findByUsername(username).map(profile -> {
            profile.setFirstName(newProfile.getFirstName());
            profile.setLastName(newProfile.getLastName());
            profile.setEmail(newProfile.getEmail());
            profile.setPhoneNumber(newProfile.getPhoneNumber());
            return repository.save(profile);
        }).orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
