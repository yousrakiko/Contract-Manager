package contract.management.bea.CONTRACT_MANAGER.dto;

public class SimpleActionDto {
    private String module;
    private Integer referenceId;
    private String userMatricule;
    private String summary;
    private String timestamp;

    public SimpleActionDto() {}

    public SimpleActionDto(String module, Integer referenceId, String userMatricule, String summary, String timestamp) {
        this.module = module;
        this.referenceId = referenceId;
        this.userMatricule = userMatricule;
        this.summary = summary;
        this.timestamp = timestamp;
    }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public Integer getReferenceId() { return referenceId; }
    public void setReferenceId(Integer referenceId) { this.referenceId = referenceId; }

    public String getUserMatricule() { return userMatricule; }
    public void setUserMatricule(String userMatricule) { this.userMatricule = userMatricule; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

