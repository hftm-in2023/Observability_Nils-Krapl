package ch.hftm.validator.messaging;

public class ValidationResponse {
    public String entityType;   // "BLOG" oder "COMMENT"
    public Long entityId;
    public boolean approved;
    public String reason;

    public ValidationResponse() {
    }

    public ValidationResponse(String entityType, Long entityId, boolean approved, String reason) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.approved = approved;
        this.reason = reason;
    }
}