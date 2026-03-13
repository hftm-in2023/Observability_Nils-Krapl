package ch.hftm.blog.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlogMetrics {

    private final Counter blogCreatedCounter;
    private final Counter approvedCounter;
    private final Counter rejectedCounter;

    public BlogMetrics(MeterRegistry registry) {
        this.blogCreatedCounter = Counter.builder("blog_created_total")
                .description("Number of created blog posts")
                .register(registry);

        this.approvedCounter = Counter.builder("blog_validation_result_total")
                .description("Validation result of blog posts")
                .tag("result", "approved")
                .register(registry);

        this.rejectedCounter = Counter.builder("blog_validation_result_total")
                .description("Validation result of blog posts")
                .tag("result", "rejected")
                .register(registry);
    }

    public void incrementBlogCreated() {
        blogCreatedCounter.increment();
    }

    public void incrementApproved() {
        approvedCounter.increment();
    }

    public void incrementRejected() {
        rejectedCounter.increment();
    }
}