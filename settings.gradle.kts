pluginManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        jcenter()
        google()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        jcenter()
        google()
    }
}

rootProject.name = "Quicklink Caller"
include(":app")
 