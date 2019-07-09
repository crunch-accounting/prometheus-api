package uk.co.crunch

import io.prometheus.client.CollectorRegistry

object TestUtils {
    fun samplesString(registry: CollectorRegistry) = registry.metricFamilySamples().toList().toString()
}
