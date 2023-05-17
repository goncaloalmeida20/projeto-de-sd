package com.example.webserver;

import classes.AdminInfo;
import classes.Page;
import com.example.webserver.forms.Login;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.HtmlUtils;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

@EnableScheduling
@Controller
public class WebserverController {

    //===========================================================================
    // Constants
    //===========================================================================

    private static final Logger logger = LoggerFactory.getLogger(WebserverController.class);
    private static final int RESULTS_PER_PAGE = 10;
    private static final int MAX_RMI_CONCURRENT_CALLS = 100;
    @Autowired
    private SimpMessagingTemplate template;

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
    @ResponseBody
    public String processRegistration(@RequestParam("username") String username,
                                      @RequestParam("password") String password) throws RemoteException {
        int res = rmiw.maven_register(username, password, RequestContextHolder.currentRequestAttributes().getSessionId());
        if (res == 0) {
            return "register-error"; // Redirect to error page if client already exists
        } else {
            return "register-success"; // Redirect to login page if client is registered
        }
    }

    @GetMapping("/login")
    public String createLoginForm(Model model) {
        model.addAttribute("login", new Login());
        return "login";
    }

    @PostMapping("/save-login")
    @ResponseBody
    public String saveLoginSubmission(@ModelAttribute Login login) throws RemoteException {
        int res = rmiw.maven_login(login.getUsername(), login.getPassword(), RequestContextHolder.currentRequestAttributes().getSessionId());
        logger.info(String.valueOf(res));
        if (res == 0) {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("login", true);
            return "already-login"; // Redirect to client page if Client already logged on!
        } else if (res == 1){
            return "logged-in"; // Redirect to client page if Client just logged in
        } else{
            return "invalid-credentials"; // Redirect to login page if Client inserted invalid credentials
        }
    }

    @PostMapping("/logout")
    @ResponseBody
    public String logout() throws RemoteException {
        int res = rmiw.maven_logout(RequestContextHolder.currentRequestAttributes().getSessionId());
        if (res == 0) {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("login", false);
            return "logged-off"; // Redirect to login page if Client is log off
        } else{
            return "is-guest"; // Redirect to guest page if Client is not logged on
        }
    }

    @PostMapping("/check-authentication")
    @ResponseBody
    public String checkAuthentication() {
       ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
       HttpSession session = attr.getRequest().getSession(true);
       if((boolean) session.getAttribute("login")){
           return "authenticated";
       }
       return "not-authenticated";
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


        return "redirect:/client";
    }

    @GetMapping("/top-stories")
    public String topStories(){
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

                HackerNewsItemRecord hnir = restTemplate.getForObject(formattedTopStoryURL, HackerNewsItemRecord.class);
                if(hnir == null || hnir.url() == null || hnir.text() == null
                        || termsJson.stream().noneMatch(hnir.text()::contains)) continue;
                logger.info("Indexing top story " + topStory + "  with URL: " + formattedTopStoryURL);
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
    public List<Page> searchTermsResults(@RequestBody String[] terms) throws RemoteException {
        return rmiw.maven_search(terms.length, terms);
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
    public List<Page> searchLinksResults(@RequestBody String url, @RequestParam(required = false, defaultValue = "1") int page) throws RemoteException {
        List<Page> pages = rmiw.maven_searchPages(url);
        System.out.println(pages.size());
        // Apply pagination
        int startIndex = (page - 1) * RESULTS_PER_PAGE;
        int endIndex = Math.min(startIndex + RESULTS_PER_PAGE, pages.size());
        List<Page> paginatedPages = pages.subList(startIndex, endIndex);

        return paginatedPages;
    }


    //===========================================================================
    // Admin Info
    //===========================================================================

    @Scheduled(fixedRate = 3000)
    public void onAdminInfo() throws RemoteException, InterruptedException {
        Thread.sleep(1000);
        this.template.convertAndSend("/topic/admin", rmiw.maven_admin());
    }


    @GetMapping("/admin/info")
    public AdminInfo getAdminInfo() throws RemoteException {
        return rmiw.maven_admin();
    }
}
