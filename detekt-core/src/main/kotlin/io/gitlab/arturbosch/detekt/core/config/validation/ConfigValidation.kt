package io.gitlab.arturbosch.detekt.core.config.validation

import io.github.detekt.tooling.api.InvalidConfig
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.ConfigValidator
import io.gitlab.arturbosch.detekt.api.Notification
import io.gitlab.arturbosch.detekt.core.NL
import io.gitlab.arturbosch.detekt.core.ProcessingSettings
import io.gitlab.arturbosch.detekt.core.config.YamlConfig
import io.gitlab.arturbosch.detekt.core.extensions.loadExtensions
import io.gitlab.arturbosch.detekt.core.reporting.red
import io.gitlab.arturbosch.detekt.core.reporting.yellow

/**
 * Known existing properties on rule's which my be absent in the default-detekt-config.yml.
 *
 * We need to predefine them as the user may not have already declared an 'config'-block
 * in the configuration and we want to validate the config by default.
 */
internal val DEFAULT_PROPERTY_EXCLUDES = setOf(
    ".*>excludes",
    ".*>includes",
    ".*>active",
    ".*>.*>excludes",
    ".*>.*>includes",
    ".*>.*>active",
    ".*>.*>autoCorrect",
    ".*>severity",
    ".*>.*>severity",
    "build>weights.*",
    ".*>.*>ignoreAnnotated",
    ".*>.*>ignoreFunction",
).joinToString(",")

internal fun checkConfiguration(settings: ProcessingSettings, baseline: Config) {
    var shouldValidate = settings.spec.configSpec.shouldValidateBeforeAnalysis
    if (shouldValidate == null) {
        val props = settings.config.subConfig("config")
        shouldValidate = props.valueOrDefault("validation", true)
    }
    if (shouldValidate) {
        val validators =
            loadExtensions<ConfigValidator>(settings) + DefaultPropertiesConfigValidator(settings, baseline)
        val notifications = validators.flatMap { it.validate(settings.config) }
        notifications.map(Notification::message).forEach(settings::info)
        val errors = notifications.filter(Notification::isError)
        if (errors.isNotEmpty()) {
            val problems = notifications.joinToString(NL) { "\t- ${it.renderMessage()}" }
            val propsString = if (errors.size == 1) "property" else "properties"
            val title = "Run failed with ${errors.size} invalid config $propsString.".red()
            throw InvalidConfig("$title$NL$problems")
        }
    }
}

internal fun validateConfig(
    config: Config,
    baseline: Config,
    excludePatterns: Set<Regex>
): List<Notification> {
    require(baseline != Config.empty) { "Cannot validate configuration based on an empty baseline config." }
    require(baseline is YamlConfig) {
        val yamlConfigClass = YamlConfig::class.simpleName
        val actualClass = baseline.javaClass.simpleName

        "Only supported baseline config is the $yamlConfigClass. Actual type is $actualClass"
    }

    if (config == Config.empty) {
        return emptyList()
    }

    return when (config) {
        is YamlConfig -> validateYamlConfig(config, baseline, excludePatterns)
        is ValidatableConfiguration -> config.validate(baseline, excludePatterns)
        else -> error("Unsupported config type for validation: '${config::class}'.")
    }
}

private fun validateYamlConfig(
    configToValidate: YamlConfig,
    baseline: YamlConfig,
    excludePatterns: Set<Regex>
): List<Notification> {
    val validators: List<ConfigValidator> = listOf(
        InvalidPropertiesConfigValidator(baseline, excludePatterns),
        DeprecatedPropertiesConfigValidator(),
        MissingRulesConfigValidator(baseline, excludePatterns)
    )

    return validators.flatMap { it.validate(configToValidate) }
}

internal fun Notification.renderMessage(): String =
    when (level) {
        Notification.Level.Error -> message.red()
        Notification.Level.Warning -> message.yellow()
        Notification.Level.Info -> message
    }
