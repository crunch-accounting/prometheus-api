package uk.co.crunch

import io.prometheus.client.CollectorRegistry

object TestUtils {

    fun samplesString(registry: CollectorRegistry): String {
        return registry.metricFamilySamples().toList().toString()
    }
}
