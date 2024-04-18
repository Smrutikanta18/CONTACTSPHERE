package com.example.contactManager.config;

import com.example.contactManager.entities.User;
import com.example.contactManager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user=this.userRepository.getUserByUserName(username);
        if(user==null){
            throw new UsernameNotFoundException("could not found user");
        }
        CustomUserDetail customUserDetail=new CustomUserDetail(user);
        return customUserDetail;
    }
}
