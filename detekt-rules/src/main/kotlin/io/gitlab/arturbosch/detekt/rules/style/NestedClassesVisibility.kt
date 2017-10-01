package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.rules.isInternal
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

class NestedClassesVisibility(config: Config = Config.empty) : Rule(config) {
	override val issue: Issue = Issue("NestedClassesVisibility",
			severity = Severity.Security,
			description = "Nested types are often used for implementing private functionality. " +
					"Therefore, they shouldn't be externally visible.")

	private val visitor = VisibilityVisitor()

	override fun visitClass(klass: KtClass) {
		super.visitClass(klass)
		if (klass.isInternal()) {
			klass.declarations.forEach {
				it.accept(visitor)
			}
		}
	}

	private fun handleClass(klass: KtClass) {
		report(CodeSmell(issue, Entity.from(klass)))
	}

	private inner class VisibilityVisitor : DetektVisitor() {
		override fun visitClass(klass: KtClass) {
			if (!klass.isInternal() && !klass.isPrivate()) {
				handleClass(klass)
			}
		}
	}
}
