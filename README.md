# A simplified Prometheus API for Java

[![Build Status](https://travis-ci.org/crunch-accounting/prometheus-api.svg?branch=master)](https://travis-ci.org/crunch-accounting/prometheus-api) [![codecov](https://codecov.io/gh/crunch-accounting/prometheus-api/branch/master/graph/badge.svg)](https://codecov.io/gh/crunch-accounting/prometheus-api)

* API geared aimed at application developers rather than Prometheus experts.
* Name-based metrics; no need to create instances or handle registration.
* Cleaner timer / summary syntax, via resource/try
* Aim is to simplify Prometheus adoption, reduce excessive code intrusion.
* Lexical compatibility with Codahale/Dropwizard Metrics API, simplifying complete migration.
* Regular Prometheus API can always be used directly for more advanced cases (unclear what those might be).

## Example:

```java
private final PrometheusMetrics metrics;

public void onUserLogin(Object event) {
    metrics.gauge("Sessions.open").inc();
    metrics.counter("Sessions.total").inc();
}

public String handleLogin() {
    try (Context timer = metrics.summary("Sessions.handleLogin").time()) {
        return "Login handled!";
    }
}
```

## Features:

#### Application prefix:

Configure a common prefix...

```java
@Value("${spring.application.name}")  // "MyApp"
private String prefixToUse;

@Bean
public PrometheusMetrics prometheusMetrics(CollectorRegistry collector) {
    return new PrometheusMetrics(collector, prefixToUse);
}
```

All created metrics will use that (normalised) prefix:

```java
metrics.counter("counter_1").inc();
assertThat(samplesString(registry)).startsWith("[Name: myapp_counter_1 Type: COUNTER ");
```

---

#### Helpful descriptions:

Defaulted if not set:

```java
metrics.timer("transaction");  // ==> "transaction"

metrics.timer("transaction", "Transaction time");  // ==> "Transaction time"
```

Alternatively, load a Java `Properties` file like:

```
transaction = Transaction time
```

when you create your PrometheusMetrics object, e.g.

```java
final Properties props = new Properties();
try (Reader r = Files.newReader( new File("descriptions.properties"), Charsets.UTF_8)) {
    props.load(r);
}

metrics.setDescriptionMappings(props);
```

Now you can use the simpler API:

```java
metrics.timer("transaction");  // ==> "Transaction time"
```

---

#### More helpful `summary` quantiles:

By default, summaries will get the following percentiles, rather than a simple median:

50%, 75%, 90%, 95%, 99%, 99.9%

---

#### Error counts implemented via labels:

```java
metrics.error("salesforce");

assertThat(registry.getSampleValue("myapp_errors", \
                                    new String[]{"error_type"}, \
                                    new String[]{"salesforce"})).isEqualTo(1.0d);
// ...

metrics.error("transaction");

assertThat(registry.getSampleValue("myapp_errors", \
                                    new String[]{"error_type"}, \
                                    new String[]{"transaction"})).isEqualTo(1.0d);
```

---

#### All names sanitised to ensure no invalid characters

* All names lowercased
* `.`, `-`, `#`, ` ` seamlessly mapped to `_`
