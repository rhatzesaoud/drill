import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    java
    application
}
application {
    mainClassName = "org.springframework.boot.loader.JarLauncher"
    val (pref, ex) = when {
        Os.isFamily(Os.FAMILY_MAC) -> Pair("lib", "dylib")
        Os.isFamily(Os.FAMILY_UNIX) -> Pair("lib", "so")
        else -> Pair("", "dll")
    }
    val drillDistrDir = rootProject.buildDir.resolve("install").resolve("nativeAgent").absolutePath
    val agentPath = "${file("$drillDistrDir/${pref}drill_agent.$ex")}"
    applicationDefaultJvmArgs = listOf(
        "-agentpath:$agentPath=drillInstallationDir=$drillDistrDir,adminAddress=${project.properties["adminAddress"]
            ?: "localhost:8090"},agentId=${project.properties["agentId"] ?: "Petclinic"}"
    )

}
repositories {
    mavenLocal()
    maven("https://dl.bintray.com/drill/drill4j")
}

dependencies {
    compileOnly("org.springframework:spring-context:5.1.8.RELEASE")
    implementation("org.springframework.samples:spring-petclinic:2.1.0")
}
tasks {
    named("run") {
        dependsOn(rootProject.tasks.getByPath("installNativeAgentDist"))
    }
}