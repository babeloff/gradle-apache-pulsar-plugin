/**
 * https://docs.gradle.org/current/samples/sample_gradle_plugin.html
 */

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("maven-publish")
}

val pulsarVersion:String by project
val kotlinBuildVersion:String by project
val log4jVersion:String by project
val junitVersion:String by project

group = "org.ensample"
version = "2020-06-0"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    compileOnly(gradleApi())
    implementation(group="org.apache.logging.log4j", name="log4j-slf4j18-impl", version=log4jVersion)

    testImplementation(group="junit", name="junit", version=junitVersion)
}

// a.k.a. gradlePlugin
configure<GradlePluginDevelopmentExtension> {
    (plugins) {
        register("greeterPlugin") {
            id = "org.ensample.greeting"
            implementationClass = "org.ensample.gradle.api.plugins.GreetingPlugin"
        }
    }
}

// a.k.a. publishing
configure<PublishingExtension> {
    this.repositories {
        maven {
            url = uri("../../consuming/maven-repo")
        }
    }
}


