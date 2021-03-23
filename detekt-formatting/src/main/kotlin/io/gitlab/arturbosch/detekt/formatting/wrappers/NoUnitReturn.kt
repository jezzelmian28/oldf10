package io.gitlab.arturbosch.detekt.formatting.wrappers

import com.pinterest.ktlint.ruleset.standard.NoUnitReturnRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.internal.ActiveByDefault
import io.gitlab.arturbosch.detekt.formatting.FormattingRule

/**
 * See <a href="https://ktlint.github.io/#rule-unit-return">ktlint-website</a> for documentation.
 *
 * @autoCorrect since v1.0.0
 */
@ActiveByDefault("1.0.0")
class NoUnitReturn(config: Config) : FormattingRule(config) {

    override val wrapping = NoUnitReturnRule()
    override val issue = issueFor("Detects optional 'Unit' return types")
}
