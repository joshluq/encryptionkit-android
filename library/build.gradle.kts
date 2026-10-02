import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.configure

plugins {
    alias(libs.plugins.pluginkit.android.library)
    alias(libs.plugins.pluginkit.quality)
    alias(libs.plugins.pluginkit.formatting)
    alias(libs.plugins.pluginkit.android.testing)
    alias(libs.plugins.pluginkit.android.publishing)
}

group = providers.gradleProperty("groupId").get()
version = providers.gradleProperty("libraryVersion").get()

configure<LibraryExtension> {
    namespace = "es.joshluq.encryptionkit"
}
dependencies {
    implementation("es.joshluq.kit:foundationkit:2.0.0-SNAPSHOT")
    implementation(libs.tink.android)
    implementation(libs.androidx.datastore.preferences)
    // Optional provider dependencies (not bundled into SDK AAR)
    compileOnly("androidx.room:room-common:2.8.5")
    compileOnly("androidx.biometric:biometric:1.1.0")

    // Required for JVM unit tests (AGP does not inherit compileOnly in test classpath)
    testImplementation("androidx.room:room-common:2.8.5")
    testImplementation("androidx.biometric:biometric:1.1.0")
}

pluginkitQuality {
    sonarHost = "https://sonarcloud.io"
    sonarProjectKey = "joshluq_encryptionkit-android"
    koverExclusions =
        listOf(
            "**.showcase.*",
            "**.di.*",
            "**.*_di_*",
            "**.BuildConfig",
            "**.R",
            "**.R$*",
            "**.Dagger*",
            "**.*_Factory",
            "**.*_Factory*",
            "**.*_MembersInjector",
            "**.*_HiltModules*",
            "**.Hilt_*",
            "**.*_Provide*Factory*",
        )
}

androidPublishing {
    repoName = "GitHubPackages"
    repoUrl = "${providers.gradleProperty("repositoryUrl").get()}/${providers.gradleProperty("artifactId").get()}-android"
    repoUser = System.getenv("GITHUB_ACTOR")
    repoPassword = System.getenv("GITHUB_TOKEN")
    version = "${project.version}${project.findProperty("versionType") ?: ""}"
    groupId = project.group.toString()
    artifactId = providers.gradleProperty("artifactId").get()
}
