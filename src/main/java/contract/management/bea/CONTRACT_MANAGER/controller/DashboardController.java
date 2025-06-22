package contract.management.bea.CONTRACT_MANAGER.controller;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import contract.management.bea.CONTRACT_MANAGER.model.User;
import contract.management.bea.CONTRACT_MANAGER.repository.ContractsRepository;
import contract.management.bea.CONTRACT_MANAGER.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final ContractsRepository contractsRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public DashboardController(ContractsRepository contractsRepository, 
                             UserRepository userRepository,
                             MessageSource messageSource) {
        this.contractsRepository = contractsRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal, HttpServletRequest request) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();
        User user = userRepository.findByMatricule(username);

        if (user == null) {
            return "redirect:/auth/login";
        }

        String fullName = user.getFirstName() + " " + user.getLastName(); 
        String role = user.getRole().getRoleName();

        model.addAttribute("name", fullName);
        model.addAttribute("role", role);
        model.addAttribute("user", user);

        Locale currentLocale = LocaleContextHolder.getLocale();
        String welcomeMessage = messageSource.getMessage("welcomeToast", 
            new Object[]{role.replace("ROLE_", "")}, 
            currentLocale);
        model.addAttribute("welcomeMessage", welcomeMessage);

        logger.info("Current locale: {}", currentLocale);
        logger.info("Request locale: {}", request.getLocale());
        logger.info("Accept-Language header: {}", request.getHeader("Accept-Language"));
        
        // Test messages
        String welcome = messageSource.getMessage("welcome", null, currentLocale);
        String dashboard = messageSource.getMessage("dashboard", null, currentLocale);
        String contracts = messageSource.getMessage("contracts", null, currentLocale);
        
        logger.info("Test messages - Welcome: {}, Dashboard: {}, Contracts: {}", 
            welcome, dashboard, contracts);

        // Contract stats
        long activeCount = contractsRepository.countByStatus("active");
        long pendingCount = contractsRepository.countByStatus("pending");
        long approvedCount = contractsRepository.countByStatus("signed");
        long declinedCount = contractsRepository.countByStatus("rejected");

        model.addAttribute("activeCount", activeCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("declinedCount", declinedCount);

        // Chart data
        List<Object[]> approvedPerMonth = contractsRepository.countApprovedContractsPerMonth();
        List<Object[]> declinedPerMonth = contractsRepository.countDeclinedContractsPerMonth();

        int[] approvedMonthly = new int[12];
        int[] declinedMonthly = new int[12];

        for (Object[] row : approvedPerMonth) {
            int month = ((Number) row[0]).intValue() - 1;
            int count = ((Number) row[1]).intValue();
            approvedMonthly[month] = count;
        }

        for (Object[] row : declinedPerMonth) {
            int month = ((Number) row[0]).intValue() - 1;
            int count = ((Number) row[1]).intValue();
            declinedMonthly[month] = count;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            model.addAttribute("approvedMonthly", mapper.writeValueAsString(approvedMonthly));
            model.addAttribute("declinedMonthly", mapper.writeValueAsString(declinedMonthly));
        } catch (JsonProcessingException e) {
            model.addAttribute("approvedMonthly", "[]");
            model.addAttribute("declinedMonthly", "[]");
        }

        return "dashboard";
    }

    @GetMapping("/test-locale")
    @ResponseBody
    public String testLocale(@RequestParam(defaultValue = "en") String lang) {
        Locale locale = Locale.forLanguageTag(lang);
        LocaleContextHolder.setLocale(locale);
        
        logger.info("Testing locale: {}", locale);
        
        String welcome = messageSource.getMessage("welcome", null, locale);
        String dashboard = messageSource.getMessage("dashboard", null, locale);
        String contracts = messageSource.getMessage("contracts", null, locale);
        
        return String.format("Locale: %s\nWelcome: %s\nDashboard: %s\nContracts: %s", 
            locale, welcome, dashboard, contracts);
    }
}
