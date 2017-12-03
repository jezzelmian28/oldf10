package io.gitlab.arturbosch.detekt.rules.empty

import io.gitlab.arturbosch.detekt.api.Config
import org.jetbrains.kotlin.psi.KtCatchClause

/**
 * @active since v1.0.0
 * @author Artur Bosch
 * @author Marvin Ramin
 */
class EmptyCatchBlock(config: Config) : EmptyRule(config = config) {

	override fun visitCatchSection(catchClause: KtCatchClause) {
		catchClause.catchBody?.addFindingIfBlockExprIsEmpty()
	}

}
