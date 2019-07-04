package uk.co.crunch;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Test;
import uk.co.crunch.api.AlertRule;
import uk.co.crunch.api.AlertRule.Annotation;
import uk.co.crunch.api.AlertRule.Label;
import uk.co.crunch.api.AlertRule.Severity;
import uk.co.crunch.api.AlertRules;
import uk.co.crunch.api.PrometheusVersion;
import uk.co.crunch.impl.AlertRulesGenerator;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@AlertRules(groupName="webapp.alerts", value={
    @AlertRule(name = "NginxIsDroppingConnections_Full",
        metricNames = "nginx_dropped_connections",
        rule = "increase($1[1m]) > 0",
        duration = "2m",
        severity = Severity.WARNING,
        labels = {@Label(name = "extra", value = "blah")},
        summary = "NGINX node {{ $labels.instance }} is dropping connections",
        description = "NGINX node \"{{ $labels.instance }}\" is dropping connections. This is normally due to running out of resources.",
        confluenceLink = "https://crunch.atlassian.net/wiki/spaces/PLAT/pages/199229454/NGINX+dropping+connections",
        annotations = {@Annotation(name = "foo", value = "bar")}
    ),
    @AlertRule(name = "RequestsPerSecondIncrease_Minimal",
        metricNames = "requests per second",
        rule = "avg_over_time($1[1m]) / avg_over_time($1[24h]) * 100 > 150",
        duration = "5m",
        summary = "NGINX node {{ $labels.instance }} request rate has increased dramatically",
        description = "NGINX node {{ $labels.instance }} has an abnormal increase in request rate. This could either indicate a traffic spike/DDoS attempt, or a misbehaving upstream service",
        confluenceLink = "/PLAT/pages/199294994/NGINX+request+rate"  // relative paths will be completed by the annotation processor
    )
})
public class AlertRulesTest {

    @Test
    public void testGenerationV1X() throws IOException {
        final AlertRules rules = this.getClass().getAnnotation(AlertRules.class);

        assertThat( AlertRulesGenerator.INSTANCE.buildRulesFile(PrometheusVersion.V1_X, "Test", "???", rules.value()).trim() )
                .isEqualTo( Files.asCharSource(new File("src/test/resources/expectations/generated_rules.rule"), Charsets.UTF_8).read().trim() );
    }

    @Test
    public void testGenerationV2X() throws IOException {
        final AlertRules rules = this.getClass().getAnnotation(AlertRules.class);

        assertThat( AlertRulesGenerator.INSTANCE.buildRulesFile(PrometheusVersion.V2_X, "Test", rules.groupName(), rules.value()).trim() )
                .isEqualTo( Files.asCharSource(new File("src/test/resources/expectations/generated_rules.yml"), Charsets.UTF_8).read().trim() );
    }

    @AlertRule(name = "rpsRule",
            metricNames = "rps",
            rule = "avg_over_time($1[1m]) / avg_over_time($1[24h]) * 100 > 200",
            duration = "12h",
            summary = "Summary",
            description = "Desc",
            confluenceLink = "/PLAT/pages/1976"
    )
    @Test
    public void testExtra() throws IOException, NoSuchMethodException {
        final AlertRule thisRule = this.getClass().getMethod("testExtra").getAnnotation(AlertRule.class);

        assertThat( AlertRulesGenerator.INSTANCE.buildRulesFile(PrometheusVersion.V2_X, "audit-service", "Untilted", thisRule).trim() )
                .isEqualTo( Files.asCharSource(new File("src/test/resources/expectations/individual_rule.yml"), Charsets.UTF_8).read().trim() );
    }
}
