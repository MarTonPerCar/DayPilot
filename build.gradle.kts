// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.sonarqube") version "5.1.0.4882"
}

sonar {
    properties {
        property("sonar.projectKey", "daypilot-test_daypilot-android")
        property("sonar.organization", "daypilot-test")
        property("sonar.host.url", "https://sonarcloud.io")
        // Resolved via Gradle's own build-directory API instead of a hand-written relative
        // string — a bare "app/build/..." path silently failed to resolve in CI even though
        // it matched the real file when checked locally.
        val appBuildDir = project(":app").layout.buildDirectory.get()
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "$appBuildDir/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
        )
        property(
            "sonar.androidLint.reportPaths",
            "$appBuildDir/reports/lint-results-debug.xml"
        )
        property(
            "sonar.exclusions",
            "**/res/**/*.webp,**/res/**/*.png,**/build/**"
        )
        property(
            "sonar.coverage.exclusions",
            "**/*Screen.kt,**/core/ui/components/**,**/core/ui/theme/**,**/data/supabase/dto/**,**/ComponentCatalog.kt"
        )
        property(
            "sonar.cpd.exclusions",
            "**/core/ui/theme/Color.kt"
        )
    }
}
