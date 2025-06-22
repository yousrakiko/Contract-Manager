package contract.management.bea.CONTRACT_MANAGER.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import contract.management.bea.CONTRACT_MANAGER.model.User;
import contract.management.bea.CONTRACT_MANAGER.model.UserRole;
import contract.management.bea.CONTRACT_MANAGER.repository.UserRepository;
import contract.management.bea.CONTRACT_MANAGER.repository.UserRoleRepository;
import contract.management.bea.CONTRACT_MANAGER.service.ActionLogService;
import contract.management.bea.CONTRACT_MANAGER.service.NotificationService;
import contract.management.bea.CONTRACT_MANAGER.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActionLogService actionLogService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getListUsers());
        return "usersList";
    }

    @GetMapping("/json")
    @ResponseBody
    public List<User> getUsersJson() {
        return userService.getListUsers();
    }

    @GetMapping("/form")
    public String showUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", userRoleRepository.findAll());
        return "add_user_form";
    }

    @PostMapping("/add")
    public String createUser(@Valid @ModelAttribute("user") User user,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", userRoleRepository.findAll());
            return "add_user_form";
        }

        if (userService.isEmailExists(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email is already in use");
            model.addAttribute("roles", userRoleRepository.findAll());
            return "add_user_form";
        }

        if (userService.isMatriculeExists(user.getMatricule())) {
            result.rejectValue("matricule", "error.user", "Matricule is already in use");
            model.addAttribute("roles", userRoleRepository.findAll());
            return "add_user_form";
        }

        try {
            userService.addUser(user);
            String loggedUserMatricule = SecurityContextHolder.getContext().getAuthentication().getName();

            actionLogService.logUserAction(
                    "user",
                    user.getMatricule(),
                    "CREATE",
                    null,
                    user.getRole() != null ? user.getRole().getRoleName() : null,
                    "Created new user",
                    loggedUserMatricule
            );

            redirectAttributes.addFlashAttribute("successMessage", "User created successfully");
            return "redirect:/userManager";
        } catch (Exception e) {
            result.rejectValue("email", "error.user", "Error creating user: " + e.getMessage());
            model.addAttribute("roles", userRoleRepository.findAll());
            return "add_user_form";
        }
    }

    @GetMapping("/{matricule}")
    public String viewUserDetails(@PathVariable String matricule, Model model) {
        User user = userService.getUserByMatricule(matricule);
        model.addAttribute("user", user);
        model.addAttribute("roles", userRoleRepository.findAll());  
        return "userDetails";
    }

    @PostMapping("/{matricule}/change-role")
    public String changeUserRole(@PathVariable String matricule,
                               @RequestParam("newRole") String newRoleName,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByMatricule(matricule);
            UserRole newRole = userRoleRepository.findByRoleName(newRoleName);

            if (newRole == null) {
                throw new RuntimeException("Role not found");
            }

            // Log the role change
            String loggedUserMatricule = SecurityContextHolder.getContext().getAuthentication().getName();
            actionLogService.logUserAction(
                "user",
                matricule,
                "UPDATE",
                user.getRole().getRoleName(),
                newRoleName,
                "Changed user role",
                loggedUserMatricule
            );

            user.setRole(newRole);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("message", "Role updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating role: " + e.getMessage());
        }
        return "redirect:/api/users/" + matricule;
    }

    @GetMapping("/{matricule}/pdf")
    public void downloadPdf(@PathVariable String matricule, HttpServletResponse response) throws IOException {
        File pdfFile = userService.getUserPdfFile(matricule);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + pdfFile.getName());
        Files.copy(pdfFile.toPath(), response.getOutputStream());
        response.getOutputStream().flush();
    }
    @PostMapping("/{matricule}/deactivate")
public String deactivateUser(@PathVariable String matricule, RedirectAttributes redirectAttributes) {
    try {
        userService.deactivateUser(matricule);

        String loggedUserMatricule = SecurityContextHolder.getContext().getAuthentication().getName();
        actionLogService.logUserAction(
            "user",
            matricule,
            "UPDATE",
            "ACTIVE",
            "INACTIVE",
            "Deactivated user account",
            loggedUserMatricule
        );

        redirectAttributes.addFlashAttribute("message", "User deactivated successfully.");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error deactivating user: " + e.getMessage());
    }

    return "redirect:/userManager";
}
@PostMapping("/{matricule}/reactivate")
public String reactivateUser(@PathVariable String matricule, RedirectAttributes redirectAttributes) {
    try {
        userService.activateUser(matricule);
        String loggedUserMatricule = SecurityContextHolder.getContext().getAuthentication().getName();
        actionLogService.logUserAction(
            "user",
            matricule,
            "UPDATE",
            "INACTIVE",
            "ACTIVE",
            "Reactivated user account",
            loggedUserMatricule
        );

        redirectAttributes.addFlashAttribute("successMessage", "User reactivated successfully.");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Error reactivating user: " + e.getMessage());
    }
    return "redirect:/userManager";
}

    @PostMapping("/update-email")
    public ResponseEntity<?> updateEmail(@RequestBody Map<String, String> request, 
                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String newEmail = request.get("email");
            if (newEmail == null || newEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email cannot be empty");
            }

            String matricule = userDetails.getUsername();
            userService.updateUserEmail(matricule, newEmail);
            
            // Create notification for admin
            String notificationTitle = "Email Update";
            String notificationMessage = "User " + matricule + " has updated their email to " + newEmail;
            String notificationLink = "/api/users/" + matricule;
            
            notificationService.sendNotificationToRole(
                "ADMIN",
                notificationTitle,
                notificationMessage,
                notificationLink
            );
            
            return ResponseEntity.ok().body(Map.of("message", "Email updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Failed to update email: " + e.getMessage());
        }
    }
}
