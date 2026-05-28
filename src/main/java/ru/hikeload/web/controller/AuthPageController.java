package ru.hikeload.web.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.hikeload.service.AuthService;
import ru.hikeload.service.BusinessException;
import ru.hikeload.web.dto.RegisterRequest;
import ru.hikeload.web.form.RegisterForm;

@Controller
public class AuthPageController {

    private final AuthService authService;

    public AuthPageController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("form") RegisterForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            authService.register(new RegisterRequest(form.getEmail(), form.getDisplayName(), form.getPassword()));
            redirectAttributes.addFlashAttribute("successMessage", "Регистрация успешна. Войдите в систему.");
            return "redirect:/login";
        } catch (BusinessException ex) {
            bindingResult.reject("email", ex.getMessage());
            return "register";
        }
    }
}
