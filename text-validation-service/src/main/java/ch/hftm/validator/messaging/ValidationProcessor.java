package ch.hftm.validator.messaging;

import io.micrometer.core.instrument.Timer;
import io.smallrye.common.annotation.Blocking;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import ch.hftm.validator.metrics.ValidationMetrics;

import java.util.List;

@ApplicationScoped
public class ValidationProcessor {

    @Inject
    ValidationMetrics validationMetrics;

    private static final List<String> BLOCKLIST = List.of(
            "spam",
            "hate",
            "idiot",
            "stupid",
            "offensive",
            "scam");

    @Incoming("validation-requests")
    @Outgoing("validation-responses")
    @Blocking
    public ValidationResponse process(ValidationRequest req) {

        Log.infof("Received validation request for entityType=%s, entityId=%d", req.entityType, req.entityId);

        validationMetrics.validationRequested();
        Timer.Sample sample = Timer.start();

        try {
            String text = req.text == null ? "" : req.text;
            String lower = text.toLowerCase();

            boolean approved = BLOCKLIST.stream().noneMatch(lower::contains);
            String reason = approved ? "OK" : "Blocked by content policy";

            if (approved) {
                Log.infof("Validation approved for entityId=%d", req.entityId);
            } else {
                Log.warnf("Validation rejected for entityId=%d", req.entityId);
            }

            return new ValidationResponse(req.entityType, req.entityId, approved, reason);
        } finally {
            sample.stop(validationMetrics.timer());
            Log.infof("Finished validation for entityId=%d", req.entityId);
        }
    }
}