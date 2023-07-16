package io.gitlab.arturbosch.detekt.invoke

import io.gitlab.arturbosch.detekt.extensions.DetektReportType
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import java.io.File

private const val DEBUG_PARAMETER = "--debug"
private const val INPUT_PARAMETER = "--input"
private const val CONFIG_PARAMETER = "--config"
private const val BASELINE_PARAMETER = "--baseline"
private const val PARALLEL_PARAMETER = "--parallel"
private const val DISABLE_DEFAULT_RULESETS_PARAMETER = "--disable-default-rulesets"
private const val BUILD_UPON_DEFAULT_CONFIG_PARAMETER = "--build-upon-default-config"
private const val AUTO_CORRECT_PARAMETER = "--auto-correct"
private const val ALL_RULES_PARAMETER = "--all-rules"
private const val REPORT_PARAMETER = "--report"
private const val GENERATE_CONFIG_PARAMETER = "--generate-config"
private const val CREATE_BASELINE_PARAMETER = "--create-baseline"
private const val CLASSPATH_PARAMETER = "--classpath"
private const val LANGUAGE_VERSION_PARAMETER = "--language-version"
private const val JVM_TARGET_PARAMETER = "--jvm-target"
private const val JDK_HOME_PARAMETER = "--jdk-home"
private const val BASE_PATH_PARAMETER = "--base-path"

internal sealed class CliArgument {
    abstract fun toArgument(): List<String>
}

internal data object CreateBaselineArgument : CliArgument() {
    override fun toArgument() = listOf(CREATE_BASELINE_PARAMETER)
}

internal data object GenerateConfigArgument : CliArgument() {
    override fun toArgument() = listOf(GENERATE_CONFIG_PARAMETER)
}

internal data class InputArgument(val fileCollection: FileCollection) : CliArgument() {
    override fun toArgument() = listOf(INPUT_PARAMETER, fileCollection.joinToString(",") { it.absolutePath })
}

internal data class ClasspathArgument(val fileCollection: FileCollection) : CliArgument() {
    override fun toArgument() = if (!fileCollection.isEmpty) {
        listOf(
            CLASSPATH_PARAMETER,
            fileCollection.joinToString(File.pathSeparator) { it.absolutePath }
        )
    } else {
        emptyList()
    }
}

internal data class LanguageVersionArgument(val languageVersion: String?) : CliArgument() {
    override fun toArgument() = languageVersion?.let { listOf(LANGUAGE_VERSION_PARAMETER, it) }.orEmpty()
}

internal data class JvmTargetArgument(val jvmTarget: String?) : CliArgument() {
    override fun toArgument() = jvmTarget?.let { listOf(JVM_TARGET_PARAMETER, it) }.orEmpty()
}

internal data class JdkHomeArgument(val jdkHome: DirectoryProperty) : CliArgument() {
    override fun toArgument() = jdkHome.orNull?.let { listOf(JDK_HOME_PARAMETER, it.toString()) }.orEmpty()
}

internal data class BaselineArgument(val baseline: RegularFile?) : CliArgument() {
    override fun toArgument() = baseline?.let { listOf(BASELINE_PARAMETER, it.asFile.absolutePath) }.orEmpty()
}

internal data class DefaultReportArgument(val type: DetektReportType, val file: RegularFile?) : CliArgument() {
    override fun toArgument() =
        file?.let { listOf(REPORT_PARAMETER, "${type.reportId}:${it.asFile.absoluteFile}") }.orEmpty()
}

internal data class CustomReportArgument(val reportId: String, val file: RegularFile) : CliArgument() {
    override fun toArgument() = listOf(REPORT_PARAMETER, "$reportId:${file.asFile.absolutePath}")
}

internal data class BasePathArgument(val basePath: String?) : CliArgument() {
    override fun toArgument() = basePath?.let { listOf(BASE_PATH_PARAMETER, it) }.orEmpty()
}

internal data class ConfigArgument(val files: Collection<File>) : CliArgument() {

    constructor(configFile: RegularFile) : this(listOf(configFile.asFile))
    constructor(config: FileCollection) : this(config.files)

    override fun toArgument() = if (files.isEmpty()) {
        emptyList()
    } else {
        listOf(CONFIG_PARAMETER, files.joinToString(",") { it.absolutePath })
    }
}

internal sealed class BoolCliArgument(open val value: Boolean, val configSwitch: String) : CliArgument() {
    override fun toArgument() = if (value) listOf(configSwitch) else emptyList()
}

internal data class DebugArgument(override val value: Boolean) : BoolCliArgument(value, DEBUG_PARAMETER)

internal data class ParallelArgument(override val value: Boolean) : BoolCliArgument(value, PARALLEL_PARAMETER)

internal data class DisableDefaultRuleSetArgument(
    override val value: Boolean
) : BoolCliArgument(value, DISABLE_DEFAULT_RULESETS_PARAMETER)

internal data class BuildUponDefaultConfigArgument(
    override val value: Boolean
) : BoolCliArgument(value, BUILD_UPON_DEFAULT_CONFIG_PARAMETER)

internal data class AllRulesArgument(override val value: Boolean) : BoolCliArgument(value, ALL_RULES_PARAMETER)

internal data class AutoCorrectArgument(override val value: Boolean) : BoolCliArgument(value, AUTO_CORRECT_PARAMETER)
