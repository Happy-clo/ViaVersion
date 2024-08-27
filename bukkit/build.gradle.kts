dependencies {
    implementation(projects.viaversionBukkitLegacy)
    compileOnlyApi(projects.viaversionCommon)
    
    compileOnly(libs.paper) {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
        exclude("javax.persistence", "persistence-api")
    }

    // 添加 XJar 依赖
    implementation 'com.github.core-lib:xjar:4.0.2'
    
    // 如果使用 JUnit 测试类来运行加密，可以将 XJar 依赖的 scope 设置为 test
    // testImplementation 'com.github.core-lib:xjar:4.0.2'
}
