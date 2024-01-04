package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.ActiveByDefault
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.config
import io.gitlab.arturbosch.detekt.api.internal.Configuration
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * Wildcard imports should be replaced with imports using fully qualified class names. This helps increase clarity of
 * which classes are imported and helps prevent naming conflicts.
 *
 * Library updates can introduce naming clashes with your own classes which might result in compilation errors.
 *
 * **NOTE**: This rule has a twin implementation NoWildcardImports in the formatting rule set (a wrapped KtLint rule).
 * When suppressing an issue of WildcardImport in the baseline file, make sure to suppress the corresponding NoWildcardImports issue.
 *
 * <noncompliant>
 * import io.gitlab.arturbosch.detekt.*
 *
 * class DetektElements {
 *     val element1 = DetektElement1()
 *     val element2 = DetektElement2()
 * }
 * </noncompliant>
 *
 * <compliant>
 * import io.gitlab.arturbosch.detekt.DetektElement1
 * import io.gitlab.arturbosch.detekt.DetektElement2
 *
 * class DetektElements {
 *     val element1 = DetektElement1()
 *     val element2 = DetektElement2()
 * }
 * </compliant>
 */
@ActiveByDefault(since = "1.0.0")
class WildcardImport(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        "Wildcard imports should be replaced with imports using fully qualified class names. " +
            "Wildcard imports can lead to naming conflicts. " +
            "A library update can introduce naming clashes with your classes which " +
            "results in compilation errors.",
    )

    @Configuration("Define a list of package names that should be allowed to be imported with wildcard imports.")
    private val excludeImports: List<String> by config(listOf("java.util.*")) { imports ->
        imports.map { it.removePrefix("*").removeSuffix("*") }
    }

    override fun visitImportDirective(importDirective: KtImportDirective) {
        val import = importDirective.importPath?.pathStr
        if (import != null) {
            if (!import.contains("*")) {
                return
            }

            if (excludeImports.any { import.contains(it, ignoreCase = true) }) {
                return
            }
            report(
                CodeSmell(
                    issue,
                    Entity.from(importDirective),
                    "$import " +
                        "is a wildcard import. Replace it with fully qualified imports."
                )
            )
        }
    }
}
