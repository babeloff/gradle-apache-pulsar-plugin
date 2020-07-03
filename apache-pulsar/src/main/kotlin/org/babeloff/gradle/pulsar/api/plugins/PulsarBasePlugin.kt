package org.babeloff.gradle.pulsar.api.plugins

import org.apache.log4j.LogManager
import org.gradle.api.Plugin
import org.gradle.api.Project

class PulsarBasePlugin : Plugin<Project>
{
    companion object {
        val logger = LogManager.getLogger(PulsarBasePlugin::class.java)
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
    override fun apply(project: Project) {

    }
}

