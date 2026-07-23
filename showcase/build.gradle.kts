import com.android.build.api.dsl.ApplicationExtension
import org.gradle.kotlin.dsl.configure

plugins {
    alias(libs.plugins.pluginkit.android.application)
    alias(libs.plugins.pluginkit.android.compose)
    alias(libs.plugins.pluginkit.android.hilt)
    alias(libs.plugins.pluginkit.quality)
    alias(libs.plugins.pluginkit.android.testing)
}

configure<ApplicationExtension> {
    namespace = "es.joshluq.encryptionkit.showcase"

    defaultConfig {
        applicationId = "es.joshluq.encryptionkit.showcase"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":encryptionkit"))
    implementation("es.joshluq.kit:foundationkit:1.3.0")
    implementation(libs.androidx.datastore.preferences)
}
