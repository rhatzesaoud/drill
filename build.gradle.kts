allprojects {
    repositories {
        mavenCentral()
        jcenter()
        if (version.toString().endsWith("-SNAPSHOT"))
            maven(url = "https://oss.jfrog.org/artifactory/list/oss-snapshot-local")
        else {
            maven(url = "https://oss.jfrog.org/artifactory/list/oss-release-local")
        }
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }
}