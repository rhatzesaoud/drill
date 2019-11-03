subprojects {
    repositories {
        mavenCentral()
        jcenter()
        mavenLocal()
        maven(url = "https://oss.jfrog.org/artifactory/list/oss-release-local")
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }
}