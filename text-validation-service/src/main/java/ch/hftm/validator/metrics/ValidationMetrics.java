package ch.hftm.validator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ValidationMetrics {

    private final Counter validationRequestsCounter;
    private final Timer validationTimer;

    public ValidationMetrics(MeterRegistry registry) {
        this.validationRequestsCounter = Counter.builder("validation_requests_total")
                .description("Number of validation requests")
                .register(registry);

        this.validationTimer = Timer.builder("validation_duration_seconds")
                .description("Validation duration")
                .register(registry);
    }

    public void validationRequested() {
        validationRequestsCounter.increment();
    }

    public Timer timer() {
        return validationTimer;
    }
}