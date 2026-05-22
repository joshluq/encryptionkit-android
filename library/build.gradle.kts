import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.configure

plugins {
    alias(libs.plugins.pluginkit.android.library)
    alias(libs.plugins.pluginkit.quality)
    alias(libs.plugins.pluginkit.android.testing)
    alias(libs.plugins.pluginkit.android.publishing)
}

group = providers.gradleProperty("groupId").get()
version = providers.gradleProperty("libraryVersion").get()

configure<LibraryExtension> {
    namespace = "es.joshluq.encryptionkit"
}

dependencies {
    implementation("es.joshluq.kit:foundationkit:1.2.0-SNAPSHOT")
    implementation("com.google.crypto.tink:tink-android:1.21.0")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
}

pluginkitQuality {
    sonarHost = "https://sonarcloud.io"
    sonarProjectKey = "joshluq_encryptionkit-android"
    koverExclusions = listOf(
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
        "**.*_Provide*Factory*"
    )
}

androidPublishing {
    repoName = "GitHubPackages"
    repoUrl = "${providers.gradleProperty("repositoryUrl").get()}/${providers.gradleProperty("artifactId").get()}-android"
    repoUser = System.getenv("GITHUB_ACTOR")
    repoPassword = System.getenv("GITHUB_TOKEN")
    version = "${project.version}${project.findProperty("versionType")}"
    groupId = project.group.toString()
    artifactId = providers.gradleProperty("artifactId").get()
}
