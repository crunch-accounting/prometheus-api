package uk.co.crunch.impl.v2x

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.co.crunch.impl.v2x.AlertRulesGenerator2x.titlecase

class AlertRulesGenerator2xTest {

    @Test
    fun testTitleCase() {
        assertThat(titlecase("")).isEqualTo("")
        assertThat(titlecase("audit-service")).isEqualTo("AuditService")
        assertThat(titlecase("audit_service")).isEqualTo("AuditService")
        assertThat(titlecase("audit.service")).isEqualTo("AuditService")
    }
}