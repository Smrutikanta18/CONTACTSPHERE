package com.example.contactManager.controller;

import com.example.contactManager.entities.User;
import com.example.contactManager.helper.Message;
import com.example.contactManager.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Controller
public class HomeController {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @RequestMapping("/")
    public String home(){
        return "home";
    }
    @RequestMapping("/about")
    public String about(){
        return "about";
    }
    @RequestMapping("/signup")
    public String signup(
            Model model
    ){
        User user=new User();
        model.addAttribute("user",user);
        return "signup";
    }
    @RequestMapping(value = "/do_register",method = RequestMethod.POST)
    public String registerUser(@Valid
            @ModelAttribute("user") User user,BindingResult result, @RequestParam(value = "agreement",defaultValue = "false")
           boolean agreement, Model model,  HttpSession session
            ){
        try{
            if(!agreement){
                throw new Exception("agreement not checked");
            }
            if(result.hasErrors()){
                model.addAttribute("user",user);
                return "signup";
            }
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User result1=this.userRepository.save(user);
            model.addAttribute("user",user);
            session.setAttribute("message",new Message("successful","alert-success"));
            return "signup";
        }catch (Exception e){
            e.printStackTrace();
            model.addAttribute("user",user);
            session.setAttribute("message",new Message("something went wrong"+e.getMessage(),"alert-danger"));
            return "signup";
        }

    }
    @GetMapping("/signin")
    public String login(){
        return "login";
    }
}
