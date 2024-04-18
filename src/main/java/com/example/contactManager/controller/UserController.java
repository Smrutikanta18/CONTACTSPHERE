package com.example.contactManager.controller;

import com.example.contactManager.entities.Contact;
import com.example.contactManager.entities.User;
import com.example.contactManager.helper.Message;
import com.example.contactManager.repository.ContactRepository;
import com.example.contactManager.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    @ModelAttribute
    public void addCommonData(Model model,Principal principal){
        String username= principal.getName();
        User user=this.userRepository.getUserByUserName(username);
        model.addAttribute("user",user);
    }
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal){

        return "normal/user_dashboard";
    }
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model){
        model.addAttribute("title","Add contact");
        model.addAttribute("contact",new Contact());
        return "normal/add_contact_form";
    }
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("image") MultipartFile img,
                                 Principal principal,
                                 HttpSession session) {
        try {
            String name = principal.getName();
            User user = this.userRepository.getUserByUserName(name);
            if (img.isEmpty()) {
                String img1="img_1.png";
                contact.setImageFileName(img1);
            } else {

                String originalFileName= img.getOriginalFilename();
                contact.setImage(img);
                contact.setImageFileName(originalFileName);
                File file=new ClassPathResource("static/img1").getFile();
                 Path path =Paths.get(file.getAbsolutePath()+File.separator+img.getOriginalFilename());

                Files.copy(img.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("image is uploaded");
            }
            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);
            System.out.println("Added to database");
            session.setAttribute("message",new Message("successful","alert-success"));

        }catch (Exception e){
            e.printStackTrace();
            session.setAttribute("message",new Message("something went wrong"+e.getMessage(),"alert-danger"));
        }
        return "normal/add_contact_form";
    }
    @GetMapping("/show-contact/{page}")
    public String showContacts(@PathVariable("page") int page,Model m,Principal principal){
        String userName=principal.getName();
        User user=this.userRepository.getUserByUserName(userName);
        Pageable pageable =PageRequest.of(page,5);
        Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
        m.addAttribute("contacts",contacts);
        m.addAttribute("currentPage",page);
        m.addAttribute("totalPages",contacts.getTotalPages());
        return "normal/show_contacts";
    }
    @GetMapping("/{cid}/contact")
    public String showContactDetail(
            @PathVariable("cid") Integer cid,
            Model model,Principal principal
    ){
         Optional<Contact> contactOptional =this.contactRepository.findById(cid);
         Contact contact=contactOptional.get();
         String userName=principal.getName();
         User user=this.userRepository.getUserByUserName(userName);
         if(user.getId()==contact.getUser().getId()) {
             model.addAttribute("contact", contact);
         }
        return "normal/contact_detail";
    }

    @GetMapping("/delete/{cid}")
    public String deleteContact(
            @PathVariable("cid") Integer cid,
            Model model, Principal principal
    ) throws IOException {
        Contact contact= this.contactRepository.findById(cid).get();

        User user=this.userRepository.getUserByUserName(principal.getName());
        user.getContacts().remove(contact);
        if(contact.getImageFileName() != null && !contact.getImageFileName().isEmpty()) {
            File deleteFile = new ClassPathResource("static/img1").getFile();
            File file1 = new File(deleteFile, contact.getImageFileName());
            file1.delete();
        }
        this.userRepository.save(user);
        return "redirect:/user/show-contact/0";
    }

    @PostMapping("/update-contact/{cid}")
    public String updateForm(
            @PathVariable("cid") Integer cid,
            Model model
    ){
        Contact contact=this.contactRepository.findById(cid).get();
       model.addAttribute("contact",contact);
        return "normal/update_form";
    }
    @GetMapping("/profile")
    public String profilePage( Model m,Principal principal){
        String image=principal.getName();
        User user=this.userRepository.getUserByUserName(image);
        user.setImageUrl("default.png");
        return "normal/profile";
    }
    @PostMapping("/process-update")
    public String processUpdate(@ModelAttribute Contact contact,
                                @RequestParam("image") MultipartFile img,HttpSession session,
                                Principal principal,Model model){
        try{
            Contact oldContact=this.contactRepository.findById(contact.getCid()).get();
            if(!img.isEmpty()){
                File deleteFile=new ClassPathResource("static/img1").getFile();
                File file1=new File(deleteFile,oldContact.getImageFileName());
                file1.delete();

                File file=new ClassPathResource("static/img1").getFile();
                Path path =Paths.get(file.getAbsolutePath()+File.separator+img.getOriginalFilename());
                Files.copy(img.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImageFileName(img.getOriginalFilename());

            }else {
                contact.setImageFileName(oldContact.getImageFileName());
            }
            User user=this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);
            session.setAttribute("message",new Message("your contact is updated ","success"));

        }catch (Exception e){
            e.printStackTrace();
        }
       System.out.println(contact.getName());
        System.out.println(contact.getCid());
        return "redirect:/user/"+contact.getCid()+"/contact";
    }
//    @PostMapping("/update-user")
//    public String updateUser(@ModelAttribute User updatedUser, HttpSession session) {
//        try {
//            User existingUser = userRepository.findById(updatedUser.getId()).orElse(null); // Fetch the existing user from the database
//            if (existingUser != null) {
//                existingUser.setName(updatedUser.getName());
//                existingUser.setEmail(updatedUser.getEmail());
//                existingUser.setRole(updatedUser.getRole());
//                existingUser.setAbout(updatedUser.getAbout());
//                userRepository.save(existingUser);
//                session.setAttribute("message", new Message("User details updated successfully", "alert-success"));
//            } else {
//                session.setAttribute("message", new Message("User not found", "alert-danger"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            session.setAttribute("message", new Message("Failed to update user details: " + e.getMessage(), "alert-danger"));
//        }
//        return "redirect:/user/profile";
//    }
@GetMapping("/delete-user/{id}")
public String deleteUser(@PathVariable("id") Integer id,HttpSession session) {
    try {

        Optional<User> userOptional = this.userRepository.findById(id);
        User user = userOptional.get();
        if (user != null) {
            user.setContacts(null);
            this.userRepository.delete(user);
            this.userRepository.delete(user);
            session.setAttribute("message", new Message("User account deleted successfully", "alert-success"));
        } else {
            session.setAttribute("message", new Message("User account not found", "alert-danger"));
        }
    } catch (Exception e) {
        e.printStackTrace();
        session.setAttribute("message", new Message("Failed to delete user account: " + e.getMessage(), "alert-danger"));
    }
    return "redirect:/logout";
}
@GetMapping("/settings")
public String settings(){
        return "normal/settings";
}
@PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Principal principal,
                                 HttpSession session){
    String userName=principal.getName();
    User currentUser=this.userRepository.getUserByUserName(userName);

    if(this.bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword())){
        currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
        this.userRepository.save(currentUser);
        session.setAttribute("message", new Message("password changed successfully", "success"));

    }
    else {
        session.setAttribute("message", new Message("wrong password", "danger"));
        return "redirect:/user/settings";
    }

        return "redirect:/user/index";
}


}
