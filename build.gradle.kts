import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    id("com.epam.drill.version.plugin")
    `maven-publish`
}

subprojects {
    apply(plugin = "com.epam.drill.version.plugin")

    apply { plugin(org.gradle.api.publish.maven.plugins.MavenPublishPlugin::class) }
    publishing {
        repositories {
            maven {
                url = uri("https://oss.jfrog.org/oss-release-local")
                credentials {
                    username =
                        if (project.hasProperty("bintrayUser"))
                            project.property("bintrayUser").toString()
                        else System.getenv("BINTRAY_USER")
                    password =
                        if (project.hasProperty("bintrayApiKey"))
                            project.property("bintrayApiKey").toString()
                        else System.getenv("BINTRAY_API_KEY")
                }
            }
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.allWarningsAsErrors = true
    }
}
