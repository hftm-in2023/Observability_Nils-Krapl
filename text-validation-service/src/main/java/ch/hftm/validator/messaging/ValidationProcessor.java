package ch.hftm.validator.messaging;

import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.List;

@ApplicationScoped
public class ValidationProcessor {

    private static final List<String> BLOCKLIST = List.of(
            "spam",
            "hate",
            "idiot",
            "stupid",
            "offensive",
            "scam"
    );

    @Incoming("validation-requests")
    @Outgoing("validation-responses")
    @Blocking
    public ValidationResponse process(ValidationRequest req) {
        String text = req.text == null ? "" : req.text;
        String lower = text.toLowerCase();

        boolean approved = BLOCKLIST.stream().noneMatch(lower::contains);
        String reason = approved ? "OK" : "Blocked by content policy";

        return new ValidationResponse(
                req.entityType,
                req.entityId,
                approved,
                reason
        );
    }
}