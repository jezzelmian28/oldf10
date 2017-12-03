package io.gitlab.arturbosch.detekt.rules.empty

import io.gitlab.arturbosch.detekt.api.Config
import org.jetbrains.kotlin.psi.KtDoWhileExpression

/**
 * @active since v1.0.0
 * @author Artur Bosch
 * @author Marvin Ramin
 */
class EmptyDoWhileBlock(config: Config) : EmptyRule(config) {

	override fun visitDoWhileExpression(expression: KtDoWhileExpression) {
		expression.body?.addFindingIfBlockExprIsEmpty()
	}

}
