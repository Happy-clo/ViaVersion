plugins {
    base
    id("via.build-logic")
    id("com.github.core-lib.xjar") version "4.0.2" apply false
}

allprojects {
    group = "com.viaversion"
    version = property("projectVersion") as String // 从 gradle.properties 中获取项目版本
    description = "Allows the connection of newer clients to older server versions for Minecraft servers."
}

val mainProjects = setOf(
    projects.viaversion,
    projects.viaversionCommon,
    projects.viaversionApi,
    projects.viaversionBukkit,
    projects.viaversionVelocity,
    projects.viaversionFabric
).map { it.dependencyProject }

subprojects {
    when (this) {
        in mainProjects -> plugins.apply("via.shadow-conventions")
        else -> plugins.apply("via.base-conventions")
    }
}

repositories {
    maven("https://jitpack.io")
}

// 注册 XJar 任务
tasks.register<com.github.core_lib.xjar.XJarTask>("xjar") {
    // XJar 的配置
    password.set("io.xjar")

    // 可选参数
    keySize.set(128) // 设置密钥大小
    ivSize.set(16) // 设置初始化向量大小
    includes.add("**/*.class") // 添加需要包含的文件
    excludes.add("**/api/**") // 添加需要排除的文件
    sourceDir.set(file("build/libs")) 
    sourceJar.set(file("build/libs/ViaVersion-5.0.4-SNAPSHOT.jar"))
    targetDir.set(file("build/libs"))
    targetJar.set(file("yourTarget.jar"))
}

// 使 jar 任务依赖于 xjar 任务
tasks.named("jar") {
    dependsOn("xjar")
}
