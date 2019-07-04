package uk.co.crunch.impl.v1x

import org.jtwig.JtwigModel
import org.jtwig.JtwigTemplate
import uk.co.crunch.api.AlertRule
import uk.co.crunch.impl.AlertRulesGenerator.getAnnotations
import uk.co.crunch.impl.AlertRulesGenerator.getLabels
import uk.co.crunch.impl.AlertRulesGenerator.replaceRulePlaceholders
import uk.co.crunch.utils.PrometheusUtils

object AlertRulesGenerator1x {

    fun buildRulesFile(metricPrefix: String, vararg rules: AlertRule): String {
        val model = JtwigModel.newModel()
        model.with("prefix", metricPrefix)

        val template = JtwigTemplate.classpathTemplate("templates/rule_template_1.x.rule")

        val ruleStrs = arrayListOf<String>()

        val normalisedPrefix = PrometheusUtils.normaliseName(metricPrefix) + "_"

        for (eachRule in rules) {
            model.with("alertName", eachRule.name)
            model.with("duration", eachRule.duration)
            model.with("rule", replaceRulePlaceholders(eachRule, normalisedPrefix))
            model.with("annotations", entriesMapToString(getAnnotations(eachRule)))
            model.with("labels", entriesMapToString(getLabels(eachRule)))

            ruleStrs.add(template.render(model))
        }

        return ruleStrs.joinToString("\n")
    }

    private fun quoteString(s: String): CharSequence {
        return StringBuilder().append("\"").append(s.replace("\"", "\\\"")).append("\"")
    }

    private fun entriesMapToString(entries: Map<String, String>): String {
        return entries.entries.map{ it.key + " = " + quoteString(it.value) }.joinToString(",\n    ")
    }
}
