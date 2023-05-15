package com.example.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;
import RMISearchModule.SearchModuleC_S_I;
import classes.Page;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WebserverController {
    private static final Logger logger = LoggerFactory.getLogger(WebserverController.class);
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

    @GetMapping("/index-url")
    public String indexUrlForm() {
        return "index-url";
    }

    @PostMapping("/submit-index-url")
    public String indexUrl(@RequestParam("url") String url) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 7004);
            SearchModuleC_S_I searchC = (SearchModuleC_S_I) registry.lookup("127.0.0.1");
            logger.info("Indexing URL: " + url);
            searchC.indexUrl(url.toLowerCase());

        } catch (Exception e) {
            e.printStackTrace();
        }


        return "redirect:/index-url";
    }

    @PostMapping("/search")
    public String search(@RequestParam("numTerms") int numTerms, @RequestParam("term") String[] terms) {
        // Process the terms

        // TESTE
        for (int i = 0; i < numTerms; i++) {
            System.out.println("Term " + (i + 1) + ": " + terms[i]);
        }

        return "redirect:/index";
    }

    @PostMapping("/search-pages")
    public String searchPages(@RequestParam("url") String url, Model model) {
        List<Page> pages = searchResults(url);
        model.addAttribute("pages", pages);

        return "search-results";
    }

    private List<Page> searchResults(String url) {

        //TESTE

        List<Page> pages = new ArrayList<>();

        Page page1 = new Page();
        page1.url = "https://example.com/page1";
        page1.title = "Page 1";
        page1.citation = "Citation 1";
        pages.add(page1);

        Page page2 = new Page();
        page2.url = "https://example.com/page2";
        page2.title = "Page 2";
        page2.citation = "Citation 2";
        pages.add(page2);

        return pages;
    }
}
