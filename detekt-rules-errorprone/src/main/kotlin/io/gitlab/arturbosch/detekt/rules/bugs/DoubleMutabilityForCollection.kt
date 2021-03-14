package io.gitlab.arturbosch.detekt.rules.bugs

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.BindingContext

/**
 * Using `var` when declaring a mutable collection leads to double mutability. Consider instead
 * declaring your variable with `val` or switching your declaration to use an immutable type.
 *
 * <noncompliant>
 * var myList = mutableListOf(1,2,3)
 * var mySet = mutableSetOf(1,2,3)
 * var myMap = mutableMapOf("answer" to 42)
 * </noncompliant>
 *
 * <compliant>
 * // Use val
 * val myList = mutableListOf(1,2,3)
 * val mySet = mutableSetOf(1,2,3)
 * val myMap = mutableMapOf("answer" to 42)
 *
 * // Use immutable types
 * var myList = listOf(1,2,3)
 * var mySet = setOf(1,2,3)
 * var myMap = mapOf("answer" to 42)
 * </compliant>
 *
 * @requiresTypeResolution
 */
class DoubleMutabilityForCollection(config: Config = Config.empty) : Rule(config) {

    override val issue: Issue = Issue(
        "DoubleMutabilityInCollection",
        Severity.CodeSmell,
        "Using var with mutable collections leads to double mutability. " +
                "Consider using val or immutable collection types.",
        Debt.FIVE_MINS
    )

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)

        if (bindingContext == BindingContext.EMPTY) return

        val type =
            (bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, property] as? VariableDescriptor)?.type ?: return
        val standardType = type.getJetTypeFqName(false)
        if (property.isVar && standardType in mutableTypes) {
            report(CodeSmell(
                issue,
                Entity.from(property),
                "Property ${property.name} is declared as `var` with a mutable type $standardType. " +
                        "Consider using `val` or an immutable collection type"
            ))
        }
    }

    companion object {
        val mutableTypes = setOf(
            "kotlin.collections.MutableList",
            "kotlin.collections.MutableMap",
            "kotlin.collections.MutableSet",
            "java.util.ArrayList",
            "java.util.LinkedHashSet",
            "java.util.HashSet",
            "java.util.LinkedHashMap",
            "java.util.HashMap",
        )
    }
}
