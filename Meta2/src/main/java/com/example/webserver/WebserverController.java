package com.example.webserver;

import RMISearchModule.SearchModuleC_S_I;
import classes.Page;
import com.example.webserver.forms.Login;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;
import RMISearchModule.SearchModuleC_S_I;
import classes.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WebserverController {

    //===========================================================================
    // Constants
    //===========================================================================

    private static final Logger logger = LoggerFactory.getLogger(WebserverController.class);
    private static final int RESULTS_PER_PAGE = 10;

    //===========================================================================
    // Pages
    //===========================================================================

    @GetMapping("/")
    public String redirect(){return "redirect:/guest";}

    @GetMapping("/guest")
    public String guestPage() {
        return "guest";
    }

    @GetMapping("/client")
    public String clientPage() {
        return "client";
    }

    //===========================================================================
    // Authentication
    //===========================================================================

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/submit-register")
    public String processRegistration(@RequestParam("username") String username,
                                      @RequestParam("password") String password) {
        //TODO: Verify the registration

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String createLoginForm(Model model) {

        model.addAttribute("login", new Login());
        return "login";
    }

    @PostMapping("/save-login")
    public String saveLoginSubmission(@ModelAttribute Login login) {
        if(validateLogin(login)) {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("login", true);
            return "redirect:/client";
        }
        else {
            // TODO: Show error message
            return "redirect:/login";
        }
    }

    public boolean validateLogin(Login login){
        //TODO: VALIDATE LOGIN

        return !login.getUsername().isEmpty() && !login.getPassword().isEmpty();
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }


    //===========================================================================
    // Index URL
    //===========================================================================


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

    @GetMapping("/top-stories")
    public String topStories(Model model){
        String topStoriesURL = "https://hacker-news.firebaseio.com/v0/topstories.json";
        RestTemplate restTemplate = new RestTemplate();
        List hnTopStories = restTemplate.getForObject(topStoriesURL, List.class);
        List<String> topStoryURLs = new ArrayList<>();
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 7004);
            SearchModuleC_S_I searchC = (SearchModuleC_S_I) registry.lookup("127.0.0.1");
            for (var topStory : hnTopStories) {
                String formattedTopStoryURL = "https://hacker-news.firebaseio.com/v0/item/" +
                        topStory.toString() +
                        ".json";
                logger.info("Analyzing top story with URL: " + formattedTopStoryURL);
                HackerNewsItemRecord hnir = restTemplate.getForObject(formattedTopStoryURL
                        , HackerNewsItemRecord.class);
                if(hnir == null || hnir.url() == null || hnir.text() == null) continue;
                String storyURL = hnir.url().toLowerCase();
                searchC.indexUrl(storyURL);
                topStoryURLs.add(storyURL);
            }
            model.addAttribute("topStoriesList", topStoryURLs);
        } catch(Exception e){
            e.printStackTrace();
        }
        return "top-stories";
    }

    @PostMapping("/search")
    public String search(@RequestParam("numTerms") int numTerms, @RequestParam("term") String[] terms) {
        // Process the terms

    //===========================================================================
    // Search for pages with certain terms
    //===========================================================================

    @GetMapping("/search-terms")
    public String showSearchPage() {
        return "search-terms";
    }

    @PostMapping("/search-terms-results")
    @ResponseBody
    public List<Page> searchTermsResults(@RequestBody List<String> terms) {
        // TODO: With terms, get pages from the server that contain the terms and save them in the "pages" variable
        logger.info(terms.toString());

        List<Page> pages = new ArrayList<>();
        Page p = new Page();
        p.title = "OLA";
        p.url = "hello";
        p.citation = "bye";
        pages.add(p);

        return pages;
    }


    //===========================================================================
    // Search for pages with certain links
    //===========================================================================


}
