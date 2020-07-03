package org.example.gradle.api.plugins

import org.babeloff.gradle.apache.pulsar.api.extension.PulsarExtension
import org.example.gradle.api.tasks.PulsarAdminCreateTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class PulsarPlugin : Plugin<Project>
{
    companion object {
        val EVAL_GROUP = "examplePlugin"; // BasePlugin.BUILD_GROUP;
        val EXTENSION_NAME = "example";
    }

    override fun apply(project: Project) {

        val extension = project
                .convention
                .create("Pulsar", PulsarExtension::class.java)

        val tasks = project.tasks

        tasks.register("sourceCreate", PulsarAdminCreateTask::class.java)  {
            group = EVAL_GROUP
            tenant.set(extension.tenant)
            namespace.set(extension.namespace)

            doFirst {
                System.out.println("first");
            }
            doLast {
                System.out.println("last");
            }
        }
    }
}

