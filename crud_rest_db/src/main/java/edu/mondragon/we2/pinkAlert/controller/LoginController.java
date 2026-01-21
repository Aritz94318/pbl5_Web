package edu.mondragon.we2.pinkalert.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import edu.mondragon.we2.pinkalert.model.Role;
import edu.mondragon.we2.pinkalert.model.User;
import edu.mondragon.we2.pinkalert.service.UserService;

import java.util.Optional;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({ "/", "/login" })
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam("username") String identifier,
            @RequestParam("password") String password,
            Model model,
            HttpServletRequest request) {

        Optional<User> opt = userService.findByIdentifier(identifier);
        System.out.println("LOGIN identifier=" + identifier);
        System.out.println("USER found? " + opt.isPresent());
        opt.ifPresent(u -> System.out.println("ROLE=" + u.getRole() + " hash=" + u.getPasswordHash()));

        if (opt.isEmpty() || !userService.matches(opt.get(), password)) {
            model.addAttribute("error", "Invalid username/email or password.");
            return "login";
        }

        User user = opt.get();
        request.getSession().setAttribute("loggedUser", user);

        if (user.getRole() == Role.DOCTOR)
            return "redirect:/doctor/dashboard";
        if (user.getRole() == Role.PATIENT)
            return "redirect:/patient/dashboard";
        return "redirect:/admin/dashboard";
    }
}
