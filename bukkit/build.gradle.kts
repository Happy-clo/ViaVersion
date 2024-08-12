repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.luckperms:api:5.4")
    implementation(projects.viaversionBukkitLegacy)
    compileOnlyApi(projects.viaversionCommon)
    compileOnly(libs.paper) {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
        exclude("javax.persistence", "persistence-api")
    }
}