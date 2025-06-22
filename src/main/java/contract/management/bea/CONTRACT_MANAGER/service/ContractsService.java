package contract.management.bea.CONTRACT_MANAGER.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import contract.management.bea.CONTRACT_MANAGER.model.ActionLog;
import contract.management.bea.CONTRACT_MANAGER.model.Contracts;
import contract.management.bea.CONTRACT_MANAGER.repository.ActionLogRepository;
import contract.management.bea.CONTRACT_MANAGER.repository.ContractsRepository;
;


@Service
public class ContractsService {

    @Autowired
    private ContractsRepository contractsRepository;

    @Autowired
    private ActionLogRepository actionLogRepository;

    public List<Contracts> getAllContracts() {
        return contractsRepository.findAll();
    }

    public Contracts addContract(Contracts contract) {
        return contractsRepository.save(contract);
    }

    public Contracts updateContract(Integer id, Contracts updatedContract) {
        return contractsRepository.findById(id).map(contract -> {
            contract.setCreatedAt(updatedContract.getCreatedAt());
            contract.setContractorName(updatedContract.getContractorName());
            contract.setIsSigned(updatedContract.getIsSigned());
            contract.setStatus(updatedContract.getStatus());
            contract.setType(updatedContract.getType());
            return contractsRepository.save(contract);
        }).orElseThrow(() -> new RuntimeException("Contract not found"));
    }

    public Map<String, Long> getPerformanceSnapshot() {
        LocalDateTime fiveMonthsAgo = LocalDateTime.now().minusMonths(5);
        long created = contractsRepository.countCreatedInLast5Months(fiveMonthsAgo);
        long signed = contractsRepository.countSignedInLast5Months(fiveMonthsAgo);
        long active = contractsRepository.countActiveInLast5Months(fiveMonthsAgo);

        Map<String, Long> snapshot = new HashMap<>();
        snapshot.put("createdContracts", created);
        snapshot.put("signedContracts", signed);
        snapshot.put("activeContracts", active);
        return snapshot;
    }

    public Contracts saveDraftContract(String title, String createdBy, String description, String contractorName,
                                       Contracts.ContractType type, MultipartFile file, String userMatricule) throws IOException {

        Contracts contract = new Contracts();
        contract.setContractTitle(title);
        contract.setCreatedBy(createdBy);
        contract.setDescription(description);
        contract.setContractorName(contractorName);
        contract.setType(type);
        contract.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        contract.setStatus("Draft");
        contract.setApprovalStage(Contracts.ApprovalStage.INITIAL);
        contract.setIsSigned(false);
        contract.setFileData(file.getBytes());

        Contracts saved = contractsRepository.save(contract);

        ActionLog log = new ActionLog(
            "contract",
            String.valueOf(saved.getContractId()),
            "CREATE_CONTRACT",
            null,
            "Draft",
            "New contract created: " + saved.getContractTitle(),
            userMatricule
        );
        actionLogRepository.save(log);

        return saved;
    }

    public Contracts updateDraftFile(Integer contractId, MultipartFile newFile, String userMatricule) throws IOException {
        Contracts contract = contractsRepository.findById(contractId).orElseThrow();

        if (!"Draft".equalsIgnoreCase(contract.getStatus())) {
            throw new IllegalStateException("Cannot update file after confirmation.");
        }

        contract.setFileData(newFile.getBytes());
        contract.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        Contracts updated = contractsRepository.save(contract);

        ActionLog log = new ActionLog(
            "contract",
            String.valueOf(updated.getContractId()),
            "UPDATE_FILE",
            null,
            "Draft",
            "Draft file updated for: " + updated.getContractTitle(),
            userMatricule
        );
        actionLogRepository.save(log);

        return updated;
    }

    public Contracts getById(Integer id) {
        return contractsRepository.findById(id).orElseThrow();
    }

    public Contracts confirmContract(Integer contractId, String userMatricule) {
        Contracts contract = contractsRepository.findById(contractId).orElseThrow();

        if (!"Draft".equalsIgnoreCase(contract.getStatus())) {
            throw new IllegalStateException("Only Draft contracts can be confirmed.");
        }

        String oldStatus = contract.getStatus();
        contract.setStatus("Pending");
        contract.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        Contracts updated = contractsRepository.save(contract);

        ActionLog log = new ActionLog(
            "contract",
            String.valueOf(updated.getContractId()),
            "STATUS_UPDATE",
            oldStatus,
            "Pending",
            "Status changed: " + oldStatus + " → Pending",
            userMatricule
        );
        actionLogRepository.save(log);

        return updated;
    }
    public void updateContractStatus(Integer id, String newStatus, String userMatricule) {
    Contracts contract = getById(id);
    String oldStatus = contract.getStatus();
    contract.setStatus(newStatus);
    contract.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    contractsRepository.save(contract);
  
}


}
