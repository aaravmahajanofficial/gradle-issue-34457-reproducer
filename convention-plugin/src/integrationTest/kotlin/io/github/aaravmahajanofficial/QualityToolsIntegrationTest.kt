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
package io.github.aaravmahajanofficial

import io.github.aaravmahajanofficial.utils.TestProjectBuilder
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.io.path.readText

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DisplayName("Quality Tools Integration Tests")
class QualityToolsIntegrationTest {
    lateinit var builder: TestProjectBuilder

    @Test
    @DisplayName("should execute Jacoco with defaults")
    fun `execute jacoco with defaults`() {
        builder =
            TestProjectBuilder
                .create()
                .withVersionCatalog()
                .withSettingsGradle()
                .withGradleProperties(
                    mapOf(
                        "org.gradle.jvmargs" to
                            "--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED" +
                            "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED" +
                            "--add-opens=java.base/java.lang=ALL-UNNAMED" +
                            "--add-opens=java.base/java.io=ALL-UNNAMED" +
                            "--add-opens=java.base/java.util=ALL-UNNAMED" +
                            "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED" +
                            "--add-opens=java.base/java.security=ALL-UNNAMED" +
                            "--add-opens=java.base/java.net=ALL-UNNAMED" +
                            "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED" +
                            "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED" +
                            "--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED",

                        "org.gradle.daemon" to "false"
                    ),
                ).withBuildGradle(
                    """
                    plugins {
                        `jvm-test-suite`
                        `java-gradle-plugin`
                        id("io.github.aaravmahajanofficial.jenkins-gradle-convention-plugin")
                    }

                    testing {
                        suites {
                            val test by getting(JvmTestSuite::class) {
                                useJUnitJupiter()
                            }
                        }
                    }

                    jenkinsConvention {
                        quality {
                            jacoco {
                                minimumCodeCoverage = 0.5
                            }
                        }
                    }

                    """.trimIndent(),
                ).withJavaSource()
                .withTestSource()

        val result = builder.runGradle("test", "jacocoTestReport", "jacocoTestCoverageVerification")
        result.task("test")?.outcome shouldBe TaskOutcome.SUCCESS
        result.task("jacocoTestReport")?.outcome shouldBe TaskOutcome.SUCCESS
        result.task("jacocoTestCoverageVerification")?.outcome shouldBe TaskOutcome.SUCCESS

        val xmlReport = builder.projectDir.resolve("build/reports/jacoco/test/jacocoTestReport.xml")
        val htmlReport = builder.projectDir.resolve("build/reports/jacoco/test/html/index.html")
        val csvReport = builder.projectDir.resolve("build/reports/jacoco/test/jacocoTestReport.csv")

        println(xmlReport.readText())

        xmlReport.shouldExist()
        htmlReport.shouldExist()
        csvReport.shouldExist()
    }
}
