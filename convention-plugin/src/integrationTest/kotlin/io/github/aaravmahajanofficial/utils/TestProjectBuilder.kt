/*
 * Copyright 2025 Aarav Mahajan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.github.aaravmahajanofficial.utils

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.tooling.BuildException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

class TestProjectBuilder(
    val projectDir: Path,
) {
    init {
        Files.createDirectories(projectDir)
    }

    fun withVersionCatalog(content: String = defaultVersionCatalog): TestProjectBuilder {
        val catalogDir = projectDir.resolve("gradle")
        Files.createDirectories(catalogDir)
        catalogDir.resolve("libs.versions.toml").writeText(content)
        return this
    }

    fun withSettingsGradle(content: String = defaultSettings): TestProjectBuilder {
        projectDir.resolve("settings.gradle.kts").writeText(content)
        return this
    }

    fun withBuildGradle(content: String): TestProjectBuilder {
        projectDir.resolve("build.gradle.kts").writeText(content)
        return this
    }

    fun withGradleProperties(properties: Map<String, String>): TestProjectBuilder {
        val content = properties.entries.joinToString("\n") { "${it.key}=${it.value}" }
        projectDir.resolve("gradle.properties").writeText(content)
        return this
    }

    fun withJavaSource(
        packageName: String = "com.example",
        className: String = "JavaTestClass",
        content: String? = null,
    ): TestProjectBuilder {
        val sourceDir = projectDir.resolve("src/main/java/${packageName.replace('.', '/')}")
        Files.createDirectories(sourceDir)

        val javaContent =
            content
                ?: """
                    package $packageName;

                    /**
                     * A class that returns a message.
                     * This class is not intended to be subclassed.
                     */
                    public class $className {
                        /**
                         * Returns the message.
                         * @return a greeting string
                         */
                        public String getMessage() {
                            return "Hello from $className :)";
                        }
                    }

                    """.trimIndent()

        sourceDir.resolve("$className.java").writeText(javaContent)
        return this
    }

    fun withTestSource(
        packageName: String = "com.example",
        className: String = "JavaTestClassTest",
        testClassName: String = "JavaTestClass",
    ): TestProjectBuilder {
        val testDir = projectDir.resolve("src/test/java/${packageName.replace('.', '/')}")

        Files.createDirectories(testDir)

        val testContent = """
                    package $packageName;

                    import org.junit.jupiter.api.Test;
                    import static org.junit.jupiter.api.Assertions.*;

                    public class $className {
                        @Test
                        public void testGetMessage() {
                            $testClassName testClass = new $testClassName();
                            assertEquals("Hello from $testClassName :)", testClass.getMessage());
                        }
                    }

                    """.trimIndent()

        val fileExtension = "java"
        testDir.resolve("$className.$fileExtension").writeText(testContent)
        return this
    }

    fun runGradle(vararg tasks: String): BuildResult = runGradle(tasks.toList())

    fun runGradle(
        tasks: List<String>,
        arguments: List<String> = emptyList(),
        expectFailure: Boolean = false,
    ): BuildResult {
        val allArgs =
            buildList {
                addAll(tasks)
                addAll(arguments)
                add("--stacktrace")
                add("--info")
                add("--build-cache")
            }

        val runner =
            GradleRunner
                .create()
                .withProjectDir(projectDir.toFile())
                .withArguments(allArgs)
                .withPluginClasspath()
                .withDebug(true)

        return try {
            if (expectFailure) runner.buildAndFail() else runner.build()
        } catch (e: BuildException) {
            throw BuildException("Build failed for tasks: $tasks\n${e.message}", e)
        }
    }

    companion object {
        fun create(): TestProjectBuilder {
            val tempDir = Files.createTempDirectory("gradle-test-")
            return TestProjectBuilder(tempDir)
        }

        private val defaultVersionCatalog =
            """
            [versions]
            kotlin = "2.2.0"
            jenkins-core = "2.520"
            jacoco = "0.8.13"

            [libraries]
            # Kotlin
            kotlin-bom = { module = "org.jetbrains.kotlin:kotlin-bom", version.ref = "kotlin" }
            kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
            kotlin-reflect = { module = "org.jetbrains.kotlin:kotlin-reflect", version.ref = "kotlin" }
            kotlin-gradle-plugin = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }
            jetbrains-annotations = { module = "org.jetbrains:annotations", version = "26.0.2" }

            """.trimIndent()

        private val defaultSettings =
            """
            rootProject.name = "test-project"

            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                    gradlePluginPortal()
                }
            }

            """.trimIndent()
    }
}
