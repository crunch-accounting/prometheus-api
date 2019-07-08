package uk.co.crunch.impl.v2x;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.crunch.impl.v2x.AlertRulesGenerator2x.titlecase;

public class AlertRulesGenerator2xTest {

    @Test
    public void testTitleCase() {
        assertThat(titlecase("")).isEqualTo("");
        assertThat(titlecase("audit-service")).isEqualTo("AuditService");
        assertThat(titlecase("audit_service")).isEqualTo("AuditService");
        assertThat(titlecase("audit.service")).isEqualTo("AuditService");
    }
}