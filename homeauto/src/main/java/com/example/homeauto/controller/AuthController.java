package com.example.homeauto.controller;

import com.example.homeauto.model.ApprovalStatus;
import com.example.homeauto.model.AppUser;
import com.example.homeauto.model.Role;
import com.example.homeauto.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {

        model.addAttribute(
                "user",
                new AppUser()
        );

        return "register";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute("user") AppUser user,
            Model model) {

        if (userRepository.existsByEmail(user.getEmail())) {model.addAttribute( "error", "Email is already registered."
            );

            return "register";
        }

        user.setPassword( passwordEncoder.encode( user.getPassword()));
        user.setRole(Role.HOMEOWNER);
        user.setApprovalStatus(ApprovalStatus.PENDING
        );
        user.setEnabled(false);
        userRepository.save(user);
        return "redirect:/login?registered";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/homeowner/dashboard";
    }
}