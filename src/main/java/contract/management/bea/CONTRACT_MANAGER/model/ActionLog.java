package contract.management.bea.CONTRACT_MANAGER.model;



import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "action_log")
public class ActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50)
    private String module;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "action_type", length = 100)
    private String actionType;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(length = 500)
    private String description;

    private String userMatricule;

    private LocalDateTime timestamp;

    public ActionLog() {}

    public ActionLog(String module, String referenceId, String actionType, String oldValue, String newValue, String description, String userMatricule) {
        this.module = module;
        this.referenceId = referenceId;
        this.actionType = actionType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.description = description;
        this.userMatricule = userMatricule;
        this.timestamp = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUserMatricule() { return userMatricule; }
    public void setUserMatricule(String userMatricule) { this.userMatricule = userMatricule; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public void setContractId(Integer contractId) {
 
        throw new UnsupportedOperationException("Unimplemented method 'setContractId'");
    }
}
