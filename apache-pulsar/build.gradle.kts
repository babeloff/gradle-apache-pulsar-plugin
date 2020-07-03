/**
 * https://docs.gradle.org/current/samples/sample_gradle_plugin.html
 */

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("maven-publish")
}

val kotlinBuildVersion:String by project
val log4jVersion:String by project
val junitVersion:String by project

val pulsarVersion:String by project

group = "org.ensample"
version = "2020-06-0"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    compileOnly(gradleApi())
//    implementation(group="org.apache.logging.log4j", name="log4j-slf4j18-impl", version=log4jVersion)

    implementation(group="org.apache.pulsar", name="pulsar-io-core", version=pulsarVersion)
    implementation(group="org.apache.pulsar", name="pulsar-common", version=pulsarVersion)
    implementation(group="org.apache.pulsar", name="buildtools", version=pulsarVersion)
    implementation(group="org.apache.pulsar", name="pulsar-functions-runtime", version=pulsarVersion)
    implementation(group="org.apache.pulsar", name="pulsar-functions-local-runner", version=pulsarVersion)

    testImplementation(group="junit", name="junit", version=junitVersion)
}

// a.k.a. gradlePlugin
configure<GradlePluginDevelopmentExtension> {
    (plugins) {
        register("pulsarPlugin") {
            id = "org.babeloff.apache-pulsar"
            implementationClass = "org.babeloff.gradle.pulsar.api.plugins.PulsarPlugin"
        }
        register("pulsarBasePlugin") {
            id = "org.babeloff.apache-pulsar-base"
            implementationClass = "org.babeloff.gradle.pulsar.api.plugins.PulsarBasePlugin"
        }
    }
}

// a.k.a. publishing
configure<PublishingExtension> {
    this.repositories {
        mavenLocal()
    }
}

