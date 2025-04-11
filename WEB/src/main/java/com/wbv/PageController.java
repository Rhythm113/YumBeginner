package com.wbv;


import com.wbv.model.User;
import com.wbv.repository.UserRepository;
import com.wbv.service.GeminiService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class PageController {

    private final GeminiService geminiService;
    private final ErrorAttributes errorAttributes;

    @Autowired
    public PageController(GeminiService geminiService, ErrorAttributes errorAttributes) {
        this.geminiService = geminiService;
        this.errorAttributes = errorAttributes;
    }


    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/auth")
    public String showAuthPage(Model model) {
        model.addAttribute("loginUser", new User());
        model.addAttribute("registerUser", new User());
        return "auth";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser != null) {
            model.addAttribute("profile", loggedInUser);
            return "profile";
        } else {
            return "redirect:/auth";
        }
    }

    @GetMapping("/cookwithai")
    public String cookWithAI() {
        return "cookwithai";
    }


    @PostMapping("/getRecipe")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getRecipe(@RequestParam("ingredients") List<String> ingredients,
                                                         @RequestParam("dietary") String dietary,
                                                         @RequestParam("cuisine") String cuisine) {
        String allIngredients = String.join(", ", ingredients);
        String recipe = geminiService.generateRecipe(allIngredients, dietary, cuisine);

        Map<String, String> response = new HashMap<>();
        response.put("recipe", recipe);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/beef-curry")
    public String beefCurry() {
        return "Beef-Curry";
    }

    @GetMapping("/bhuna-khichuri")
    public String bhunaKhichuri() {
        return "Bhuna-Khichuri";
    }

    @GetMapping("/chicken-curry")
    public String chickenCurry() {
        return "Chicken-Curry";
    }

    @GetMapping("/fish-curry")
    public String fishCurry() {
        return "Fish-Curry";
    }

    @GetMapping("/roasted-chicken")
    public String roastedChicken() {
        return "roasted-chicken";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registerUser") User user,
                               @RequestParam("email") String email,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Model model) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            model.addAttribute("error", "Username already exists");
            model.addAttribute("showRegisterForm", true);
            model.addAttribute("loginUser", new User());
            return "auth";
        }
        if (userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "Email already exists");
            model.addAttribute("showRegisterForm", true);
            model.addAttribute("loginUser", new User());
            user.setEmail(email);
            return "auth";
        }
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("showRegisterForm", true);
            model.addAttribute("loginUser", new User());
            user.setEmail(email);
            return "auth";
        }

        user.setEmail(email);
        userRepository.save(user);
        model.addAttribute("success", "Registration successful! Please login.");
        model.addAttribute("showLoginForm", true);
        model.addAttribute("loginUser", new User());
        model.addAttribute("registerUser", new User());
        return "auth";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            Model model, HttpServletRequest request) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            model.addAttribute("message", "Login successful!");
            return "index";
        } else {
            model.addAttribute("error", "Invalid username or password");
            model.addAttribute("showLoginForm", true);
            model.addAttribute("loginUser", new User());
            model.addAttribute("registerUser", new User());
            return "auth";
        }
    }
// err handling
@RequestMapping("/error")
public String handleError(HttpServletRequest request, Model model) {
    WebRequest webRequest = new ServletWebRequest(request);
    Map<String, Object> errors = this.errorAttributes.getErrorAttributes(webRequest,
            org.springframework.boot.web.error.ErrorAttributeOptions.defaults());

    HttpStatus status = getStatus(request);
    model.addAttribute("statusCode", status.value());
    model.addAttribute("statusMessage", status.getReasonPhrase());
    model.addAttribute("errorMessage", errors.get("message"));
    model.addAttribute("timestamp", errors.get("timestamp"));
    model.addAttribute("path", errors.get("path"));

    return "error";
}

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }




}