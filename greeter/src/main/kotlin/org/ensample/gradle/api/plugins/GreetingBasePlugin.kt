package org.ensample.gradle.api.plugins

import org.apache.log4j.LogManager
import org.ensample.gradle.api.extension.GreetingPluginExtension
import org.ensample.gradle.api.tasks.GreetingTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * This is the gradle ensample rewritten in Kotlin.
 *
 */
class GreetingBasePlugin : Plugin<Project>
{
    companion object {
        val logger = LogManager.getLogger(GreetingBasePlugin::class.java)
    }

    /**
     * Define capabilities.
     *
     * Provides un-opinionated functionality and general purpose concepts.
     * For example it
     * - formalized the concept of a SourceSet
     * - introduces dependency management configurations.
     *
     * However, it does not actually
     * - create tasks you’d use as a Pulsar developer on a regular basis
     * - create instances of source set
     * - establish a life-cycle / work-flow
     *
     */
    override fun apply(project: Project)
    {

    }
}

