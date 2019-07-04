package uk.co.crunch.utils

object PrometheusUtils {
    fun normaliseName(name: String): String {
        return name.replace('.', '_')
                .replace('-', '_')
                .replace('#', '_')
                .replace(' ', '_')
                .toLowerCase()
    }
}
