

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("kapt")  version "1.3.50"
//    id ("org.asciidoctor.jvm.convert") version "3.1.0"
}

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
}

val pulsarVersion = "2.6.0"
val kotlinBuildVersion = "1.2.1"
val log4jVersion = "2.13.3"

kapt {
    includeCompileClasspath = false
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(group="org.ajoberstar", name="grgit") {
        version { strictly("1.9.1") }
    }

    implementation(group="org.apache.logging.log4j", name="log4j-slf4j18-impl", version=log4jVersion)
    implementation(group="org.ajoberstar", name="gradle-git", version="1.7.1")
    implementation(group="org.ajoberstar", name="gradle-git-publish", version="0.3.3")

    implementation(group="com.thinkinglogic.builder", name="kotlin-builder-annotation", version=kotlinBuildVersion)
    kapt(group="com.thinkinglogic.builder", name="kotlin-builder-processor", version=kotlinBuildVersion)

    implementation(group="org.apache.pulsar", name="pulsar-io-core", version=pulsarVersion)
    implementation(group="org.apache.pulsar", name="pulsar-common", version=pulsarVersion)
    implementation(group="org.apache.pulsar", name="buildtools", version=pulsarVersion)
    implementation(group="org.apache.pulsar", name="pulsar-functions-runtime", version=pulsarVersion)
    implementation(group="org.apache.pulsar", name="pulsar-functions-local-runner", version=pulsarVersion)
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "org.babeloff.apache-pulsar"
            implementationClass = "org.babeloff.gradle.pulsar.api.plugins.PulsarPlugin"
        }
    }
}

