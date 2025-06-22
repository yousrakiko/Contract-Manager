package contract.management.bea.CONTRACT_MANAGER.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import contract.management.bea.CONTRACT_MANAGER.model.User;
import contract.management.bea.CONTRACT_MANAGER.repository.UserRepository;

@Controller
public class PageController {

    @Autowired
    private UserRepository userRepository;

   

    @GetMapping("/contractsManager")
    public String contractsManager() {
        return "contractsManager"; 
    }

    
    @GetMapping("/userManager")
    public String userManager() {
        return "userManager"; 
    }
    @GetMapping("/track")
    public String tracking() {
        return "contractsReview";
    }

    @GetMapping("/analyse")
    public String reports(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        User user = userRepository.findByMatricule(username);
        String role = user.getRole().getRoleName();

        model.addAttribute("name", user.getFirstName());
        model.addAttribute("user", user);
        model.addAttribute("role", role);
        return "analyse"; 
    }
}