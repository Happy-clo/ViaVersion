
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
        in main -> {
            plugins.apply("via.shadow-conventions")
            
            // 配置 ProGuard
            plugins.apply("com.guardsquare.proguard")

            extensions.configure<ProGuardExtension> {
                configuration = file("$rootDir/proguard-rules.pro")
                injars = file("$buildDir/libs/${project.name}-${version}.jar")
                outjars = file("$buildDir/libs/${project.name}-${version}-obfuscated.jar")
            }

            tasks.register("obfuscateJar", ProGuardTask::class) {
                description = "Obfuscate the JAR file using ProGuard"
                group = "build"
                dependsOn(tasks.named("jar"))
                doLast {
                    copy {
                        from("$buildDir/libs/${project.name}-${version}-obfuscated.jar")
                        into("$buildDir/libs/")
                    }
                }
            }
            
            tasks.named("build").configure {
                dependsOn("obfuscateJar")
            }
        }
        else -> plugins.apply("via.base-conventions")
    }
}