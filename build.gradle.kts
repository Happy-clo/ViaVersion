
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.5.0")
    }
}

plugins {
    base
    id("via.build-logic")
    id("com.guardsquare.proguard")
}
allprojects {
    group = "com.viaversion"
    version = property("projectVersion") as String // from gradle.properties
    description = "Allows the connection of newer clients to older server versions for Minecraft servers."
}

val main = setOf(
    projects.viaversion,
    projects.viaversionCommon,
    projects.viaversionApi,
    projects.viaversionBukkit,
    projects.viaversionVelocity,
    projects.viaversionFabric
).map { it.dependencyProject }

// 配置 ProGuard 的设置
extensions.configure<ProGuardExtension> {
    // 指定 ProGuard 配置文件
    configuration = file("$rootDir/proguard-rules.pro")
    
    // 指定输入 JAR 文件
    injars = file("$buildDir/libs/${project.name}-${version}.jar")
    
    // 指定输出的混淆后的 JAR 文件
    outjars = file("$buildDir/libs/${project.name}-${version}-obfuscated.jar")
}

// 注册一个任务来执行 ProGuard 的混淆操作
tasks.register("obfuscateJar", ProGuardTask::class) {
    description = "使用 ProGuard 混淆 JAR 文件"
    group = "build"
    
    // 确保在执行混淆任务前，先生成原始的 JAR 文件
    dependsOn(tasks.named("jar"))
    
    doLast {
        // 将混淆后的 JAR 文件复制到指定目录
        copy {
            from("$buildDir/libs/${project.name}-${version}-obfuscated.jar")
            into("$buildDir/libs/")
        }
    }
}

// 确保构建时会执行混淆任务
tasks.named("build").configure {
    dependsOn("obfuscateJar")
}