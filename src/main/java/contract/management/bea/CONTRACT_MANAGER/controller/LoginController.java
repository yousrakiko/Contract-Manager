package contract.management.bea.CONTRACT_MANAGER.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import contract.management.bea.CONTRACT_MANAGER.model.User;
import contract.management.bea.CONTRACT_MANAGER.service.UserService;

@Controller
public class LoginController {

    @Autowired
    private UserService uService;

   
    @GetMapping("/login")
    public String loginPageLog() {
        return "login"; 
    }

    @GetMapping("/auth/gestionUser")
    public String getUser(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        User user = uService.getByLogin(principal.getName().trim());
        if (user == null) return "redirect:/login";

        model.addAttribute("matricule", user.getMatricule());
        model.addAttribute("pageNo", 1);
        return "users";
    }

    @GetMapping("/auth/loadInformation/{id}")
    public String loadpage(@PathVariable long id) {
        System.out.println("Load info for user ID " + id);
        return "loadpdf";
    }
}
