package com.example.webserver;

import RMISearchModule.SearchModuleC_S_I;
import classes.Page;
import com.example.webserver.forms.Login;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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


    //===========================================================================
    // Search for pages with certain terms
    //===========================================================================


    @GetMapping("/search")
    public String showSearchPage() {
        return "search";
    }

    @PostMapping("/submit-search")
    public String search(@RequestParam("terms") List<String> terms, Model model) {
        //TODO: With terms get pages

        //TESTE
        List<Page> pages = new ArrayList<>();

        // Pass the search results to the HTML view
        model.addAttribute("pages", pages);

        // Pagination
        int numPages = (int) Math.ceil(pages.size() / (double) RESULTS_PER_PAGE);
        model.addAttribute("numPages", numPages);

        return "search-results";
    }


    //===========================================================================
    // Search for pages with certain links
    //===========================================================================


}
