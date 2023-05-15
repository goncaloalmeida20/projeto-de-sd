package com.example.webserver;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;
import RMISearchModule.SearchModuleC_S_I;
import classes.Page;

import java.util.ArrayList;
import java.util.List;

@Controller
public class WebserverController {
    @GetMapping("/random")
    public String random(){
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
        return "client";
    }

    @GetMapping("/index-url")
    public String indexUrlForm() {
        return "index-url";
    }

    @PostMapping("/index-url")
    public String indexUrl(@RequestParam("urlInput") String url) {
        /*try {
            Registry registry = LocateRegistry.getRegistry("localhost", 6000);
            SearchModuleC searchC = (SearchModuleC) registry.lookup("searchC");

            searchC.indexUrl(url.toLowerCase());

        } catch (Exception e) {
            e.printStackTrace();
        }*/
        System.out.println("INDEXURL");

        //TODO: Display a message or perform any necessary actions after successful indexation


        return "redirect:/index-url";
    }

    @PostMapping("/process-form")
    public String processForm(@RequestParam("numTerms") int numTerms, @RequestParam("term") String[] terms) {
        // Process the terms

        // TESTE
        for (int i = 0; i < numTerms; i++) {
            System.out.println("Term " + (i + 1) + ": " + terms[i]);
        }

        return "redirect:/index";
    }

    @PostMapping("/search")
    public String search(@RequestParam("url") String url, Model model) {
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
