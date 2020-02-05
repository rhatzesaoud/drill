subprojects {
    repositories {
        jcenter()
        mavenLocal()
        mavenCentral()
        maven(url = "https://kotlin.bintray.com/kotlinx")
        maven(url = "https://oss.jfrog.org/artifactory/list/oss-release-local")
    }
}