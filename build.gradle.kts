// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktfmt) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

val installGitHooks by tasks.registering(Copy::class) {
    description = "Installs git pre-commit hook for ktfmtCheck"
    from(rootProject.layout.projectDirectory.file("scripts/git-hooks/pre-commit"))
    into(rootProject.layout.projectDirectory.dir(".git/hooks"))
    filePermissions {
        user {
            read = true
            write = true
            execute = true
        }
        group {
            read = true
            execute = true
        }
        other {
            read = true
            execute = true
        }
    }
}

subprojects {
    apply(plugin = "com.ncorti.ktfmt.gradle")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<com.ncorti.ktfmt.gradle.KtfmtExtension> {
        googleStyle()
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    }

    tasks.matching { it.name == "preBuild" }.configureEach {
        dependsOn(installGitHooks)
    }
}
