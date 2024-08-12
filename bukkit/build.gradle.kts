repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.luckperms:api:5.4")
    implementation(projects.viaversionBukkitLegacy)
    compileOnlyApi(projects.viaversionCommon)
    compileOnly(libs.paper) {
        exclude group: "junit", module: "junit"
        exclude group: "com.google.code.gson", module: "gson"
        exclude group: "javax.persistence", module: "persistence-api"
    }
}