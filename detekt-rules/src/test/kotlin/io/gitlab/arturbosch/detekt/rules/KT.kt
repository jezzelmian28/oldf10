package io.gitlab.arturbosch.detekt.rules

import io.gitlab.arturbosch.detekt.test.resource
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Artur Bosch
 */
enum class Case(val file: String) {
	CasesFolder("/cases"),
	DataClassContainsFunctions("/cases/DataClassContainsFunctions.kt"),
	Default("/cases/Default.kt"),
	Empty("/cases/Empty.kt"),
	EmptyIfPositive("/cases/EmptyIfPositive.kt"),
	EmptyIfNegative("/cases/EmptyIfNegative.kt"),
	EmptyDefaultConstructor("/cases/EmptyDefaultConstructor.kt"),
	EqualsAlwaysReturnsConstant("/cases/EqualsAlwaysReturnsConstant.kt"),
	Exceptions("/cases/Exceptions.kt"),
	ExceptionRaisedInMethods("/cases/ExceptionRaisedInMethods.kt"),
	FinalClass("/cases/FinalClass.kt"),
	InvalidLoopCondition("/cases/InvalidLoopCondition.kt"),
	IteratorImpl("/cases/IteratorImpl.kt"),
	LoopWithTooManyJumpStatements("cases/LoopWithTooManyJumpStatements.kt"),
	LabeledExpression("/cases/LabeledExpression.kt"),
	NamingConventions("/cases/NamingConventions.kt"),
	NewLineAtEndOfFile("/cases/NewLineAtEndOfFile.kt"),
	MaxLineLength("/cases/MaxLineLength.kt"),
	ModifierOrder("/cases/ModifierOrder.kt"),
	ComplexClass("/cases/ComplexClass.kt"),
	Comments("/cases/Comments.kt"),
	NestedClasses("/cases/NestedClasses.kt"),
	NestedLongMethods("/cases/NestedLongMethods.kt"),
	FeatureEnvy("/cases/FeatureEnvy.kt"),
	UnreachableCode("/cases/UnreachableCode.kt"),
	UnnecessaryAbstractClass("/cases/UnnecessaryAbstractClass.kt"),
	SuppressedElements("/SuppressedByElementAnnotation.kt"),
	SuppressedElementsByFile("/SuppressedElementsByFileAnnotation.kt"),
	SuppressedElementsByClass("/SuppressedElementsByClassAnnotation.kt"),
	SwallowedException("/cases/SwallowedException.kt"),
	UnconditionalJumpStatementInLoop("/cases/UnconditionalJumpStatementInLoop.kt"),
	TooManyFunctions("/cases/TooManyFunctions.kt"),
	TooManyFunctionsTopLevel("/cases/TooManyFunctionsTopLevel.kt"),
	UseDataClass("/cases/UseDataClass.kt");

	fun path(): Path = Paths.get(resource(file))
}
