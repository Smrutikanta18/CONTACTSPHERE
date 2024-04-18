package com.example.contactManager.controller;

import com.example.contactManager.entities.Contact;
import com.example.contactManager.entities.User;
import com.example.contactManager.repository.ContactRepository;
import com.example.contactManager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class SearchController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;

    @GetMapping("/search/{query}")
    public ResponseEntity<?> search(
            @PathVariable("query") String query, Principal principal
            ){
        User user= this.userRepository.getUserByUserName(principal.getName());
        List<Contact> contacts=this.contactRepository.findByNameContainingAndUser(query,user);
        return  ResponseEntity.ok(contacts);
    }

}
