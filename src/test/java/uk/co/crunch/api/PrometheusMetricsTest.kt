package uk.co.crunch.api

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.TestableTimeProvider
import io.prometheus.client.hotspot.StandardExports
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isNull
import uk.co.crunch.TestUtils.samplesString
import java.io.File
import java.util.*

class PrometheusMetricsTest {
    private lateinit var metrics: PrometheusMetrics
    private lateinit var registry: CollectorRegistry

    private val timedValueDemonstratingFriendlyTimingSyntax: String
        get() = metrics.histogram("Test_calc1").time().use { return "Hi" }

    @Before
    fun setUp() {
        TestableTimeProvider.install()

        registry = CollectorRegistry()
        metrics = PrometheusMetrics(registry, "MyApp")

        val props = Properties()
        File("src/test/resources/app.properties").reader(Charsets.UTF_8).use { r -> props.load(r) }

        metrics.setDescriptionMappings(props)
    }

    @Test
    fun testDefaultConstructor() {
        val pm = PrometheusMetrics()
        pm.counter("counter_1").inc(1701.0)
        assertThat(pm.registry.getSampleValue("counter_1")).isEqualTo(1701.0)

        expectThat(registry.getSampleValue("counter_1")).isNull()
    }

    @Test
    fun testDropwizardTimerCompatibility() {
        metrics.timer("Test.timer#a").time().use { println("Hi") }

        assertThat(samplesString(registry)).startsWith("[Name: myapp_test_timer_a Type: SUMMARY Help: myapp_test_timer_a")
                .contains("Name: myapp_test_timer_a_count LabelNames: [] labelValues: [] Value: 1.0 TimestampMs: null, Name: myapp_test_timer_a_sum LabelNames: [] labelValues: [] Value: 1.979E-6")
        assertThat(registry.getSampleValue("myapp_test_timer_a_sum")!! * 1E+9).isEqualByComparingTo(1979.0)
    }

    @Test
    fun testDropwizardHistogramCompatibility() {
        metrics.histogram("response-sizes").update(30000.0).update(4535.0)
        assertThat(samplesString(registry)).isEqualTo("[Name: myapp_response_sizes Type: HISTOGRAM Help: myapp_response_sizes Samples: [Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.005] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.01] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.025] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.05] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.075] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.1] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.25] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.5] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.75] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [1.0] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [2.5] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [5.0] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [7.5] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [10.0] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [+Inf] Value: 2.0 TimestampMs: null, Name: myapp_response_sizes_count LabelNames: [] labelValues: [] Value: 2.0 TimestampMs: null, Name: myapp_response_sizes_sum LabelNames: [] labelValues: [] Value: 34535.0 TimestampMs: null]]")
        assertThat(registry.getSampleValue("myapp_response_sizes_sum")).isEqualByComparingTo(34535.0)
    }

    @Test
    fun testPluggableDescriptions() {
        metrics.gauge("sizes-with-desc").inc(198.0)
        assertThat(samplesString(registry)).contains("Name: myapp_sizes_with_desc Type: GAUGE Help: Response Sizes なお知らせ (bytes) Samples")
    }

    @Test
    fun testHistograms() {
        timedValueDemonstratingFriendlyTimingSyntax

        assertThat(samplesString(registry)).isEqualTo("[Name: myapp_test_calc1 Type: HISTOGRAM Help: myapp_test_calc1 Samples: [Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.005] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.01] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.025] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.05] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.075] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.1] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.25] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.5] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.75] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [1.0] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [2.5] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [5.0] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [7.5] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [10.0] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [+Inf] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_count LabelNames: [] labelValues: [] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_sum LabelNames: [] labelValues: [] Value: 1.979E-6 TimestampMs: null]]")
        assertThat(registry.getSampleValue("myapp_test_calc1_sum")!! * 1E+9).isEqualByComparingTo(1979.0)

        // Update existing one
        metrics.histogram("Test_calc1").update(0.00000032)
        assertThat(registry.getSampleValue("myapp_test_calc1_sum")!! * 1E+9).isEqualByComparingTo(2299.0)
    }

    @Test
    fun testHistogramWithExplicitDesc() {
        metrics.histogram("MyName", "MyDesc").time().use {
            // Something
        }

        assertThat(samplesString(registry)).isEqualTo("[Name: myapp_myname Type: HISTOGRAM Help: MyDesc Samples: [Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.005] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.01] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.025] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.05] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.075] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.1] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.25] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.5] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.75] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [1.0] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [2.5] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [5.0] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [7.5] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [10.0] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [+Inf] Value: 1.0 TimestampMs: null, Name: myapp_myname_count LabelNames: [] labelValues: [] Value: 1.0 TimestampMs: null, Name: myapp_myname_sum LabelNames: [] labelValues: [] Value: 1.979E-6 TimestampMs: null]]")
        assertThat(registry.getSampleValue("myapp_myname_sum")!! * 1E+9).isEqualByComparingTo(1979.0)
    }

    @Test
    fun testSummaryTimers() {
        metrics.summary("Test_calc1").time().use { println("First") }

        metrics.summary("Test_calc1").time().use { println("Second") }

        assertThat(samplesString(registry)).startsWith("[Name: myapp_test_calc1 Type: SUMMARY Help: myapp_test_calc1 ")
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
    fun testSummaryObservations() {
        metrics.summary("Vals").update(1212.213412).observe(3434.34234).observe(3.1415926535875)

        assertThat(samplesString(registry)).contains("Name: myapp_vals_count LabelNames: [] labelValues: [] Value: 3.0 TimestampMs: null, Name: myapp_vals_sum LabelNames: [] labelValues: [] Value: 4649.697344653588")
    }

    @Test
    fun testCounter() {
        val expected = System.nanoTime().toDouble()

        metrics.counter("counter_1", "My first counter").inc(expected)
        assertThat(registry.getSampleValue("myapp_counter_1")).isEqualTo(expected)

        assertThat(samplesString(registry)).startsWith("[Name: myapp_counter_1 Type: COUNTER Help: My first counter Samples:")

        metrics.counter("counter_1").inc()
        assertThat(registry.getSampleValue("myapp_counter_1")).isEqualTo(expected + 1)
    }

    @Test
    fun testErrors() {
        metrics.error("salesforce")
        assertThat(registry.getSampleValue("myapp_errors", arrayOf("error_type"), arrayOf("salesforce"))).isEqualTo(1.0)

        metrics.error("stripe_transaction", "Stripe transaction error")
        assertThat(registry.getSampleValue("myapp_errors", arrayOf("error_type"), arrayOf("stripe_transaction"))).isEqualTo(1.0)

        val stErr = metrics.error("stripe_transaction")
        assertThat(stErr.count()).isEqualTo(2.0)
        assertThat(registry.getSampleValue("myapp_errors", arrayOf("error_type"), arrayOf("stripe_transaction"))).isEqualTo(2.0)

        expectThat(registry.getSampleValue("myapp_errors", arrayOf("error_type"), arrayOf("unknown"))).isNull()

        assertThat(metrics.error("stripe_transaction", "with desc this time").count()).isEqualTo(3.0)
    }

    @Test
    fun testGauge() {
        val expected = System.nanoTime().toDouble()
        expectThat(registry.getSampleValue("g_1")).isNull()

        metrics.gauge("g_1").inc(expected)
        assertThat(registry.getSampleValue("myapp_g_1")).isEqualTo(expected)

        metrics.gauge("g_1").inc()
        assertThat(registry.getSampleValue("myapp_g_1")).isEqualTo(expected + 1)

        metrics.gauge("g_1").dec()
        assertThat(registry.getSampleValue("myapp_g_1")).isEqualTo(expected)

        metrics.gauge("g_1", "desc").dec(1981.0)
        assertThat(registry.getSampleValue("myapp_g_1")).isEqualTo(expected - 1981)
    }

    @Test
    fun testClear() {
        assertThat(samplesString(registry)).isEqualTo("[]")

        metrics.counter("counter_1").inc(1.0)
        assertThat(samplesString(registry)).contains("counter_1").contains("Value: 1.0")

        metrics.clear()
        assertThat(samplesString(registry)).isEqualTo("[]")

        // Count again with the same name to check that no prior state is maintained
        metrics.counter("counter_1").inc(21.0)
        assertThat(samplesString(registry)).contains("counter_1").contains("Value: 21.0")

        metrics.clear()
        assertThat(samplesString(registry)).isEqualTo("[]")
    }

    @Test
    fun testHotspotExports() {
        val c = StandardExports()
        metrics.registerCustomCollector(c)
        c.collect()  // Force collection
        assertThat(samplesString(registry))
                .contains("Name: process_cpu_seconds_total Type: COUNTER")
                .contains("Name: process_cpu_seconds_total LabelNames")
                .contains("Name: process_open_fds Type: GAUGE")
    }

    @Test
    fun testCannotReuseMetricName() {
        metrics.counter("xxx", "My first counter")

        try {
            metrics.gauge("xxx")
            fail<Any>("Should not pass")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("myapp_xxx is already used for a different type of metric")
        }
    }
}
