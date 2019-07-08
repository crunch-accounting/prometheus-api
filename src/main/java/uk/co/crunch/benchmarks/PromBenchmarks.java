package uk.co.crunch.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import uk.co.crunch.api.PrometheusMetrics;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class PromBenchmarks {

    private final PrometheusMetrics metrics = new PrometheusMetrics();

    @Benchmark
    public void testIncrement(Blackhole blackhole) {
        int count = 1;
        metrics.counter("counter").inc(count);
        blackhole.consume(count);  // Try to ensure we don't get optimised away
    }

    @Benchmark
    public void testTiming(Blackhole blackhole) throws IOException {
        try (Closeable ctxt = metrics.timer("Timer").time()) {
            blackhole.consume(ctxt);  // Ensure we don't get optimised away
        }
    }
}
