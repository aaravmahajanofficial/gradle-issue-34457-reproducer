import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.dsl)
}

val javaToolchainVersion: Provider<Int> =
    providers.gradleProperty("java.toolchain.version").map(String::toInt).orElse(21)

java {
    toolchain {
        languageVersion = javaToolchainVersion.map(JavaLanguageVersion::of)
    }
}

kotlin {
    jvmToolchain {
        languageVersion = javaToolchainVersion.map(JavaLanguageVersion::of)
    }

    explicitApi()

    compilerOptions {
        apiVersion =
            org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2
        jvmTarget = JvmTarget.JVM_21

        allWarningsAsErrors.set(true)
        progressiveMode.set(false)

        optIn.add("kotlin.RequiresOptIn")
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xjvm-default=all",
        )
    }
}

gradlePlugin {
    plugins {
        create("kotlinConventions") {
            id = "conventions.kotlin"
            displayName = "Kotlin Conventions"
            implementationClass = "conventions.KotlinConventionsPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation(libs.kotlin.gradle.plugin)
}
