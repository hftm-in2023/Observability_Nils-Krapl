package ch.hftm.blog.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ValidationRequestProducer {

    @Inject
    @Channel("validation-requests")
    Emitter<String> emitter;

    private final Jsonb jsonb = JsonbBuilder.create();

    public void send(ValidationRequest request) {
        Log.infof("Sending validation request for entityType=%s, entityId=%d", request.entityType, request.entityId);
        emitter.send(jsonb.toJson(request));
    }
}