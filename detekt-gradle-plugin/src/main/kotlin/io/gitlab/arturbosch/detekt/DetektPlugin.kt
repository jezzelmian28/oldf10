package io.gitlab.arturbosch.detekt

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.internal.DetektAndroid
import io.gitlab.arturbosch.detekt.internal.DetektJvm
import io.gitlab.arturbosch.detekt.internal.registerCreateBaselineTask
import io.gitlab.arturbosch.detekt.internal.registerDetektTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.gradle.language.base.plugins.LifecycleBasePlugin

class DetektPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply(ReportingBasePlugin::class.java)
        val extension = project.extensions.create(DETEKT_EXTENSION, DetektExtension::class.java)
        extension.reportsDir = project.extensions.getByType(ReportingExtension::class.java).file("detekt")

        val defaultConfigFile =
            project.file("${project.rootProject.layout.projectDirectory.dir(CONFIG_DIR_NAME)}/$CONFIG_FILE")
        if (defaultConfigFile.exists()) {
            extension.config = project.files(defaultConfigFile)
        }

        configurePluginDependencies(project, extension)
        setTaskDefaults(project)

        project.registerDetektVanillaTask(extension)
        project.registerDetektJvmTasks(extension)
        project.registerDetektAndroidTasks(extension)
        project.registerGenerateConfigTask(extension)
    }

    private fun Project.registerDetektJvmTasks(extension: DetektExtension) {
        plugins.withId("org.jetbrains.kotlin.jvm") {
            DetektJvm(this).registerTasks(extension)
        }
    }

    private fun Project.registerDetektAndroidTasks(extension: DetektExtension) {
        plugins.withId("kotlin-android") {
            DetektAndroid(this).registerTasks(extension)
        }
    }

    private fun Project.registerDetektVanillaTask(extension: DetektExtension) {
        val detektTaskProvider = registerDetektTask(DETEKT_TASK_NAME, extension) {
            baseline.set(project.layout.file(project.provider { extension.baseline }))
            setSource(existingInputDirectoriesProvider(project, extension))
            setIncludes(defaultIncludes)
            setExcludes(defaultExcludes)
            reportsDir.set(project.provider { extension.customReportsDir })
            reports = extension.reports
        }

        tasks.matching { it.name == LifecycleBasePlugin.CHECK_TASK_NAME }.configureEach {
            it.dependsOn(detektTaskProvider)
        }

        registerCreateBaselineTask(BASELINE_TASK_NAME, extension) {
            setSource(existingInputDirectoriesProvider(project, extension))
            baseline.set(project.layout.file(project.provider { extension.baseline }))
        }
    }

    private fun Project.registerGenerateConfigTask(extension: DetektExtension) {
        tasks.register(GENERATE_CONFIG, DetektGenerateConfigTask::class.java) {
            it.config.setFrom(project.provider { extension.config })
        }
    }

    private fun existingInputDirectoriesProvider(
        project: Project,
        extension: DetektExtension
    ): Provider<FileCollection> = project.provider { extension.input.filter { it.exists() } }

    private fun configurePluginDependencies(project: Project, extension: DetektExtension) {
        project.configurations.create(CONFIGURATION_DETEKT_PLUGINS) { configuration ->
            configuration.isVisible = false
            configuration.isTransitive = true
            configuration.description = "The $CONFIGURATION_DETEKT_PLUGINS libraries to be used for this project."
        }

        project.configurations.create(CONFIGURATION_DETEKT) { configuration ->
            configuration.isVisible = false
            configuration.isTransitive = true
            configuration.description = "The $CONFIGURATION_DETEKT dependencies to be used for this project."

            configuration.defaultDependencies { dependencySet ->
                val version = extension.toolVersion ?: DEFAULT_DETEKT_VERSION
                dependencySet.add(project.dependencies.create("io.gitlab.arturbosch.detekt:detekt-cli:$version"))
            }
        }
    }

    private fun setTaskDefaults(project: Project) {
        project.tasks.withType(Detekt::class.java).configureEach {
            it.detektClasspath.setFrom(project.configurations.getAt(CONFIGURATION_DETEKT))
            it.pluginClasspath.setFrom(project.configurations.getAt(CONFIGURATION_DETEKT_PLUGINS))
        }

        project.tasks.withType(DetektCreateBaselineTask::class.java).configureEach {
            it.detektClasspath.setFrom(project.configurations.getAt(CONFIGURATION_DETEKT))
            it.pluginClasspath.setFrom(project.configurations.getAt(CONFIGURATION_DETEKT_PLUGINS))
        }

        project.tasks.withType(DetektGenerateConfigTask::class.java).configureEach {
            it.detektClasspath.setFrom(project.configurations.getAt(CONFIGURATION_DETEKT))
        }
    }

    companion object {
        const val DETEKT_TASK_NAME = "detekt"
        const val BASELINE_TASK_NAME = "detektBaseline"
        const val DETEKT_EXTENSION = "detekt"
        private const val GENERATE_CONFIG = "detektGenerateConfig"
        internal val defaultExcludes = listOf("build/")
        internal val defaultIncludes = listOf("**/*.kt", "**/*.kts")
        internal const val CONFIG_DIR_NAME = "config/detekt"
        internal const val CONFIG_FILE = "detekt.yml"
    }
}

const val CONFIGURATION_DETEKT = "detekt"
const val CONFIGURATION_DETEKT_PLUGINS = "detektPlugins"
