package io.github.detekt.parser

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.addJavaSourceRoots
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.pom.PomModel
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader
import java.nio.file.Path

/**
 * Creates an environment instance which can be used to compile source code to KtFile's.
 * This environment also allows to modify the resulting AST files.
 */
fun createKotlinCoreEnvironment(
    configuration: CompilerConfiguration = CompilerConfiguration(),
    disposable: Disposable = Disposer.newDisposable(),
    printStream: PrintStream,
): KotlinCoreEnvironment {
    configuration.put(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
        PrintingMessageCollector(printStream, MessageRenderer.PLAIN_FULL_PATHS, false)
    )
    configuration.put(CommonConfigurationKeys.MODULE_NAME, "detekt")

    val environment = KotlinCoreEnvironment.createForProduction(
        disposable,
        configuration,
        EnvironmentConfigFiles.JVM_CONFIG_FILES
    )

    val projectCandidate = environment.project

    val project = requireNotNull(projectCandidate as? MockProject) {
        "MockProject type expected, actual - ${projectCandidate.javaClass.simpleName}"
    }

    project.registerService(PomModel::class.java, DetektPomModel)

    return environment
}

/**
 * Creates a compiler configuration for the kotlin compiler with all known sources and classpath jars.
 * Be aware that if any path of [pathsToAnalyze] is a directory it is scanned for java and kotlin files.
 */
fun createCompilerConfiguration(
    pathsToAnalyze: List<Path>,
    classpath: List<String>,
    apiVersion: ApiVersion?,
    languageVersion: LanguageVersion?,
    jvmTarget: JvmTarget,
    jdkHome: Path?,
): CompilerConfiguration {
    val javaFiles = pathsToAnalyze.flatMap { path ->
        path.toFile().walk()
            .filter { it.isFile && it.extension.equals("java", true) }
            .toList()
    }
    val kotlinFiles = pathsToAnalyze.flatMap { path ->
        path.toFile().walk()
            .filter { it.isFile }
            .filter { it.extension.equals("kt", true) || it.extension.equals("kts", true) }
            .map { it.absolutePath }
            .toList()
    }

    val classpathFiles = classpath.map { File(it) }
    val retrievedLanguageVersion = languageVersion ?: classpathFiles.getKotlinLanguageVersion()
    val retrievedApiVersion = when {
        apiVersion != null -> apiVersion
        languageVersion != null -> ApiVersion.createByLanguageVersion(languageVersion)
        else -> ApiVersion.LATEST_STABLE
    }
    val languageVersionSettings: LanguageVersionSettings? = retrievedLanguageVersion?.let {
        LanguageVersionSettingsImpl(
            languageVersion = it,
            apiVersion = retrievedApiVersion
        )
    }

    return CompilerConfiguration().apply {
        if (languageVersionSettings != null) {
            put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, languageVersionSettings)
        }
        put(JVMConfigurationKeys.JVM_TARGET, jvmTarget)
        addJavaSourceRoots(javaFiles)
        addKotlinSourceRoots(kotlinFiles)
        addJvmClasspathRoots(classpathFiles)

        if (jdkHome != null) {
            put(JVMConfigurationKeys.JDK_HOME, jdkHome.toFile())
        } else {
            put(JVMConfigurationKeys.JDK_HOME, File(System.getProperty("java.home")))
        }

        configureJdkClasspathRoots()
    }
}

/**
 * Infer the language version from the files representing classpath.
 */
internal fun Iterable<File>.getKotlinLanguageVersion(): LanguageVersion? {
    val urls = map { it.toURI().toURL() }
    if (urls.isEmpty()) {
        return null
    }
    return URLClassLoader(urls.toTypedArray()).use { classLoader ->
        runCatching {
            val clazz = classLoader.loadClass("kotlin.KotlinVersion")
            val field = clazz.getField("CURRENT")
            field.isAccessible = true
            val versionObj = field[null]
            val versionString = versionObj?.toString()
            versionString?.let { LanguageVersion.fromFullVersionString(it) }
        }.getOrNull()
    }
}
