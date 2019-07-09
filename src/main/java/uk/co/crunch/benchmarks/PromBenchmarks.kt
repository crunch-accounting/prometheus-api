package uk.co.crunch.benchmarks

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import uk.co.crunch.api.PrometheusMetrics
import java.io.IOException
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.AverageTime)
open class PromBenchmarks {

    private val metrics = PrometheusMetrics()

    @Benchmark
    fun testIncrement(blackhole: Blackhole) {
        val count = 1
        metrics.counter("counter").inc(count.toDouble())
        blackhole.consume(count)  // Try to ensure we don't get optimised away
    }

    @Benchmark
    @Throws(IOException::class)
    fun testTiming(blackhole: Blackhole) {
        metrics.timer("Timer").time().use {
            blackhole.consume(it)  // Ensure we don't get optimised away
        }
    }
}
