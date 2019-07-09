package uk.co.crunch.api

import io.prometheus.client.Collector
import io.prometheus.client.CollectorRegistry
import uk.co.crunch.utils.PrometheusUtils
import java.io.Closeable
import java.util.*
import java.util.Optional.empty
import java.util.Optional.of
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.CheckReturnValue

class PrometheusMetrics {
    private val metrics = ConcurrentHashMap<String, Metric>()

    internal var registry: CollectorRegistry

    private val metricNamePrefix: String
    private var errorCounter: io.prometheus.client.Counter? = null
    private var descriptionMappings = Properties()

    constructor() {
        this.registry = CollectorRegistry(true)
        this.metricNamePrefix = ""
    }

    constructor(registry: CollectorRegistry, metricNamePrefix: String) {
        this.registry = registry
        this.metricNamePrefix = PrometheusUtils.normaliseName(metricNamePrefix) + "_"
    }

    fun registerCustomCollector(collector: Collector) {
        this.registry.register(collector)
    }

    fun setDescriptionMappings(props: Properties) {
        this.descriptionMappings = props
    }

    // Clear out the configured CollectorRegistry as well as internal storage
    fun clear() {
        this.metrics.clear()
        this.registry.clear()
    }

    @CheckReturnValue
    fun timer(name: String) = summary(name)

    @CheckReturnValue
    fun timer(name: String, desc: String) = summary(name, desc)

    @CheckReturnValue
    fun histogram(name: String) = getOrAdd(name, empty(), MetricBuilder.HISTOGRAMS)

    @CheckReturnValue
    fun histogram(name: String, desc: String) = getOrAdd(name, of(desc), MetricBuilder.HISTOGRAMS)

    @CheckReturnValue
    fun summary(name: String) = getOrAdd(name, empty(), MetricBuilder.SUMMARIES)

    @CheckReturnValue
    fun summary(name: String, desc: String) = getOrAdd(name, of(desc), MetricBuilder.SUMMARIES)

    @CheckReturnValue
    fun timed(name: String) = getOrAdd(name, empty(), MetricBuilder.ONE_SHOT_TIMERS)

    @CheckReturnValue
    fun timed(name: String, desc: String) = getOrAdd(name, of(desc), MetricBuilder.ONE_SHOT_TIMERS)

    @CheckReturnValue
    fun counter(name: String) = getOrAdd(name, empty(), MetricBuilder.COUNTERS)

    @CheckReturnValue
    fun counter(name: String, desc: String) = getOrAdd(name, of(desc), MetricBuilder.COUNTERS)

    @CheckReturnValue
    fun gauge(name: String) = getOrAdd(name, empty(), MetricBuilder.GAUGES)

    @CheckReturnValue
    fun gauge(name: String, desc: String) = getOrAdd(name, of(desc), MetricBuilder.GAUGES)

    fun error(name: String) = incrementError(name, empty())
    fun error(name: String, desc: String) = incrementError(name, of(desc))

    private fun <T : Metric> getOrAdd(name: String, desc: Optional<String>, builder: MetricBuilder<T>): T {
        val adjustedName = metricNamePrefix + PrometheusUtils.normaliseName(name)

        // Get/check existing local metric
        val metric = metrics[adjustedName]
        if (metric != null) {
            if (builder.isInstance(metric)) {
                return metric as T
            }
            throw IllegalArgumentException("$adjustedName is already used for a different type of metric")
        }

        val description = desc.orElse(descriptionMappings.getProperty(name) ?: adjustedName)
        val newMetric = builder.newMetric(adjustedName, description, this.registry)

        this.metrics.putIfAbsent(adjustedName, newMetric)

        return newMetric
    }

    private fun incrementError(name: String, desc: Optional<String>): ErrorCounter {
        val counter = getErrorCounter(desc)!!.labels(name)
        counter.inc()
        return ErrorCounter(counter)
    }

    @Synchronized
    private fun getErrorCounter(desc: Optional<String>): io.prometheus.client.Counter? {
        if (this.errorCounter == null) {
            val adjustedName = metricNamePrefix + "errors"
            val description = desc.orElse( descriptionMappings.getProperty(adjustedName) ?: adjustedName)
            this.errorCounter = registerPrometheusMetric(io.prometheus.client.Counter.build().name(adjustedName).help(description).labelNames("error_type").create(), registry)
        }
        return this.errorCounter
    }

    private interface MetricBuilder<T : Metric> {

        fun newMetric(name: String, desc: String, registry: CollectorRegistry): T
        fun isInstance(metric: Metric): Boolean

        companion object {
            val COUNTERS: MetricBuilder<Counter> = object : MetricBuilder<Counter> {
                override fun newMetric(name: String, desc: String, registry: CollectorRegistry) =
                        Counter(registerPrometheusMetric(io.prometheus.client.Counter.build().name(name).help(desc).create(), registry))

                override fun isInstance(metric: Metric) = metric is Counter
            }

            val GAUGES: MetricBuilder<Gauge> = object : MetricBuilder<Gauge> {
                override fun newMetric(name: String, desc: String, registry: CollectorRegistry) =
                        Gauge(registerPrometheusMetric(io.prometheus.client.Gauge.build().name(name).help(desc).create(), registry))

                override fun isInstance(metric: Metric) = metric is Gauge
            }

            val HISTOGRAMS: MetricBuilder<Histogram> = object : MetricBuilder<Histogram> {
                override fun newMetric(name: String, desc: String, registry: CollectorRegistry) =
                        Histogram(registerPrometheusMetric(io.prometheus.client.Histogram.build().name(name).help(desc).create(), registry))

                override fun isInstance(metric: Metric) = metric is Histogram
            }

            val SUMMARIES: MetricBuilder<Summary> = object : MetricBuilder<Summary> {
                override fun newMetric(name: String, desc: String, registry: CollectorRegistry) =
                        Summary(registerPrometheusMetric(createSummary(name, desc), registry))

                override fun isInstance(metric: Metric) = metric is Summary
            }

            val ONE_SHOT_TIMERS: MetricBuilder<OneShotTimer> = object : MetricBuilder<OneShotTimer> {
                override fun newMetric(name: String, desc: String, registry: CollectorRegistry) =
                        OneShotTimer(registerPrometheusMetric(createSummary(name, desc), registry).startTimer())

                override fun isInstance(metric: Metric) = metric is OneShotTimer
            }
        }
    }

    private interface Metric

    interface Context : Closeable {
        override fun close()
    }

    class Counter internal constructor(private val promMetric: io.prometheus.client.Counter) : Metric {
        fun inc() = this.promMetric.inc()
        fun inc(incr: Double) = this.promMetric.inc(incr)
    }

    class Gauge internal constructor(private val promMetric: io.prometheus.client.Gauge) : Metric {
        fun inc() = this.promMetric.inc()
        fun inc(incr: Double) = this.promMetric.inc(incr)
        fun dec() = this.promMetric.dec()
        fun dec(incr: Double) = this.promMetric.dec(incr)
    }

    class ErrorCounter internal constructor(private val promMetric: io.prometheus.client.Counter.Child) : Metric {
        fun count() = this.promMetric.get()
    }

    class Summary internal constructor(private val promMetric: io.prometheus.client.Summary) : Metric {
        fun update(value: Double) = observe(value)

        fun observe(value: Double): Summary {
            this.promMetric.observe(value)
            return this
        }

        fun time() = TimerContext(promMetric.startTimer()) as Context

        private class TimerContext internal constructor(internal val requestTimer: io.prometheus.client.Summary.Timer) : Context {
            override fun close() = requestTimer.close()
        }
    }

    class OneShotTimer internal constructor(private val promTimer: io.prometheus.client.Summary.Timer) : Metric, Closeable {
        override fun close() = promTimer.close()
    }

    class Histogram internal constructor(private val promMetric: io.prometheus.client.Histogram) : Metric {

        fun time() = TimerContext(promMetric.startTimer()) as Context

        fun update(value: Double) = observe(value)

        private fun observe(value: Double): Histogram {
            this.promMetric.observe(value)
            return this
        }

        private class TimerContext internal constructor(internal val requestTimer: io.prometheus.client.Histogram.Timer) : Context {
            override fun close() = requestTimer.close()
        }
    }

    companion object {

        private fun createSummary(name: String, description: String) = io.prometheus.client.Summary.build()
                .name(name)
                .help(description)
                .quantile(0.5, 0.01)    // Median
                .quantile(0.75, 0.01)   // 75th percentile (1% tolerated error)
                .quantile(0.9, 0.01)    // 90th percentile
                .quantile(0.95, 0.01)   // 95th percentile
                .quantile(0.99, 0.01)   // 99th percentile
                .quantile(0.999, 0.01)  // 99.9th percentile
                .create()

        private fun <T : Collector> registerPrometheusMetric(metric: T, registry: CollectorRegistry): T {
            try {
                registry.register(metric)
            } catch (e: IllegalArgumentException) {  // NOSONAR
                // Collector already registered - we ignore this
            }

            return metric
        }
    }
}
