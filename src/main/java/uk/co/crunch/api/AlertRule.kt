package uk.co.crunch.api

import kotlin.annotation.AnnotationRetention.RUNTIME

@Retention(RUNTIME)  // Only RUNTIME for testability
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class AlertRule(val name: String, val duration: String, val metricNames: Array<String>, val rule: String, // Labels...
                           val severity: Severity = Severity.PAGE, val labels: Array<Label> = [], // Annotations...
                           val summary: String, val description: String, val confluenceLink: String, val annotations: Array<Annotation> = []) {

    enum class Severity {
        PAGE, WARNING
    }

    annotation class Label(val value: String, val name: String)

    annotation class Annotation(val value: String, val name: String)
}
