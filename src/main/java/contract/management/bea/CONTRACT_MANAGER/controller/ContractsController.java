package contract.management.bea.CONTRACT_MANAGER.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import contract.management.bea.CONTRACT_MANAGER.model.Comment;
import contract.management.bea.CONTRACT_MANAGER.model.Contracts;
import contract.management.bea.CONTRACT_MANAGER.model.User;
import contract.management.bea.CONTRACT_MANAGER.repository.CommentRepository;
import contract.management.bea.CONTRACT_MANAGER.repository.ContractsRepository;
import contract.management.bea.CONTRACT_MANAGER.repository.UserRepository;
import contract.management.bea.CONTRACT_MANAGER.service.ActionLogService;
import contract.management.bea.CONTRACT_MANAGER.service.ContractsService;
import contract.management.bea.CONTRACT_MANAGER.service.NotificationService;
import jakarta.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/contracts")
@CrossOrigin(origins = "*") // Allow frontend access (adjust as needed)
public class ContractsController {

    @Autowired
    private ContractsService contractsService;
    @Autowired
    private ContractsRepository contractsRepository;
    @Autowired
private ActionLogService actionLogService;
    @Autowired
    private NotificationService notificationservice;

    @GetMapping("/contracts/list")
    public String showContracts(Model model) {
    List<Contracts> allContracts = contractsRepository.findAll();
    model.addAttribute("contracts", allContracts);
    return "contractList"; // <-- This is your   Thymeleaf HTML page name
}

    @ResponseBody
    @GetMapping
    public List<Contracts> getAllContracts() {
        return contractsService.getAllContracts();
    }
        
    @ResponseBody
    @PostMapping
    public Contracts addContract(@RequestBody Contracts contract) {
        return contractsService.addContract(contract);
    }

   @ResponseBody
   @PostMapping("/update/{id}")
    public Contracts updateContract(@PathVariable Integer id, @RequestBody Contracts contract) {
        return contractsService.updateContract(id, contract);
    }

    @GetMapping("/create")
    public String showCreateContractPage() {
        return "createContract"; // make sure this file exists in templates/
    }
    

    @GetMapping("/form/prestation")
    public String showPrestationForm() {
        return "contractFormPrestation"; // make sure prestationForm.html exists
    }
    @GetMapping("/contracts/form/acquisition")
    public String showForm() {
        return "contractFormAcquisition"; // returns the HTML form
    }

    @GetMapping("/create/form")
    public String handleContractTypeSelection(@RequestParam("type") String type) {
        if ("acquisition".equalsIgnoreCase(type)) {
            return "redirect:/contracts/form/acquisition";
        } else if ("prestation".equalsIgnoreCase(type)) {
            return "redirect:/contracts/form/prestation";
        } else {
            return "redirect:/contracts/create"; // fallback
        }
    }
    @PostMapping("/generate")
public String generateContract(
    @RequestParam("title") String title,
    @RequestParam("createdBy") String createdBy,
    @RequestParam("description") String description,
    @RequestParam("contractorName") String contractorName,
    @RequestParam("type") Contracts.ContractType type,
    @RequestParam("file") MultipartFile file,
    Model model) throws IOException {

    String userMatricule = SecurityContextHolder.getContext().getAuthentication().getName();

    Contracts contract = contractsService.saveDraftContract(
        title, createdBy, description, contractorName, type, file, userMatricule);

    //Log contract creation
    actionLogService.logUserAction(
        "contract",
        contract.getContractId().toString(),
        "CREATE",
        null,
        contract.getStatus(),
        "Contract created by user",
        userMatricule
    );

    return "redirect:/contracts/" + contract.getContractId() + "/details";
}
    @GetMapping("/{id}/details")
    public String viewContractDetails(@PathVariable("id") Integer id, @RequestParam(value = "mode", required = false) String mode, Model model) {
        Contracts contract = contractsService.getById(id);
        
        if (contract == null) {
            return "error"; // or your error.html page
        }

        // Check if contract has comments
       boolean hasComment = commentRepository.existsByContractId(id);

        
        model.addAttribute("contract", contract);
        model.addAttribute("mode", mode); // This will be "edit" or null
        model.addAttribute("hasComment", hasComment);
        return "contract_details";
    }

    @GetMapping("/{id}/sign")
    public String viewContractSign(@PathVariable("id") Integer id, Model model) {
        Contracts contract = contractsService.getById(id);
        
        if (contract == null) {
            return "error"; // or your error.html page
        }
        model.addAttribute("contract", contract);
        model.addAttribute("mode", "sign");
        return "contract_sign";
    }

    @GetMapping("/{id}/comment")
    public String viewContractComment(@PathVariable("id") Integer id, Model model) {
    Contracts contract = contractsService.getById(id);
    
    if (contract == null) {
        return "error"; // or your error.html page
    }
    model.addAttribute("contract", contract);
        model.addAttribute("mode", "comment");
        return "contract_sign";
    }

    @GetMapping("/{id}/download")
public void downloadContractFile(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException {
    Contracts contract = contractsService.getById(id);
    if (contract != null && contract.getFileData() != null) {
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=contract_" + id + ".docx");
        response.getOutputStream().write(contract.getFileData());
        response.flushBuffer();
    } else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Contract file not found");
    }
}
@PostMapping("/{id}/upload")
public String uploadModifiedContract(@PathVariable("id") Integer id,
                                     @RequestParam("file") MultipartFile file,
                                     RedirectAttributes redirectAttributes) throws IOException {
    Contracts contract = contractsService.getById(id);

    if (contract == null) {
        return "redirect:/contracts/list";
    }

    if (!"draft".equalsIgnoreCase(contract.getStatus())) {
        redirectAttributes.addFlashAttribute("error", "Contract is no longer editable.");
        return "redirect:/contracts/" + id + "/details";
    }

    contract.setFileData(file.getBytes());
    contract.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

    contractsRepository.save(contract);
    actionLogService.logUserAction(
    "contract",
    contract.getContractId().toString(),
    "FILE_UPLOAD",
    null,
    contract.getStatus(),
    "User uploaded updated contract file",
    SecurityContextHolder.getContext().getAuthentication().getName()
);
    redirectAttributes.addFlashAttribute("message", "File updated successfully.");

    return "redirect:/contracts/" + id + "/details";
}

   @PostMapping("/{id}/confirm")
public String confirmContract(@PathVariable("id") Integer id) {
    String userMatricule = SecurityContextHolder.getContext().getAuthentication().getName();
try {
        // Update contract status
        contractsService.confirmContract(id, userMatricule);

        // Log confirmation
        actionLogService.logUserAction(
            "contract",
            id.toString(),
            "STATUS_UPDATE",
            "draft",
            "pending",
            "User confirmed contract",
            userMatricule
        );

        // Send notification to VALIDATOR2 role
        String title = "New Contract Pending Review";
        String message = "Contract #" + id + " has been confirmed and is pending your review.";
        String link = "/contracts/" + id + "/details";

        System.out.println("Attempting to send notification for contract confirmation...");
        notificationservice.sendNotificationToRole("VALIDATOR2", title, message, link);
        System.out.println("Notification sent successfully for contract #" + id);

    } catch (Exception e) {
        System.err.println("Error in contract confirmation: " + e.getMessage());
        e.printStackTrace();
    }

    return "redirect:/contractsManager";
}

@PostMapping("/{id}/review/sign")
public String signContract(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
    Contracts contract = contractsService.getById(id);
    if (contract == null) {
        redirectAttributes.addFlashAttribute("error", "Contract not found.");
        return "redirect:/contracts/list";
    }

    String userMatricule = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByMatricule(userMatricule);

    contract.setStatus("signed");
    contract.setSignedBy(userMatricule);
    contract.setSignedAt(new Timestamp(System.currentTimeMillis()));
    contract.setIsSigned(true);

    contractsRepository.save(contract);

    // Create notification for contract signing
    String signTitle = "Contract Signed";
    String signMessage = "User " + user.getFirstName() + " " + user.getLastName() + " has signed contract #" + id;
    String signLink = "/contracts/" + id + "/details";
    
    notificationservice.sendNotificationToRole("ADMIN", signTitle, signMessage, signLink);

    actionLogService.logUserAction(
        "contract",
        contract.getContractId().toString(),
        "SIGN",
        null,
        "signed",
        "Contract signed",
        userMatricule
    );

    redirectAttributes.addFlashAttribute("message", "Contract signed successfully!");
    return "redirect:/track";
}
@PostMapping("/{id}/review/reject")
public ResponseEntity<String> rejectContract(@PathVariable("id") Integer id) {
    Contracts contract = contractsService.getById(id);
    if (contract == null) {
        return ResponseEntity.notFound().build();
    }

    String userMatricule = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByMatricule(userMatricule);

    contract.setStatus("rejected");
    contract.setCancelledAt(new Timestamp(System.currentTimeMillis()));

    contractsRepository.save(contract);

    // Create notification for contract rejection
    String rejectTitle = "Contract Rejected";
    String rejectMessage = "User " + user.getFirstName() + " " + user.getLastName() + " has rejected contract #" + id;
    String rejectLink = "/contracts/" + id + "/details";
    
    notificationservice.sendNotificationToRole("ADMIN", rejectTitle, rejectMessage, rejectLink);

    actionLogService.logUserAction(
        "contract",
        contract.getContractId().toString(),
        "REJECT",
        null,
        "rejected",
        "Contract rejected ",
        userMatricule
    );

    return ResponseEntity.ok("Contract rejected successfully.");
}

@Autowired
private CommentRepository commentRepository;



@Autowired
private UserRepository userRepository;

@PostMapping("/{id}/review/comment")
public String addComment(@PathVariable("id") Integer id,
                         @RequestParam("comment") String content,
                         RedirectAttributes redirectAttributes) {
    Contracts contract = contractsService.getById(id);
    if (contract == null) {
        redirectAttributes.addFlashAttribute("error", "Contract not found.");
        return "redirect:/contracts/list";
    }

    String userMatricule = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByMatricule(userMatricule);

    if (user == null) {
        redirectAttributes.addFlashAttribute("error", "User not found.");
        return "redirect:/contracts/list";
    }

    Comment comment = new Comment();
    comment.setContent(content);
    comment.setContract(contract);
    comment.setAuthor(user);
    comment.setCreatedAt(LocalDateTime.now());

    commentRepository.save(comment);

    actionLogService.logUserAction(
        "contract",
        contract.getContractId().toString(),
        "COMMENT",
        null,
        null,
        "User left a comment",
        userMatricule
    );

    // Always send notifications for new comments
    String title = "New Comment on Contract";
    String message = "Contract #" + id + " has a new comment from " + user.getFirstName() + " " + user.getLastName() + ". Please review the contract.";
    String link = "/contractsManager";

    System.out.println("Sending notifications for new comment...");
    notificationservice.sendNotificationToRole("VALIDATOR1", title, message, link);
    notificationservice.sendNotificationToRole("ADMIN", title, message, link);
    System.out.println("Notifications sent successfully for contract #" + id);

    redirectAttributes.addFlashAttribute("message", "Comment submitted successfully.");
    return "redirect:/contracts/" + id + "/comment";
}

}

