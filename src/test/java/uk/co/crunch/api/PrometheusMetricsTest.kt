package uk.co.crunch.api

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.TestableTimeProvider
import io.prometheus.client.hotspot.StandardExports
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import strikt.assertions.startsWith
import uk.co.crunch.TestUtils.samplesString
import java.io.File
import java.util.*
import kotlin.test.fail

class PrometheusMetricsTest {
    private lateinit var metrics: PrometheusMetrics
    private lateinit var registry: CollectorRegistry

    private val timedValueDemonstratingFriendlyTimingSyntax: String
        get() = metrics.histogram("Test_calc1").time().use { return "Hi" }

    @BeforeEach
    fun setUp() {
        TestableTimeProvider.install()

        registry = CollectorRegistry()
        metrics = PrometheusMetrics(registry, "MyApp")

        val props = Properties()
        File("src/test/resources/app.properties").reader(Charsets.UTF_8).use { r -> props.load(r) }

        metrics.setDescriptionMappings(props)
    }

    @Test
    fun defaultConstructor() {
        val pm = PrometheusMetrics()
        pm.counter("counter_1").inc(1701.0)
        expectThat(pm.registry.getSampleValue("counter_1")).isEqualTo(1701.0)

        expectThat(registry.getSampleValue("counter_1")).isNull()
    }

    @Test
    fun dropwizardTimerCompatibility() {
        metrics.timer("Test.timer#a").time().use { println("Hi") }

        expectThat(samplesString(registry)).startsWith("[Name: myapp_test_timer_a Type: SUMMARY Help: myapp_test_timer_a")
                .contains("Name: myapp_test_timer_a_count LabelNames: [] labelValues: [] Value: 1.0 TimestampMs: null, Name: myapp_test_timer_a_sum LabelNames: [] labelValues: [] Value: 1.979E-6")
        expectThat(registry.getSampleValue("myapp_test_timer_a_sum")!! * 1E+9).isEqualTo(1979.0)
    }

    @Test
    fun fropwizardHistogramCompatibility() {
        metrics.histogram("response-sizes").update(30000.0).update(4535.0)
        expectThat(samplesString(registry)).isEqualTo("[Name: myapp_response_sizes Type: HISTOGRAM Help: myapp_response_sizes Samples: [Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.005] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.01] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.025] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.05] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.075] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.1] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.25] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.5] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.75] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [1.0] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [2.5] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [5.0] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [7.5] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [10.0] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [+Inf] Value: 2.0 TimestampMs: null, Name: myapp_response_sizes_count LabelNames: [] labelValues: [] Value: 2.0 TimestampMs: null, Name: myapp_response_sizes_sum LabelNames: [] labelValues: [] Value: 34535.0 TimestampMs: null]]")
        expectThat(registry.getSampleValue("myapp_response_sizes_sum")).isEqualTo(34535.0)
    }

    @Test
    fun pluggableDescriptions() {
        metrics.gauge("sizes-with-desc").inc(198.0)
        expectThat(samplesString(registry)).contains("Name: myapp_sizes_with_desc Type: GAUGE Help: Response Sizes なお知らせ (bytes) Samples")
    }

    @Test
    fun histograms() {
        timedValueDemonstratingFriendlyTimingSyntax

        expectThat(samplesString(registry)).isEqualTo("[Name: myapp_test_calc1 Type: HISTOGRAM Help: myapp_test_calc1 Samples: [Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.005] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.01] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.025] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.05] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.075] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.1] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.25] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.5] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.75] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [1.0] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [2.5] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [5.0] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [7.5] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [10.0] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [+Inf] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_count LabelNames: [] labelValues: [] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_sum LabelNames: [] labelValues: [] Value: 1.979E-6 TimestampMs: null]]")
        expectThat(registry.getSampleValue("myapp_test_calc1_sum")!! * 1E+9).isEqualTo(1979.0)

        // Update existing one
        metrics.histogram("Test_calc1").update(0.00000032)
        expectThat(registry.getSampleValue("myapp_test_calc1_sum")!! * 1E+9).isEqualTo(2299.0)
    }

    @Test
    fun histogramWithExplicitDesc() {
        metrics.histogram("MyName", "MyDesc").time().use {
            // Something
        }

        expectThat(samplesString(registry)).isEqualTo("[Name: myapp_myname Type: HISTOGRAM Help: MyDesc Samples: [Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.005] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.01] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.025] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.05] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.075] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.1] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.25] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.5] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.75] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [1.0] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [2.5] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [5.0] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [7.5] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [10.0] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [+Inf] Value: 1.0 TimestampMs: null, Name: myapp_myname_count LabelNames: [] labelValues: [] Value: 1.0 TimestampMs: null, Name: myapp_myname_sum LabelNames: [] labelValues: [] Value: 1.979E-6 TimestampMs: null]]")
        expectThat(registry.getSampleValue("myapp_myname_sum")!! * 1E+9).isEqualTo(1979.0)
    }

    @Test
    fun summaryTimers() {
        metrics.summary("Test_calc1").time().use { println("First") }

        metrics.summary("Test_calc1").time().use { println("Second") }

        expectThat(samplesString(registry)).startsWith("[Name: myapp_test_calc1 Type: SUMMARY Help: myapp_test_calc1 ")
                .contains("Name: myapp_test_calc1 LabelNames: [quantile] labelValues: [0.5] Value: 1.979E-6")
                .contains("Name: myapp_test_calc1 LabelNames: [quantile] labelValues: [0.75] Value: 1.979E-6")
                .contains("Name: myapp_test_calc1 LabelNames: [quantile] labelValues: [0.9] Value: 1.979E-6")
                .contains("Name: myapp_test_calc1 LabelNames: [quantile] labelValues: [0.95] Value: 1.979E-6")
                .contains("Name: myapp_test_calc1 LabelNames: [quantile] labelValues: [0.99] Value: 1.979E-6")
                .contains("Name: myapp_test_calc1 LabelNames: [quantile] labelValues: [0.999] Value: 1.979E-6")
                .contains("Name: myapp_test_calc1_count LabelNames: [] labelValues: [] Value: 2.0")
                .contains("Name: myapp_test_calc1_sum LabelNames: [] labelValues: [] Value: 3.958E-6")
    }

    @Test
    fun summaryObservations() {
        metrics.summary("Vals").update(1212.213412).observe(3434.34234).observe(3.1415926535875)

        expectThat(samplesString(registry)).contains("Name: myapp_vals_count LabelNames: [] labelValues: [] Value: 3.0 TimestampMs: null, Name: myapp_vals_sum LabelNames: [] labelValues: [] Value: 4649.697344653588")
    }

    @Test
    fun counter() {
        val expected = System.nanoTime().toDouble()

        metrics.counter("counter_1", "My first counter").inc(expected)
        expectThat(registry.getSampleValue("myapp_counter_1")).isEqualTo(expected)

        expectThat(samplesString(registry)).startsWith("[Name: myapp_counter_1 Type: COUNTER Help: My first counter Samples:")

        metrics.counter("counter_1").inc()
        expectThat(registry.getSampleValue("myapp_counter_1")).isEqualTo(expected + 1)
    }

    @Test
    fun errors() {
        metrics.error("salesforce")
        expectThat(registry.getSampleValue("myapp_errors", arrayOf("error_type"), arrayOf("salesforce"))).isEqualTo(1.0)

        metrics.error("stripe_transaction", "Stripe transaction error")
        expectThat(registry.getSampleValue("myapp_errors", arrayOf("error_type"), arrayOf("stripe_transaction"))).isEqualTo(1.0)

        val stErr = metrics.error("stripe_transaction")
        expectThat(stErr.count()).isEqualTo(2.0)
        expectThat(registry.getSampleValue("myapp_errors", arrayOf("error_type"), arrayOf("stripe_transaction"))).isEqualTo(2.0)

        expectThat(registry.getSampleValue("myapp_errors", arrayOf("error_type"), arrayOf("unknown"))).isNull()

        expectThat(metrics.error("stripe_transaction", "with desc this time").count()).isEqualTo(3.0)
    }

    @Test
    fun gauge() {
        val expected = System.nanoTime().toDouble()
        expectThat(registry.getSampleValue("g_1")).isNull()

        metrics.gauge("g_1").inc(expected)
        expectThat(registry.getSampleValue("myapp_g_1")).isEqualTo(expected)

        metrics.gauge("g_1").inc()
        expectThat(registry.getSampleValue("myapp_g_1")).isEqualTo(expected + 1)

        metrics.gauge("g_1").dec()
        expectThat(registry.getSampleValue("myapp_g_1")).isEqualTo(expected)

        metrics.gauge("g_1", "desc").dec(1981.0)
        expectThat(registry.getSampleValue("myapp_g_1")).isEqualTo(expected - 1981)
    }

    @Test
    fun testClear() {
        expectThat(samplesString(registry)).isEqualTo("[]")

        metrics.counter("counter_1").inc(1.0)
        expectThat(samplesString(registry)).contains("counter_1").contains("Value: 1.0")

        metrics.clear()
        expectThat(samplesString(registry)).isEqualTo("[]")

        // Count again with the same name to check that no prior state is maintained
        metrics.counter("counter_1").inc(21.0)
        expectThat(samplesString(registry)).contains("counter_1").contains("Value: 21.0")

        metrics.clear()
        expectThat(samplesString(registry)).isEqualTo("[]")
    }

    @Test
    fun hotspotExports() {
        val c = StandardExports()
        metrics.registerCustomCollector(c)
        c.collect()  // Force collection
        expectThat(samplesString(registry))
                .contains("Name: process_cpu_seconds_total Type: COUNTER")
                .contains("Name: process_cpu_seconds_total LabelNames")
                .contains("Name: process_open_fds Type: GAUGE")
    }

    @Test
    fun cannotReuseMetricName() {
        metrics.counter("xxx", "My first counter")

        try {
            metrics.gauge("xxx")
            fail("Should not pass")
        } catch (e: IllegalArgumentException) {
            expectThat(e.message).isEqualTo("myapp_xxx is already used for a different type of metric")
        }
    }
}
