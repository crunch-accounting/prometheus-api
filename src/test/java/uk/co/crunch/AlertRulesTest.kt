package uk.co.crunch

import com.google.common.base.Charsets
import com.google.common.io.Files
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.co.crunch.api.AlertRule
import uk.co.crunch.api.AlertRule.*
import uk.co.crunch.api.AlertRule.Annotation
import uk.co.crunch.api.AlertRules
import uk.co.crunch.api.PrometheusVersion
import uk.co.crunch.impl.AlertRulesGenerator
import java.io.File
import java.io.IOException

@AlertRules(groupName = "webapp.alerts", value = [AlertRule(name = "NginxIsDroppingConnections_Full", metricNames = ["nginx_dropped_connections"], rule = "increase($1[1m]) > 0", duration = "2m", severity = Severity.WARNING, labels = [Label(name = "extra", value = "blah")], summary = "NGINX node {{ \$labels.instance }} is dropping connections", description = "NGINX node \"{{ \$labels.instance }}\" is dropping connections. This is normally due to running out of resources.", confluenceLink = "https://crunch.atlassian.net/wiki/spaces/PLAT/pages/199229454/NGINX+dropping+connections", annotations = [Annotation(name = "foo", value = "bar")]), AlertRule(name = "RequestsPerSecondIncrease_Minimal", metricNames = ["requests per second"], rule = "avg_over_time($1[1m]) / avg_over_time($1[24h]) * 100 > 150", duration = "5m", summary = "NGINX node {{ \$labels.instance }} request rate has increased dramatically", description = "NGINX node {{ \$labels.instance }} has an abnormal increase in request rate. This could either indicate a traffic spike/DDoS attempt, or a misbehaving upstream service", confluenceLink = "/PLAT/pages/199294994/NGINX+request+rate")// relative paths will be completed by the annotation processor
])
class AlertRulesTest {

    @Test
    @Throws(IOException::class)
    fun testGenerationV1X() {
        val rules = this.javaClass.getAnnotation(AlertRules::class.java)

        assertThat(AlertRulesGenerator.buildRulesFile(PrometheusVersion.V1_X, "Test", "???", *rules.value).trim { it <= ' ' })
                .isEqualTo(Files.asCharSource(File("src/test/resources/expectations/generated_rules.rule"), Charsets.UTF_8).read().trim { it <= ' ' })
    }

    @Test
    @Throws(IOException::class)
    fun testGenerationV2X() {
        val rules = this.javaClass.getAnnotation(AlertRules::class.java)

        assertThat(AlertRulesGenerator.buildRulesFile(PrometheusVersion.V2_X, "Test", rules.groupName, *rules.value).trim { it <= ' ' })
                .isEqualTo(Files.asCharSource(File("src/test/resources/expectations/generated_rules.yml"), Charsets.UTF_8).read().trim { it <= ' ' })
    }

    @AlertRule(name = "rpsRule", metricNames = ["rps"], rule = "avg_over_time($1[1m]) / avg_over_time($1[24h]) * 100 > 200", duration = "12h", summary = "Summary", description = "Desc", confluenceLink = "/PLAT/pages/1976")
    @Test
    @Throws(IOException::class, NoSuchMethodException::class)
    fun testExtra() {
        val thisRule = this.javaClass.getMethod("testExtra").getAnnotation(AlertRule::class.java)

        assertThat(AlertRulesGenerator.buildRulesFile(PrometheusVersion.V2_X, "audit-service", "Untilted", thisRule).trim { it <= ' ' })
                .isEqualTo(Files.asCharSource(File("src/test/resources/expectations/individual_rule.yml"), Charsets.UTF_8).read().trim { it <= ' ' })
    }
}
