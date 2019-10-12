package io.gitlab.arturbosch.detekt.rules.naming

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.LazyRegex
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.rules.identifierName
import io.gitlab.arturbosch.detekt.rules.isConstant
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

/**
 * Reports when property names inside objects which do not follow the specified naming convention are used.
 *
 * @configuration constantPattern - naming pattern (default: `'[a-z][A-Za-z0-9]*|[A-Z][_A-Z0-9]*'`)
 * @configuration propertyPattern - naming pattern (default: `'[a-z][A-Za-z0-9]*|[A-Z][_A-Z0-9]*'`)
 * @configuration privatePropertyPattern - naming pattern (default: `'_?[a-z][A-Za-z0-9]*|_?[A-Z][_A-Z0-9]*'`)
 * @active since v1.0.0
 */
class ObjectPropertyNaming(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(javaClass.simpleName,
            Severity.Style,
            "Property names inside objects should follow the naming convention set in the projects configuration.",
            debt = Debt.FIVE_MINS)

    private val constantPattern by LazyRegex(CONSTANT_PATTERN, "[a-z][A-Za-z0-9]*|[A-Z][_A-Z0-9]*")
    private val propertyPattern by LazyRegex(PROPERTY_PATTERN, "[a-z][A-Za-z0-9]*|[A-Z][_A-Z0-9]*")
    private val privatePropertyPattern by LazyRegex(PRIVATE_PROPERTY_PATTERN, "_?[a-z][A-Za-z0-9]*|_?[A-Z][_A-Z0-9]*")

    override fun visitProperty(property: KtProperty) {
        if (property.isLocal) {
            return
        }

        if (property.isConstant()) {
            handleConstant(property)
        } else {
            handleProperty(property)
        }
    }

    private fun handleConstant(property: KtProperty) {
        if (!property.identifierName().matches(constantPattern)) {
            report(CodeSmell(
                    issue,
                    Entity.from(property),
                    message = "Object constant names should match the pattern: $constantPattern"))
        }
    }

    private fun handleProperty(property: KtProperty) {
        if (property.isPrivate()) {
            if (!property.identifierName().matches(privatePropertyPattern)) {
                report(CodeSmell(
                        issue,
                        Entity.from(property),
                        message = "Private object property names should match the pattern: $privatePropertyPattern"))
            }
        } else if (!property.identifierName().matches(propertyPattern)) {
            report(CodeSmell(
                    issue,
                    Entity.from(property),
                    message = "Object property names should match the pattern: $propertyPattern"))
        }
    }

    companion object {
        const val CONSTANT_PATTERN = "constantPattern"
        const val PROPERTY_PATTERN = "propertyPattern"
        const val PRIVATE_PROPERTY_PATTERN = "privatePropertyPattern"
    }
}
