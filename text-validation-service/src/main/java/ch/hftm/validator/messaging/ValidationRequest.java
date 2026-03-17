package ch.hftm.validator.messaging;

public class ValidationRequest {
    public String entityType;
    public Long entityId;
    public String text;

    public ValidationRequest() {
    }

    public ValidationRequest(String entityType, Long entityId, String text) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.text = text;
    }
}