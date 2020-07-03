package org.babeloff.gradle.pulsar.api.plugins

import org.apache.log4j.LogManager
import org.babeloff.gradle.apache.pulsar.api.extension.PulsarPluginExtension
import org.babeloff.gradle.pulsar.api.tasks.PulsarAdminCreateTask
import org.babeloff.gradle.pulsar.api.tasks.PulsarSinkTask
import org.babeloff.gradle.pulsar.api.tasks.PulsarSourceTask
import org.example.gradle.api.pulsar.Orientation
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class PulsarPlugin : Plugin<Project> {
    companion object {
        val logger = LogManager.getLogger(PulsarPlugin::class.java)
        const val EVAL_GROUP = "pulsarPlugin"
        const val EXTENSION_NAME = "pulsar"
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
    override fun apply(project: Project) {

        val extension = project
                .extensions
                .create(EXTENSION_NAME, PulsarPluginExtension::class.java)

//        val miscDir = File("misc/test_resources")

        project.tasks.register("sourceCreate", PulsarAdminCreateTask::class.java) { task ->
            task.group = EVAL_GROUP
            task.tenant.set(extension.tenant)
            task.namespace.set(extension.namespace)
        }

        val cassandraSinkName = "cassandra-test-sink"

        project.tasks.register("createCassandra", PulsarAdminCreateTask::class.java) { task ->
            task.group = EVAL_GROUP
            task.tenant.set(extension.tenant)
            task.namespace.set(extension.namespace)

            task.dependsOn("processTestResources")

            task.group = "pulsar-io-cassandra"
//        task.classname.set("org.apache.pulsar.io.cassandra.CassandraStringSink")
//        task.archive.set(cassandraArchive)
            task.type.set("cassandra")
            task.orientation.set(Orientation.SINK)
            task.sourceName.set(cassandraSinkName)
            task.tenant.set("public")
            task.namespace.set("default")
            task.topicName.set("test_cassandra")
        }

        project.tasks.register("runSink", PulsarSinkTask::class.java) { task ->
            task.group = EVAL_GROUP
            task.tenant.set(extension.tenant)
            task.namespace.set(extension.namespace)

            task.dependsOn("processTestResources")

            task.group = "pulsar-io-cassandra"
            task.tenant.set("public")
            task.namespace.set("default")
            task.topicName.set("test_cassandra")
        }

        project.tasks.register("runSource", PulsarSourceTask::class.java) { task ->
            task.group = EVAL_GROUP
            task.tenant.set(extension.tenant)
            task.namespace.set(extension.namespace)

            task.dependsOn("processTestResources")

            task.group = "pulsar-io-cassandra"
            task.tenant.set("public")
            task.namespace.set("default")
            task.topicName.set("test_cassandra")
        }
    }
}

