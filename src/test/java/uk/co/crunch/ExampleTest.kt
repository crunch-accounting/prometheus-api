package uk.co.crunch

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.TestableTimeProvider
import org.junit.Before
import org.junit.Test
import uk.co.crunch.api.PrometheusMetrics

import org.assertj.core.api.Assertions.assertThat
import uk.co.crunch.TestUtils.samplesString

class ExampleTest {
    private var registry: CollectorRegistry? = null

    @Before
    fun setUp() {
        TestableTimeProvider.install()
        registry = CollectorRegistry()
    }

    @Test
    fun testExample() {
        val ex = Example(PrometheusMetrics(registry!!, "Example"))

        assertThat(registry!!.getSampleValue("example_sessions_open")).isNull()
        assertThat(registry!!.getSampleValue("example_errors", arrayOf("error_type"), arrayOf("generic"))).isNull()

        val resp = ex.handleLogin()
        assertThat(resp).isEqualTo("Login handled!")  // Fairly pointless, just for PiTest coverage %
        ex.onUserLogin("")
        assertThat(registry!!.getSampleValue("example_sessions_open")).isEqualTo(1.0)

        ex.onUserLogout("")
        assertThat(registry!!.getSampleValue("example_sessions_open")).isEqualTo(0.0)

        ex.onError(Throwable())
        assertThat(registry!!.getSampleValue("example_errors", arrayOf("error_type"), arrayOf("generic"))).isEqualTo(1.0)

        val contents = samplesString(registry!!)
        assertThat(contents).contains("Name: example_errors Type: COUNTER Help: Generic errors Samples: [Name: example_errors LabelNames: [error_type] labelValues: [generic] Value: 1.0")
        assertThat(contents).contains("Name: example_sessions_handlelogin Type: SUMMARY Help: Login times")
        assertThat(contents).contains("Name: example_sessions_handlelogin_count LabelNames: [] labelValues: [] Value: 1.0 TimestampMs: null, Name: example_sessions_handlelogin_sum LabelNames: [] labelValues: [] Value: 1.979E-6")
        assertThat(contents).contains("Name: example_sessions_open Type: GAUGE Help: example_sessions_open Samples: [Name: example_sessions_open LabelNames: [] labelValues: [] Value: 0.0")
    }
}
