package uk.co.crunch

import io.prometheus.client.CollectorRegistry

object TestUtils {
    @JvmStatic
    fun samplesString(registry: CollectorRegistry): String {
        return registry.metricFamilySamples().toList().toString()
    }
}
