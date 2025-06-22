package contract.management.bea.CONTRACT_MANAGER.model;

import java.sql.Timestamp;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "contracts")
public class Contracts {
     
    public enum ApprovalStage {
        INITIAL,
        MANAGER_REVIEW,
        FINAL_APPROVAL
    }

    public enum ContractType {
    supply,
    service 
}

public enum ContractStatus {
    pending,
    signed,
    approved,
    active,
    rejected,
    expired,
    modified,
    draft
}
    

    @Id
    @Column(name = "contract_id") 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Integer contractId;

    @Column(name = "contract_title")
    private String contractTitle;

    @Column(name = "created_by", length=20 )
    private String createdBy;

    @Column(name = "signed_by")
    private String signedBy;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "approval_stage")
    @Enumerated(EnumType.STRING)
    private ApprovalStage approvalStage;

    @Column(name = "is_signed")
    private Boolean isSigned;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "signed_at")
    private Timestamp signedAt;
    @Column(name = "starts_at")
    private Timestamp startsAt;

    @Column(name = "cancelled_at")
    private Timestamp cancelledAt;

 

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "contractor_name")
    private String contractorName;


    @Lob
    private byte[] fileData;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ContractType type;

    @Column(name = "budget_amount")
private Double budgetAmount;

@Column(name = "guarantee_amount")
private Double guaranteeAmount;


@Column(name = "end_date")
private LocalDate endDate;

public LocalDate getEndDate() {
    return endDate;
}

public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
}

public Double getBudgetAmount() {
    return budgetAmount;
}

public void setBudgetAmount(Double budgetAmount) {
    this.budgetAmount = budgetAmount;
}

public Double getGuaranteeAmount() {
    return guaranteeAmount;
}

public void setGuaranteeAmount(Double guaranteeAmount) {
    this.guaranteeAmount = guaranteeAmount;
}




    public Integer getContractId() {
        return contractId;
    }

    public void setContractId(Integer contractId) {
        this.contractId = contractId;
    }

    public String getContractTitle() {
        return contractTitle;
    }

    public void setContractTitle(String contractTitle) {
        this.contractTitle = contractTitle;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getSignedBy() {
        return signedBy;
    }

    public void setSignedBy(String signedBy) {
        this.signedBy = signedBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ApprovalStage getApprovalStage() {
        return approvalStage;
    }

    public void setApprovalStage(ApprovalStage approvalStage) {
        this.approvalStage = approvalStage;
    }

    public Boolean getIsSigned() {
        return isSigned;
    }

    public void setIsSigned(Boolean isSigned) {
        this.isSigned = isSigned;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Timestamp signedAt) {
        this.signedAt = signedAt;
    }

    public Timestamp getStartsdAt() {
        return startsAt;
    }

    public void setStartsAt(Timestamp startsdAt) {
        this.createdAt = startsAt;
    }

    public Timestamp getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Timestamp cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

  

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getContractorName() {
        return contractorName;
    }

    public void setContractorName(String contractorName) {
        this.contractorName = contractorName;
    }


public ContractType getType() {
    return type;
}

public void setType(ContractType type) {
    this.type = type;
}
public void setFileData(byte[] fileData) {
        this.fileData = fileData;
 }
   public byte[] getFileData() {
        return fileData;
    }


}


