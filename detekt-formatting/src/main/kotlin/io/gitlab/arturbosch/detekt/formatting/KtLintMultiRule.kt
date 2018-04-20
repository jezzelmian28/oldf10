package io.gitlab.arturbosch.detekt.formatting

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.MultiRule
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.formatting.wrappers.ChainWrapping
import io.gitlab.arturbosch.detekt.formatting.wrappers.FinalNewline
import io.gitlab.arturbosch.detekt.formatting.wrappers.ImportOrdering
import io.gitlab.arturbosch.detekt.formatting.wrappers.Indentation
import io.gitlab.arturbosch.detekt.formatting.wrappers.MaxLineLength
import io.gitlab.arturbosch.detekt.formatting.wrappers.ModifierOrder
import io.gitlab.arturbosch.detekt.formatting.wrappers.NoBlankLineBeforeRbrace
import io.gitlab.arturbosch.detekt.formatting.wrappers.NoConsecutiveBlankLines
import io.gitlab.arturbosch.detekt.formatting.wrappers.NoEmptyClassBody
import io.gitlab.arturbosch.detekt.formatting.wrappers.NoItParamInMultilineLambda
import io.gitlab.arturbosch.detekt.formatting.wrappers.NoLineBreakAfterElse
import io.gitlab.arturbosch.detekt.formatting.wrappers.NoLineBreakBeforeAssignment
import io.gitlab.arturbosch.detekt.formatting.wrappers.NoMultipleSpaces
import io.gitlab.arturbosch.detekt.formatting.wrappers.NoSemicolons
import io.gitlab.arturbosch.detekt.formatting.wrappers.NoTrailingSpaces
import io.gitlab.arturbosch.detekt.formatting.wrappers.NoUnitReturn
import io.gitlab.arturbosch.detekt.formatting.wrappers.NoUnusedImports
import io.gitlab.arturbosch.detekt.formatting.wrappers.NoWildcardImports
import io.gitlab.arturbosch.detekt.formatting.wrappers.ParameterListWrapping
import io.gitlab.arturbosch.detekt.formatting.wrappers.SpacingAroundColon
import io.gitlab.arturbosch.detekt.formatting.wrappers.SpacingAroundComma
import io.gitlab.arturbosch.detekt.formatting.wrappers.SpacingAroundCurly
import io.gitlab.arturbosch.detekt.formatting.wrappers.SpacingAroundKeyword
import io.gitlab.arturbosch.detekt.formatting.wrappers.SpacingAroundOperators
import io.gitlab.arturbosch.detekt.formatting.wrappers.SpacingAroundRangeOperator
import io.gitlab.arturbosch.detekt.formatting.wrappers.StringTemplate
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFile

/**
 * Runs all KtLint rules.
 *
 * @author Artur Bosch
 */
class KtLintMultiRule(config: Config = Config.empty) : MultiRule() {

	override val rules: List<Rule> = listOf(
			ChainWrapping(config),
			FinalNewline(config),
			ImportOrdering(config),
			Indentation(config),
			MaxLineLength(config),
			ModifierOrder(config),
			NoBlankLineBeforeRbrace(config),
			NoConsecutiveBlankLines(config),
			NoEmptyClassBody(config),
			NoItParamInMultilineLambda(config),
			NoLineBreakAfterElse(config),
			NoLineBreakBeforeAssignment(config),
			NoMultipleSpaces(config),
			NoSemicolons(config),
			NoTrailingSpaces(config),
			NoUnitReturn(config),
			NoUnusedImports(config),
			NoWildcardImports(config),
			ParameterListWrapping(config),
			SpacingAroundColon(config),
			SpacingAroundComma(config),
			SpacingAroundCurly(config),
			SpacingAroundKeyword(config),
			SpacingAroundOperators(config),
			SpacingAroundRangeOperator(config),
			StringTemplate(config)
	).sortedBy {
		it is com.github.shyiko.ktlint.core.Rule.Modifier.Last
				|| it is com.github.shyiko.ktlint.core.Rule.Modifier.RestrictToRootLast
	}.reversed()

	override fun visit(root: KtFile) {
		activeRules.forEach { it.visit(root) }
		root.node.visitTokens { node ->
			activeRules.forEach { rule ->
				println(rule.id)
				(rule as? FormattingRule)?.runIfActive { this.apply(node) }
			}
		}
	}

	private fun ASTNode.visitTokens(currentNode: (node: ASTNode) -> Unit) {
		currentNode(this)
		getChildren(null).forEach { it.visitTokens(currentNode) }
	}
}
