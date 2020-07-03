package org.babeloff.gradle

import org.babeloff.gradle.pulsar.api.tasks.PulsarAdminCreateTask
import org.babeloff.gradle.pulsar.api.tasks.PulsarSinkTask
import org.babeloff.gradle.pulsar.api.tasks.PulsarSourceTask
import org.junit.Assert.assertTrue
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class PulsarPluginTest {
    @Test
    fun pulsarPluginAddsCassandraTaskToProject() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply ("org.babeloff.apache-pulsar")

        assertTrue(project.tasks.getByName("createCassandra") is PulsarAdminCreateTask)
    }

    @Test
    fun pulsarPluginAddsRunSinkTaskToProject() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply ("org.babeloff.apache-pulsar")

        assertTrue(project.tasks.getByName("runSink") is PulsarSinkTask)
    }

    @Test
    fun pulsarPluginAddsRunSourceTaskToProject() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply ("org.babeloff.apache-pulsar")

        assertTrue(project.tasks.getByName("runSource") is PulsarSourceTask)
    }
}
