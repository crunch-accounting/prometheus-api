package uk.co.crunch.api

import io.prometheus.client.CollectorRegistry
import org.junit.jupiter.api.Test

class SharedRegistryTest {
    private val registry = CollectorRegistry.defaultRegistry

    @Test
    fun example() {
        val metrics1 = PrometheusMetrics(registry, "Example")
        metrics1.counter("hello")
        metrics1.gauge("gauge")
        metrics1.histogram("histogram")
        metrics1.summary("summary")
        metrics1.error("error")

        val metrics2 = PrometheusMetrics(registry, "Example")
        metrics2.counter("hello")
        metrics2.gauge("gauge")
        metrics2.histogram("histogram")
        metrics2.summary("summary")
        metrics2.error("error")
    }
}
