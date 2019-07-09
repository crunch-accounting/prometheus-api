# A simplified Prometheus API for Java

[![Build Status](https://travis-ci.org/crunch-accounting/prometheus-api.svg?branch=master)](https://travis-ci.org/crunch-accounting/prometheus-api) [![codecov](https://codecov.io/gh/crunch-accounting/prometheus-api/branch/master/graph/badge.svg)](https://codecov.io/gh/crunch-accounting/prometheus-api)

* API geared aimed at application developers rather than Prometheus experts.
* Name-based metrics; no need to create instances or handle registration.
* Cleaner timer / summary syntax, via resource/try
* Aim is to simplify Prometheus adoption, reduce excessive code intrusion.
* Lexical compatibility with Codahale/Dropwizard Metrics API, simplifying complete migration.
* Regular Prometheus API can always be used directly for more advanced cases (unclear what those might be).

## Example:

```kotlin
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
        metrics.timer("Sessions.handleLogin").time().use { return "Login handled!" }
    }
}
```

## Features:

#### Application prefix:

Configure a common prefix...

```kotlin
@Value("${spring.application.name}") String prefixToUse // "MyApp"

@Bean
fun prometheusMetrics(collector: CollectorRegistry) : PrometheusMetrics {
    return PrometheusMetrics(collector, prefixToUse)
}
```

All created metrics will use that (normalised) prefix:

```kotlin
metrics.counter("counter_1").inc()
expectThat(samplesString(registry)).startsWith("[Name: myapp_counter_1 Type: COUNTER ")
```

---

#### Helpful descriptions:

Defaulted if not set:

```kotlin
metrics.timer("transaction")  // ==> "transaction"

metrics.timer("transaction", "Transaction time")  // ==> "Transaction time"
```

Alternatively, load a Java `Properties` file like:

```
transaction = Transaction time
```

when you create your PrometheusMetrics object, e.g.

```kotlin
val props = Properties()
File("descriptions.properties").reader(Charsets.UTF_8).use { props.load(it) }

metrics.setDescriptionMappings(props)
```

Now you can use the simpler API:

```kotlin
metrics.timer("transaction")  // ==> "Transaction time"
```

---

#### More helpful `summary` quantiles:

By default, summaries will get the following percentiles, rather than a simple median:

50%, 75%, 90%, 95%, 99%, 99.9%

---

#### Error counts implemented via labels:

```kotlin
metrics.error("salesforce")

assertThat(registry.getSampleValue("myapp_errors", \
                                    arrayOf("error_type"), \
                                    arrayOf("salesforce")).isEqualTo(1.0d)
// ...

metrics.error("transaction")

expectThat(registry.getSampleValue("myapp_errors", \
                                    arrayOf("error_type"), \
                                    arrayOf("transaction")).isEqualTo(1.0d)
```

---

#### All names sanitised to ensure no invalid characters

* All names lowercased
* `.`, `-`, `#`, ` ` seamlessly mapped to `_`
