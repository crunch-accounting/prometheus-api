package uk.co.crunch

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.TestableTimeProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import uk.co.crunch.TestUtils.samplesString
import uk.co.crunch.api.PrometheusMetrics

class ExampleTest {
    private lateinit var registry: CollectorRegistry

    @BeforeEach
    fun setUp() {
        TestableTimeProvider.install()
        registry = CollectorRegistry()
    }

    @Test
    fun example() {
        val ex = Example(PrometheusMetrics(registry, "Example"))

        expectThat(registry.getSampleValue("example_sessions_open")).isNull()
        expectThat(registry.getSampleValue("example_errors", arrayOf("error_type"), arrayOf("generic"))).isNull()

        val resp = ex.handleLogin()
        expectThat(resp).isEqualTo("Login handled!")  // Fairly pointless, just for PiTest coverage %
        ex.onUserLogin("")
        expectThat(registry.getSampleValue("example_sessions_open")).isEqualTo(1.0)

        ex.onUserLogout("")
        expectThat(registry.getSampleValue("example_sessions_open")).isEqualTo(0.0)

        ex.onError(Throwable())
        expectThat(registry.getSampleValue("example_errors", arrayOf("error_type"), arrayOf("generic"))).isEqualTo(1.0)

        val contents = samplesString(registry)
        expectThat(contents).contains("Name: example_errors Type: COUNTER Help: Generic errors Samples: [Name: example_errors LabelNames: [error_type] labelValues: [generic] Value: 1.0")
        expectThat(contents).contains("Name: example_sessions_handlelogin Type: SUMMARY Help: Login times")
        expectThat(contents).contains("Name: example_sessions_handlelogin_count LabelNames: [] labelValues: [] Value: 1.0 TimestampMs: null, Name: example_sessions_handlelogin_sum LabelNames: [] labelValues: [] Value: 1.979E-6")
        expectThat(contents).contains("Name: example_sessions_open Type: GAUGE Help: example_sessions_open Samples: [Name: example_sessions_open LabelNames: [] labelValues: [] Value: 0.0")
    }
}
