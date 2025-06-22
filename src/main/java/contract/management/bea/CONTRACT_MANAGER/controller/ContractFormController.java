package contract.management.bea.CONTRACT_MANAGER.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import contract.management.bea.CONTRACT_MANAGER.model.AcquisitionContract;
import contract.management.bea.CONTRACT_MANAGER.model.Contracts;
import contract.management.bea.CONTRACT_MANAGER.repository.ContractsRepository;
import contract.management.bea.CONTRACT_MANAGER.service.WordTemplateService;

@Controller
    public class ContractFormController {

    @Autowired
    private WordTemplateService wordTemplateService;
     @Autowired
    private ContractsRepository contractsRepository;

    @PostMapping("/contracts/submit/acquisition")
public String handleAcquisition(
        @ModelAttribute AcquisitionContract formData,
        RedirectAttributes redirectAttributes) throws IOException {

    File docx = wordTemplateService.generateContractFromTemplate(formData, "acquisitionTemplate.docx");

    Contracts contractEntity = new Contracts();
    contractEntity.setContractTitle("Acquisition Contract - " + formData.getCompanyName());
    contractEntity.setCreatedBy("342361"); 
    contractEntity.setContractorName(formData.getCompanyName());
    contractEntity.setType(Contracts.ContractType.supply);
    contractEntity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    contractEntity.setApprovalStage(Contracts.ApprovalStage.INITIAL);
    contractEntity.setIsSigned(false);
    contractEntity.setStatus("draft");
    contractEntity.setFileData(Files.readAllBytes(docx.toPath()));

    Contracts savedContract = contractsRepository.save(contractEntity);

    return "redirect:/contracts/" + savedContract.getContractId() + "/details";
}

    @GetMapping("/form/prestation")
    public String showPrestationForm() {
        return "contractFormPrestation"; 
    }
    @GetMapping("/contracts/form/acquisition")
    public String showForm(Model model) {
        model.addAttribute("contractForm", new AcquisitionContract());
        return "contractFormAcquisition";
    }

    

    
}
