package com.example.webserver;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebserverController {
    @GetMapping("/random")
    public String hello(){
        return "clientPage";
    }

    @GetMapping("/register")
    public String register(){
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@RequestParam("username") String username,
                                      @RequestParam("password") String password) {
        //TODO: Verify the registration

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password) {
        //TODO: Verify the login process

        return "redirect:/client";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }

    @GetMapping("/guest")
    public String guestPage() {
        return "guest";
    }

    @GetMapping("/client")
    public String clientPage() {
        return "guest";
    }
}
