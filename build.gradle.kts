// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.pluginkit.android.application) apply false
    alias(libs.plugins.pluginkit.android.library) apply false
    alias(libs.plugins.pluginkit.android.compose) apply false
    alias(libs.plugins.pluginkit.android.hilt) apply false
    alias(libs.plugins.pluginkit.android.navigation) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.pluginkit.quality) apply false
    alias(libs.plugins.pluginkit.formatting) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.pluginkit.android.testing) apply false
    alias(libs.plugins.pluginkit.android.publishing) apply false
}

allprojects {
    group = providers.gradleProperty("groupId").get()
    version = "${providers.gradleProperty("libraryVersion").get()}${project.findProperty("versionType") ?: ""}"
}
