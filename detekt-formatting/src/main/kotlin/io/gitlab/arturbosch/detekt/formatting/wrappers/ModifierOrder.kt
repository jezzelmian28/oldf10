package io.gitlab.arturbosch.detekt.formatting.wrappers

import com.github.shyiko.ktlint.ruleset.standard.ModifierOrderRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.formatting.FormattingRule

/**
 * See https://ktlint.github.io for documentation.
 *
 * @author Artur Bosch
 */
class ModifierOrder(config: Config) : FormattingRule(config) {

	override val wrapping = ModifierOrderRule()
	override val issue = issueFor("Detects modifiers in non default order")
}
