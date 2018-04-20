package io.gitlab.arturbosch.detekt.formatting.wrappers

import com.github.shyiko.ktlint.ruleset.standard.NoTrailingSpacesRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.formatting.FormattingRule

/**
 * See https://ktlint.github.io for documentation.
 *
 * @author Artur Bosch
 */
class NoTrailingSpaces(config: Config) : FormattingRule(config) {

	override val wrapping = NoTrailingSpacesRule()
	override val issue = issueFor("Detects trailing spaces")
}
