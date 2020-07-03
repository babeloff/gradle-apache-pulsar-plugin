package org.ensample.gradle

import org.junit.Assert.assertTrue
import org.ensample.gradle.api.tasks.GreetingTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class GreetingPluginTest {
    @Test
    fun greeterPluginAddsGreetingTaskToProject() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply ("org.ensample.greeting")

        assertTrue(project.tasks.getByName("hello") is GreetingTask)
    }
}

