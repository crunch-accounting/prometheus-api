package uk.co.crunch.impl

import uk.co.crunch.api.AlertRule
import uk.co.crunch.api.PrometheusVersion
import uk.co.crunch.impl.v1x.AlertRulesGenerator1x
import uk.co.crunch.impl.v2x.AlertRulesGenerator2x
import uk.co.crunch.utils.PrometheusUtils

object AlertRulesGenerator {

    fun buildRulesFile(version: PrometheusVersion, metricPrefix: String, alertGroupName: String, vararg rules: AlertRule): String {
        return if (version == PrometheusVersion.V2_X) AlertRulesGenerator2x.buildRulesFile(metricPrefix, alertGroupName, *rules) else AlertRulesGenerator1x.buildRulesFile(metricPrefix, *rules)
    }

    fun replaceRulePlaceholders(rule: AlertRule, normalisedPrefix: String): String {
        var ruleStr = rule.rule
        for (i in 0 until rule.metricNames.size) {
            val rawName = rule.metricNames[i]
            val missingPrefix = if (rawName.startsWith(normalisedPrefix)) "" else normalisedPrefix

            ruleStr = ruleStr.replace("$" + (i + 1), missingPrefix + PrometheusUtils.normaliseName(rawName))
        }
        return ruleStr
    }

    fun getLabels(rule: AlertRule): Map<String, String> {
        val labels = linkedMapOf("severity" to rule.severity.toString().toLowerCase())

        for (label in rule.labels) {
            labels.putIfAbsent(label.name, label.value)
        }

        return labels
    }

    fun getAnnotations(rule: AlertRule): Map<String, String> {
        val anns = linkedMapOf("summary" to rule.summary, "description" to rule.description)

        if (rule.confluenceLink.startsWith("/")) {
            anns["confluence_link"] = "https://crunch.atlassian.net/wiki/spaces" + rule.confluenceLink
        } else {
            anns["confluence_link"] = rule.confluenceLink
        }

        for (ann in rule.annotations) {
            anns.putIfAbsent(ann.name, ann.value)
        }

        return anns
    }
}
