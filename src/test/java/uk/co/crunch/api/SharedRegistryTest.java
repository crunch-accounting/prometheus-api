package uk.co.crunch.api;

import io.prometheus.client.CollectorRegistry;
import org.junit.Test;

public class SharedRegistryTest {
    private final CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    @SuppressWarnings("CheckReturnValue")
    @Test
    public void testExample() {
        final PrometheusMetrics metrics1 = new PrometheusMetrics(registry, "Example");
        metrics1.counter("hello");
        metrics1.gauge("gauge");
        metrics1.histogram("histogram");
        metrics1.summary("summary");
        metrics1.error("error");

        final PrometheusMetrics metrics2 = new PrometheusMetrics(registry, "Example");
        metrics2.counter("hello");
        metrics2.gauge("gauge");
        metrics2.histogram("histogram");
        metrics2.summary("summary");
        metrics2.error("error");
    }
}
