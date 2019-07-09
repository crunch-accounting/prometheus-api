package io.prometheus.client;

@SuppressWarnings("unused")
public class TestableTimeProvider extends SimpleTimer.TimeProvider {

    private long baseTime = System.currentTimeMillis();

    public static void install() {
        SimpleTimer.defaultTimeProvider = new TestableTimeProvider();
    }

    @Override
    long nanoTime() {
        final long curr = baseTime;
        baseTime += 1979L;
        return curr;
    }
}
