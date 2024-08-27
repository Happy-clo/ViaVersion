plugins {
    base
    id("via.build-logic")
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

subprojects {
    when (this) {
        in main -> plugins.apply("via.shadow-conventions")
        else -> plugins.apply("via.base-conventions")
    }
}
repositories {
    maven("https://jitpack.io")
}

plugins {
    id("com.github.core-lib.xjar") version "4.0.2" apply false
}

tasks.register<com.github.core_lib.xjar.XJarTask>("xjar") {
    // 这里是 XJar 的配置
    password.set("io.xjar")

    // 可选参数
    // algorithm.set("yourAlgorithm")
    keySize.set(128)
    ivSize.set(16)
    includes.add("**/*.class") // 添加需要包含的文件
    excludes.add("**/api/**") // 添加需要排除的文件
    sourceDir.set(file("build/libs")) 
    sourceJar.set(file("build/libs/ViaVersion-5.0.4-SNAPSHOT.jar"))
    targetDir.set(file("build/libs"))
    targetJar.set(file("yourTarget.jar"))
}

tasks.named("jar") {
    dependsOn("xjar")
}