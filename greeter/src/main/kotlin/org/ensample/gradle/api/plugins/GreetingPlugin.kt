package org.ensample.gradle.api.plugins

import org.apache.log4j.LogManager
import org.ensample.gradle.api.extension.GreetingPluginExtension
import org.ensample.gradle.api.tasks.GreetingTask
//import org.ensample.gradle.api.tasks.GreetingToFileTask

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * This is the gradle ensample rewritten in Kotlin.
 *
 */
class GreetingPlugin : Plugin<Project>
{
    companion object {
        val logger = LogManager.getLogger(GreetingPlugin::class.java)
        const val EVAL_GROUP = "greetingPlugin";
        const val EXTENSION_NAME = "greeting";
    }

    /**
     * Define conventions
     *
     * Conventions include
     * - predefined tasks
     * - things set in the plugin extensions
     * - source sets
     *
     * Tasks
     * -
     * -
     *
     * Source Sets
     * -
     *
     * Lifecycle
     */
    override fun apply(project: Project)
    {
        project.plugins.apply(GreetingBasePlugin::class.java)

        val extension = project
                .extensions
                .create(EXTENSION_NAME, GreetingPluginExtension::class.java)

        val miscDir = File("misc/test_resources")

        project.tasks.register("goodbye", GreetingTask::class.java) { task ->
            task.group = EVAL_GROUP

            task.doFirst {
                System.out.printf("primero-bye %s, %s!\n", extension.message, extension.recipient)
            }
            task.doLast {
                System.out.printf("ultimo-bye %s, %s!\n", extension.message, extension.recipient)
            }
        }

        project.tasks.register("hello", GreetingTask::class.java) { task ->
            task.group = EVAL_GROUP
            task.message.set(extension.message)
            task.recipient.set(extension.recipient)

            task.inFile.set(File(miscDir, "hello_infile.foo"))

            task.doFirst {
                println("first");
            }
            task.doLast {
                println("last");
            }
        }
//
//        project.tasks.register("tellFile", GreetingToFileTask::class.java) { task ->
//            task.group = EVAL_GROUP
//
//            task.doFirst {
//                println("first");
//            }
//            task.doLast {
//                println("last");
//            }
//        }
    }
}

