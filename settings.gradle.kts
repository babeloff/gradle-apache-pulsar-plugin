/*
 * The settings file is used to specify which projects to include in your build.
 * 
 * Detailed information about configuring a multi-project
 * build in Gradle can be found in the user guide.
 * https://docs.gradle.org/5.0-rc-3/userguide/multi_project_builds.html
 *
 * https://docs.gradle.org/current/dsl/org.gradle.plugin.management.PluginManagementSpec.html
 */

pluginManagement {

    val kotlinVersion: String by settings

    plugins {
        kotlin("multiplatform") version kotlinVersion
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion

//        id("java-gradle-plugin")

        // Storage of various keys provided by data providers
        // gradle addCredentials --key someKey --value someValue
        // https://github.com/etiennestuder/gradle-credentials-plugin
        id("nu.studer.credentials") version "1.0.4"

        id("org.gradle.kotlin.embedded-kotlin") version "1.3.3"
        id("org.gradle.kotlin.kotlin-dsl") version "1.3.3"

        id("org.asciidoctor.jvm.convert") version "3.2.0"
        id("com.github.johnrengelman.shadow") version "6.0.0"

        // https://mvnrepository.com/artifact/org.hidetake.ssh/org.hidetake.ssh.gradle.plugin
        id ("org.hidetake.ssh") version "2.10.1"

        id ("org.unbroken-dome.test-sets") version "2.2.1"

        // https://github.com/ragill/nar-gradle-plugin
        id("me.ragill.nar-plugin") version "1.0.2"
    }

    resolutionStrategy {
        eachPlugin {
            println("resolve module: ns=${requested.id.namespace}, nm=${requested.id.name}" )
            when (requested.id.namespace) {
                "de.fanero.gradle.plugin" ->  {
                    useModule("de.fanero.gradle.plugin.nar:gradle-nar-plugin:0.4")
                    useVersion(requested.version)
                }
            }
        }
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
        maven {
            name = "SPONIRO"
            url = uri("http://dl.bintray.com/sponiro/gradle-plugins")
        }
    }
}

plugins {
    id("com.gradle.enterprise") version "3.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"

        if (!System.getenv("CI").isNullOrEmpty()) {
            publishAlways()
            tag("CI")
        }
    }
}

include("greeter")
//include("apache-pulsar")




