package uk.co.crunch.impl.v2x

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Splitter
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.introspector.BeanAccess
import org.yaml.snakeyaml.introspector.Property
import org.yaml.snakeyaml.introspector.PropertyUtils
import org.yaml.snakeyaml.representer.Representer
import uk.co.crunch.api.AlertRule
import uk.co.crunch.impl.AlertRulesGenerator.getAnnotations
import uk.co.crunch.impl.AlertRulesGenerator.getLabels
import uk.co.crunch.impl.AlertRulesGenerator.replaceRulePlaceholders
import uk.co.crunch.utils.PrometheusUtils
import java.util.*

object AlertRulesGenerator2x {

    private val WORDS = Splitter.onPattern("[-_\\.]")

    private val yaml: Yaml
        get() {
            val repr = Representer()
            repr.propertyUtils = UnsortedPropertyUtils()

            val dumper = DumperOptions()
            dumper.splitLines = false

            return Yaml(Constructor(), repr, dumper)
        }

    fun buildRulesFile(metricPrefix: String, alertGroupName: String, vararg rules: AlertRule): String {
        val normalisedPrefix = PrometheusUtils.normaliseName(metricPrefix) + "_"

        val group = AlertRulesGroup(alertGroupName)

        for (eachRule in rules) {
            val alertName = titlecase(metricPrefix) + titlecase(eachRule.name)

            group.addRule(AlertRulePojo(alertName, replaceRulePlaceholders(eachRule, normalisedPrefix), eachRule.duration, getLabels(eachRule), getAnnotations(eachRule)))
        }

        return yaml.dumpAsMap(AlertRulesPojo(group))
    }

    @VisibleForTesting
    internal fun titlecase(s: String): String {
        if (s.isEmpty()) {
            return ""
        }
        val sb = StringBuilder(s.length)
        for (word in WORDS.splitToList(s)) {
            sb.append(Character.toTitleCase(word[0])).append(word.substring(1))
        }
        return sb.toString()
    }

    // https://bitbucket.org/asomov/snakeyaml/src/tip/src/test/java/org/yaml/snakeyaml/issues/issue60/CustomOrderTest.java?fileviewer=file-view-default
    private class UnsortedPropertyUtils : PropertyUtils() {
        override fun createPropertySet(type: Class<out Any>, bAccess: BeanAccess): Set<Property> {
            return LinkedHashSet(getPropertiesMap(type, BeanAccess.FIELD).values)
        }
    }

    private class AlertRulesPojo(group: AlertRulesGroup) {
        private val groups = ArrayList<AlertRulesGroup>()

        init {
            groups.add(group)
        }
    }

    private class AlertRulesGroup internal constructor(private val name: String) {
        private val rules = ArrayList<AlertRulePojo>()

        internal fun addRule(rule: AlertRulePojo) {
            this.rules.add(rule)
        }
    }

    private class AlertRulePojo internal constructor(alert: String, internal var expr: String, internal var duration: String, internal var labels: Map<String, String>, internal var annotations: Map<String, String>) {
        internal var alert = "OpenstackCinderVolumeStuck"

        init {
            this.alert = alert
        }
    }
}
