package uk.co.crunch

import uk.co.crunch.api.PrometheusMetrics

class Example(private val metrics: PrometheusMetrics) {

    fun onUserLogin(event: Any) {
        metrics.gauge("Sessions.open").inc()
    }

    fun onUserLogout(event: Any) {
        metrics.gauge("Sessions.open").dec()
    }

    fun onError(event: Any) {
        metrics.error("generic", "Generic errors")
    }

    fun handleLogin(): String {
        metrics.timed("Sessions.handleLogin", "Login times").use { return "Login handled!" }
    }
}
