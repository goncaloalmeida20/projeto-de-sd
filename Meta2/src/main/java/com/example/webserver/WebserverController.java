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

}
