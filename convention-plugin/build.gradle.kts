import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `jvm-test-suite`
    `java-gradle-plugin`
    id("conventions.kotlin")
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    website = "https://github.com/aaravmahajanofficial/jenkins-gradle-convention-plugin"
    vcsUrl = "https://github.com/aaravmahajanofficial/jenkins-gradle-convention-plugin"
    plugins {
        create("jenkinsConventions") {
            id = "io.github.aaravmahajanofficial.jenkins-gradle-convention-plugin"
            displayName = "Jenkins Gradle Convention Plugin"
            implementationClass = "io.github.aaravmahajanofficial.JenkinsConventionPlugin"
        }
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("integrationTest") {

            useJUnitJupiter()

            dependencies {
                implementation(project())
                implementation(gradleTestKit())
                implementation(libs.kotest.gradle.plugin)
            }

            targets.configureEach {
                testTask.configure {

                    classpath += files(tasks.named("pluginUnderTestMetadata"))

                    shouldRunAfter(test)

                    testLogging {
                        events("passed", "skipped", "failed", "standardOut", "standardError")
                        exceptionFormat = TestExceptionFormat.FULL
                        showExceptions = true
                        showCauses = true
                        showStackTraces = true
                    }

                    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

                    // JVM args needed for withDebug(true) in GradleRunner
                    jvmArgs(
                        "--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED"
                    )
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}

tasks.register<Copy>("publishToLocal") {
    dependsOn("publishToMavenLocal")
}
