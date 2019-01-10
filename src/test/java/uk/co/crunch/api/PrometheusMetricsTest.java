package uk.co.crunch.api;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.TestableTimeProvider;
import io.prometheus.client.hotspot.StandardExports;
import org.junit.Before;
import org.junit.Test;
import uk.co.crunch.api.PrometheusMetrics.Context;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static uk.co.crunch.TestUtils.samplesString;

public class PrometheusMetricsTest {
    private PrometheusMetrics metrics;
    private CollectorRegistry registry;

    @Before
    public void setUp() throws IOException {
        TestableTimeProvider.install();

        registry = new CollectorRegistry();
        metrics = new PrometheusMetrics(registry, "MyApp");

        final Properties props = new Properties();
        try (Reader r = Files.newReader( new File("src/test/resources/app.properties"), Charsets.UTF_8)) {
            props.load(r);
        }

        metrics.setDescriptionMappings(props);
    }

    @Test
    public void testDefaultConstructor() {
        final PrometheusMetrics pm = new PrometheusMetrics();
        pm.counter("counter_1").inc(1701);
        assertThat(pm.registry.getSampleValue("counter_1")).isEqualTo(1701d);

        assertThat(registry.getSampleValue("counter_1")).isNull();
   }

    @Test
    public void testDropwizardTimerCompatibility() {
        try (Context timer = metrics.timer("Test.timer#a").time()) {
            System.out.println("Hi");
        }

        assertThat(samplesString(registry)).startsWith("[Name: myapp_test_timer_a Type: SUMMARY Help: myapp_test_timer_a")
                .contains("Name: myapp_test_timer_a_count LabelNames: [] labelValues: [] Value: 1.0 TimestampMs: null, Name: myapp_test_timer_a_sum LabelNames: [] labelValues: [] Value: 1.979E-6");
        assertThat(registry.getSampleValue("myapp_test_timer_a_sum") * 1E+9).isEqualByComparingTo(1979d);
    }

    @Test
    public void testDropwizardHistogramCompatibility() {
        metrics.histogram("response-sizes").update(30000).update(4535);
        assertThat(samplesString(registry)).isEqualTo("[Name: myapp_response_sizes Type: HISTOGRAM Help: myapp_response_sizes Samples: [Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.005] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.01] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.025] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.05] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.075] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.1] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.25] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.5] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [0.75] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [1.0] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [2.5] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [5.0] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [7.5] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [10.0] Value: 0.0 TimestampMs: null, Name: myapp_response_sizes_bucket LabelNames: [le] labelValues: [+Inf] Value: 2.0 TimestampMs: null, Name: myapp_response_sizes_count LabelNames: [] labelValues: [] Value: 2.0 TimestampMs: null, Name: myapp_response_sizes_sum LabelNames: [] labelValues: [] Value: 34535.0 TimestampMs: null]]");
        assertThat(registry.getSampleValue("myapp_response_sizes_sum")).isEqualByComparingTo(34535d);
    }

    @Test
    public void testPluggableDescriptions() {
        metrics.gauge("sizes-with-desc").inc(198);
        assertThat(samplesString(registry)).contains("Name: myapp_sizes_with_desc Type: GAUGE Help: Response Sizes なお知らせ (bytes) Samples");
    }

    @SuppressWarnings({"UnusedReturnValue", "SameReturnValue"})
    private String getTimedValueDemonstratingFriendlyTimingSyntax() {
        try (Context ignored = metrics.histogram("Test_calc1").time()) {
            return "Hi";
        }
    }

    @Test
    public void testHistograms() {
        getTimedValueDemonstratingFriendlyTimingSyntax();

        assertThat(samplesString(registry)).isEqualTo("[Name: myapp_test_calc1 Type: HISTOGRAM Help: myapp_test_calc1 Samples: [Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.005] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.01] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.025] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.05] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.075] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.1] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.25] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.5] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [0.75] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [1.0] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [2.5] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [5.0] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [7.5] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [10.0] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_bucket LabelNames: [le] labelValues: [+Inf] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_count LabelNames: [] labelValues: [] Value: 1.0 TimestampMs: null, Name: myapp_test_calc1_sum LabelNames: [] labelValues: [] Value: 1.979E-6 TimestampMs: null]]");
        assertThat(registry.getSampleValue("myapp_test_calc1_sum") * 1E+9).isEqualByComparingTo(1979d);

        // Update existing one
        metrics.histogram("Test_calc1").update(0.00000032d);
        assertThat(registry.getSampleValue("myapp_test_calc1_sum") * 1E+9).isEqualByComparingTo(2299d);
    }

    @Test
    public void testHistogramWithExplicitDesc() {
        try (Context timer = metrics.histogram("MyName", "MyDesc").time()) {
            // Something
        }

        assertThat(samplesString(registry)).isEqualTo("[Name: myapp_myname Type: HISTOGRAM Help: MyDesc Samples: [Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.005] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.01] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.025] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.05] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.075] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.1] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.25] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.5] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [0.75] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [1.0] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [2.5] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [5.0] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [7.5] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [10.0] Value: 1.0 TimestampMs: null, Name: myapp_myname_bucket LabelNames: [le] labelValues: [+Inf] Value: 1.0 TimestampMs: null, Name: myapp_myname_count LabelNames: [] labelValues: [] Value: 1.0 TimestampMs: null, Name: myapp_myname_sum LabelNames: [] labelValues: [] Value: 1.979E-6 TimestampMs: null]]");
        assertThat(registry.getSampleValue("myapp_myname_sum") * 1E+9).isEqualByComparingTo(1979d);
    }

    @Test
    public void testSummaryTimers() {
        try (Context timer = metrics.summary("Test_calc1").time()) {
            System.out.println("First");
        }

        try (Context timer = metrics.summary("Test_calc1").time()) {
            System.out.println("Second");
        }

        assertThat(samplesString(registry)).startsWith("[Name: myapp_test_calc1 Type: SUMMARY Help: myapp_test_calc1 ")
                .contains("Name: myapp_test_calc1 LabelNames: [quantile] labelValues: [0.5] Value: 1.979E-6")
                .contains("Name: myapp_test_calc1 LabelNames: [quantile] labelValues: [0.75] Value: 1.979E-6")
                .contains("Name: myapp_test_calc1 LabelNames: [quantile] labelValues: [0.9] Value: 1.979E-6")
                .contains("Name: myapp_test_calc1 LabelNames: [quantile] labelValues: [0.95] Value: 1.979E-6")
                .contains("Name: myapp_test_calc1 LabelNames: [quantile] labelValues: [0.99] Value: 1.979E-6")
                .contains("Name: myapp_test_calc1 LabelNames: [quantile] labelValues: [0.999] Value: 1.979E-6")
                .contains("Name: myapp_test_calc1_count LabelNames: [] labelValues: [] Value: 2.0")
                .contains("Name: myapp_test_calc1_sum LabelNames: [] labelValues: [] Value: 3.958E-6");
    }

    @Test
    public void testSummaryObservations() {
        metrics.summary("Vals").update(1212.213412).observe(3434.34234).observe(3.1415926535875);

        assertThat(samplesString(registry)).contains("Name: myapp_vals_count LabelNames: [] labelValues: [] Value: 3.0 TimestampMs: null, Name: myapp_vals_sum LabelNames: [] labelValues: [] Value: 4649.697344653588");
    }

    @Test
    public void testCounter() {
        final double expected = System.nanoTime();

        metrics.counter("counter_1", "My first counter").inc(expected);
        assertThat(registry.getSampleValue("myapp_counter_1")).isEqualTo(expected);

        assertThat(samplesString(registry)).startsWith("[Name: myapp_counter_1 Type: COUNTER Help: My first counter Samples:");

        metrics.counter("counter_1").inc();
        assertThat(registry.getSampleValue("myapp_counter_1")).isEqualTo(expected + 1);
    }

    @Test
    public void testErrors() {
        metrics.error("salesforce");
        assertThat(registry.getSampleValue("myapp_errors", new String[]{"error_type"}, new String[]{"salesforce"})).isEqualTo(1.0d);

        metrics.error("stripe_transaction", "Stripe transaction error");
        assertThat(registry.getSampleValue("myapp_errors", new String[]{"error_type"}, new String[]{"stripe_transaction"})).isEqualTo(1.0d);

        final PrometheusMetrics.ErrorCounter stErr = metrics.error("stripe_transaction");
        assertThat(stErr.count()).isEqualTo(2.0d);
        assertThat(registry.getSampleValue("myapp_errors", new String[]{"error_type"}, new String[]{"stripe_transaction"})).isEqualTo(2.0d);

        assertThat(registry.getSampleValue("myapp_errors", new String[]{"error_type"}, new String[]{"unknown"})).isNull();

        assertThat( metrics.error("stripe_transaction", "with desc this time").count() ).isEqualTo(3.0d);
    }

    @Test
    public void testGauge() {
        final double expected = System.nanoTime();
        assertThat(registry.getSampleValue("g_1")).isNull();

        metrics.gauge("g_1").inc(expected);
        assertThat(registry.getSampleValue("myapp_g_1")).isEqualTo(expected);

        metrics.gauge("g_1").inc();
        assertThat(registry.getSampleValue("myapp_g_1")).isEqualTo(expected + 1);

        metrics.gauge("g_1").dec();
        assertThat(registry.getSampleValue("myapp_g_1")).isEqualTo(expected);

        metrics.gauge("g_1", "desc").dec(1981);
        assertThat(registry.getSampleValue("myapp_g_1")).isEqualTo(expected - 1981);
    }

    @Test
    public void testClear() {
        assertThat(samplesString(registry)).isEqualTo("[]");

        metrics.counter("counter_1").inc(1);
        assertThat(samplesString(registry)).contains("counter_1").contains("Value: 1.0");

        metrics.clear();
        assertThat(samplesString(registry)).isEqualTo("[]");

        // Count again with the same name to check that no prior state is maintained
        metrics.counter("counter_1").inc(21);
        assertThat(samplesString(registry)).contains("counter_1").contains("Value: 21.0");

        metrics.clear();
        assertThat(samplesString(registry)).isEqualTo("[]");
    }

    @Test
    public void testHotspotExports() {
        final Collector c = new StandardExports();
        metrics.registerCustomCollector(c);
        c.collect();  // Force collection
        assertThat(samplesString(registry))
                .contains("Name: process_cpu_seconds_total Type: COUNTER")
                .contains("Name: process_cpu_seconds_total LabelNames")
                .contains("Name: process_open_fds Type: GAUGE");
    }

    @SuppressWarnings("CheckReturnValue")
    @Test
    public void testCannotReuseMetricName() {
        metrics.counter("xxx", "My first counter");

        try {
            metrics.gauge("xxx");
            fail("Should not pass");
        }
        catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("myapp_xxx is already used for a different type of metric");
        }
    }
}
