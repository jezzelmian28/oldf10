package io.gitlab.arturbosch.detekt.rules.empty

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.isOpen
import io.gitlab.arturbosch.detekt.rules.isOverride
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Reports empty functions. Empty blocks of code serve no purpose and should be removed.
 * This rule will report all the functions with an empty body, with an exception: overriding functions with a
 * comment in the body (e.g. a `// no-op` comment).
 *
 * To ignore all the override functions, please use the [ignoreOverriddenFunctions] configuration field.
 *
 * @configuration ignoreOverriddenFunctions - excludes overridden functions with an empty body (default: `false`)
 *
 * @active since v1.0.0
 * @author Artur Bosch
 * @author Marvin Ramin
 * @author schalkms
 */
class EmptyFunctionBlock(config: Config) : EmptyRule(config) {

    private val ignoreOverriddenFunctions = valueOrDefault(IGNORE_OVERRIDDEN_FUNCTIONS, false)

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (function.isOpen()) {
            return
        }
        val bodyExpression = function.bodyExpression
        if (!ignoreOverriddenFunctions) {
            if (function.isOverride()) {
                // If this function is an override, let's ignore empty bodies with comments.
                bodyExpression?.addFindingIfBlockExprIsEmptyAndNotCommented()
            } else {
                bodyExpression?.addFindingIfBlockExprIsEmpty()
            }
        } else if (!function.isOverride()) {
            bodyExpression?.addFindingIfBlockExprIsEmpty()
        }
    }

    companion object {
        const val IGNORE_OVERRIDDEN_FUNCTIONS = "ignoreOverriddenFunctions"
    }
}
