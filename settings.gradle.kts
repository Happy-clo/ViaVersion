enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    // 配置所有项目的仓库
    repositories {
        mavenCentral()
        maven("https://repo.viaversion.com")
        maven("https://jitpack.io")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

pluginManagement {
    // 默认插件版本
    repositories {
        gradlePluginPortal()
        maven("https://jitpack.io") // 如果插件在 JitPack 上
    }
    
    plugins {
        id("com.github.core-lib.xjar") version "4.0.2"
        id("net.kyori.blossom") version "2.1.0"
        id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
        id("com.gradleup.shadow") version "8.3.0"
        id("com.github.core-lib.xjar") version "4.0.2" // XJar 插件
    }
}

rootProject.name = "viaversion-parent"

includeBuild("build-logic")

// 设置子项目
setupViaSubproject("api")
setupViaSubproject("common")
setupViaSubproject("bukkit")
setupViaSubproject("bukkit-legacy")
setupViaSubproject("velocity")
setupViaSubproject("fabric")

setupSubproject("viaversion") {
    projectDir = file("universal")
}

fun setupViaSubproject(name: String) {
    setupSubproject("viaversion-$name") {
        projectDir = file(name)
    }
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}
