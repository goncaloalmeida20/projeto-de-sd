package com.example.webserver;

import RMISearchModule.SearchModuleC_S_I;
import classes.Page;
import com.example.webserver.forms.Login;
import com.example.webserver.the_data.AdminInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

@Controller
public class WebserverController {

    //===========================================================================
    // Constants
    //===========================================================================

    private static final Logger logger = LoggerFactory.getLogger(WebserverController.class);
    private static final int RESULTS_PER_PAGE = 10;
    private static final int MAX_RMI_CONCURRENT_CALLS = 100;

    //===========================================================================
    // RMIWrapper
    //===========================================================================

    @Resource(name="applicationRMISemaphore")
    private Semaphore RMISem;

    @Resource(name="sessionRMIWrapper")
    private RMIWrapper rmiw;

    @Bean
    @Scope(value= WebApplicationContext.SCOPE_APPLICATION, proxyMode=ScopedProxyMode.TARGET_CLASS)
    public Semaphore applicationRMISemaphore() throws RemoteException {
        return new Semaphore(MAX_RMI_CONCURRENT_CALLS);
    }

    @Bean
    @Scope(value= WebApplicationContext.SCOPE_SESSION, proxyMode=ScopedProxyMode.TARGET_CLASS)
    public RMIWrapper sessionRMIWrapper() throws RemoteException {
        return new RMIWrapper(RMISem);
    }

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
        //TODO: Verify the registration and add to the database

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String createLoginForm(Model model) {
        model.addAttribute("login", new Login());
        return "login";
    }

    @PostMapping("/save-login")
    public String saveLoginSubmission(@ModelAttribute Login login, Model model) {
        if (validateLogin(login)) {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("login", true);
            return "redirect:/client";
        } else {
            model.addAttribute("errorMessage", "Invalid login credentials");
            return "login";
        }
    }


    public boolean validateLogin(Login login){
        //TODO: VALIDATE LOGIN in the server

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
            rmiw.indexUrl(url);
            logger.info("Indexed URL: " + url);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return "redirect:/index-url";
    }

    @GetMapping("/top-stories")
    public String topStories(Model model){
        return "top-stories";
    }

    @PostMapping("top-stories-results")
    @ResponseBody
    public List<String> topStoriesResults(@RequestBody List<String> termsJson){
        String topStoriesURL = "https://hacker-news.firebaseio.com/v0/topstories.json";
        RestTemplate restTemplate = new RestTemplate();
        List hnTopStories = restTemplate.getForObject(topStoriesURL, List.class);
        List<String> topStoryURLs = new ArrayList<>();
        try {
            for (var topStory : hnTopStories) {
                String formattedTopStoryURL = "https://hacker-news.firebaseio.com/v0/item/" +
                        topStory.toString() +
                        ".json";
                logger.info("Analyzing top story with URL: " + formattedTopStoryURL);
                HackerNewsItemRecord hnir = restTemplate.getForObject(formattedTopStoryURL, HackerNewsItemRecord.class);
                if(hnir == null || hnir.url() == null || hnir.title() == null
                        || termsJson.stream().noneMatch(hnir.title()::contains)) continue;

                String storyURL = hnir.url().toLowerCase();
                rmiw.indexUrl(storyURL);
                topStoryURLs.add(storyURL);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return topStoryURLs;
    }

    @GetMapping("/user-stories")
    public String userStories(){
        return "user-stories";
    }

    @PostMapping("/user-stories-results")
    @ResponseBody
    public List<String> topStoriesResults(@RequestBody String user){
        List<String> userStoryURLs = new ArrayList<>();
        try {
            logger.info(user);
            //user = user.replace("\"", "");
            String hnUserURL = "https://hacker-news.firebaseio.com/v0/user/" + user + ".json";
            logger.info(hnUserURL);
            RestTemplate restTemplate = new RestTemplate();
            HackerNewsUserRecord hnUser = restTemplate.getForObject(hnUserURL, HackerNewsUserRecord.class);
            if(hnUser == null || hnUser.submitted() == null){
                return null;
            }
            for (var userStory : hnUser.submitted()) {
                String formattedUserStoryURL = "https://hacker-news.firebaseio.com/v0/item/" +
                        userStory.toString() +
                        ".json";
                //logger.info("Analyzing top story with URL: " + formattedUserStoryURL);
                HackerNewsItemRecord hnir = restTemplate.getForObject(formattedUserStoryURL
                        , HackerNewsItemRecord.class);

                if(hnir == null || hnir.url() == null) continue;

                String storyURL = hnir.url().toLowerCase();
                rmiw.indexUrl(storyURL);
                userStoryURLs.add(storyURL);
                logger.info("Indexed URL: " + formattedUserStoryURL);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        for (var v: userStoryURLs) logger.info(v);
        return userStoryURLs;
    }

    //===========================================================================
    // Search for pages with certain terms
    //===========================================================================


    @GetMapping("/search-terms")
    public String showSearchTermsPage() {
        return "search-terms";
    }

    @PostMapping("/search-terms-results")
    @ResponseBody
    public List<Page> searchTermsResults(@RequestBody String url) {
        // TODO: With terms, get pages from the server that contain the terms and save them in the "pages" variable
        //logger.info(terms.toString());
        List<Page> pages = new ArrayList<>();
        Page p = new Page();
        p.title = "OLA";
        p.url = "hello";
        p.citation = "bye";
        pages.add(p);
        p.title = "ADEUS";
        p.url = "bye";
        p.citation = "hello";
        pages.add(p);

        return pages;
    }


    //===========================================================================
    // Search for pages with certain links
    //===========================================================================


    @GetMapping("/search-links")
    public String showSearchLinksPage(Model model) {
        model.addAttribute("RESULTS_PER_PAGE", RESULTS_PER_PAGE);
        return "search-links";
    }


    @PostMapping("/search-links-results")
    @ResponseBody
    public List<Page> searchLinksResults(@RequestBody String url, @RequestParam(required = false, defaultValue = "1") int page) {
        // TODO: With links, get pages from the server that contain the link to the URL and save them in the "pages" variable
        // logger.info(url);
        List<Page> pages = new ArrayList<>();
        // Populate pages with the results

        // Apply pagination
        int startIndex = (page - 1) * RESULTS_PER_PAGE;
        int endIndex = Math.min(startIndex + RESULTS_PER_PAGE, pages.size());
        List<Page> paginatedPages = pages.subList(startIndex, endIndex);

        return paginatedPages;
    }


    //===========================================================================
    // Admin Info
    //===========================================================================


    /*@MessageMapping("/admin/info")
    @SendTo("/topic/admin")
    public AdminInfo getAdminInfo() {
        AdminInfo adminInfo = new AdminInfo();

        //TODO: Get info from server

        //TEST
        adminInfo.setMostSearchedItems(new ArrayList<>());
        adminInfo.setNumDownloads(2);
        adminInfo.setNumActiveBarrels(3);

        return adminInfo;
    }*/


    @GetMapping("/admin/info")
    @ResponseBody
    public AdminInfo getAdminInfo() {
        AdminInfo adminInfo = new AdminInfo();

        //TODO: Get info from server

        //TEST
        adminInfo.setMostSearchedItems(new ArrayList<>());
        adminInfo.setNumDownloads(2);
        adminInfo.setNumActiveBarrels(3);

        return adminInfo;
    }
}
