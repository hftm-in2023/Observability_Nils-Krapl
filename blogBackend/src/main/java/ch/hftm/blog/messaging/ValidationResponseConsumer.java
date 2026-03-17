package ch.hftm.blog.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import ch.hftm.blog.control.BlogService;
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;

@ApplicationScoped
public class ValidationResponseConsumer {

    @Inject
    BlogService blogService;

    private final Jsonb jsonb = JsonbBuilder.create();

    @Incoming("validation-responses")
    @Blocking
    public void onMessage(String payload) {
        Log.infof("Received validation response payload: %s", payload);

        ValidationResponse response = jsonb.fromJson(payload, ValidationResponse.class);

        if (!"BLOG".equals(response.entityType)) {
            Log.warnf("Ignoring validation response for unsupported entityType=%s", response.entityType);
            return;
        }

        Log.infof("Processing validation response for blog id=%d: approved=%s", response.entityId, response.approved);
        blogService.applyValidationResult(
                response.entityId,
                response.approved,
                response.reason
        );
    }
}
