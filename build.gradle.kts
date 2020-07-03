/*
 * This project retrieves information
 * from the TRANSIT-HUB sources and
 * stuffs them into the SCOPE-LAB LEDGER.
 *
 */

plugins {
    kotlin("jvm")
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
    }
}

subprojects {
    version = "2019.10.0"
    group = "edu.vu.scopelab"

}

/** https://docs.gradle.org/current/userguide/more_about_tasks.html */
tasks {

    named<Wrapper>("wrapper") {
        gradleVersion = "6.5.1"
        distributionType = Wrapper.DistributionType.ALL
    }

}
