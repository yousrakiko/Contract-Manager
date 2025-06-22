package contract.management.bea.CONTRACT_MANAGER.dto;

import java.time.LocalDateTime;

public class CommentDTO {
    private Integer id;
    private String content;
    private LocalDateTime createdAt;
    private Integer contractId;
    private String authorMatricule;

    public CommentDTO() {}

    public CommentDTO(Integer id, String content, LocalDateTime createdAt, Integer contractId, String authorMatricule) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.contractId = contractId;
        this.authorMatricule = authorMatricule;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getContractId() {
        return contractId;
    }

    public void setContractId(Integer contractId) {
        this.contractId = contractId;
    }

    public String getAuthorMatricule() {
        return authorMatricule;
    }

    public void setAuthorMatricule(String authorMatricule) {
        this.authorMatricule = authorMatricule;
    }
}
