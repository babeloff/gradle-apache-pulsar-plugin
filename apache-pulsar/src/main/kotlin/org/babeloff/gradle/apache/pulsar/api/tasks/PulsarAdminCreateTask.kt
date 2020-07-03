package org.example.gradle.api.tasks

import org.apache.log4j.LogManager
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.gradle.internal.os.OperatingSystem

import org.example.gradle.api.pulsar.Orientation
import org.example.gradle.api.pulsar.Action

abstract class PulsarAdminCreateTask : DefaultTask()
{

    companion object
    {
        val logger = LogManager.getLogger(PulsarAdminCreateTask::class.java)
        val os = OperatingSystem.current()
    }

    @get:Input
    abstract val orientation : Property<Orientation>

    @get:Optional
    @get:Input
    abstract val type: Property<String>

    @get:Optional
    @get:Input
    abstract val classname: Property<String>

    @get:Optional
    @get:Incremental
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputFile
    abstract val archive: RegularFileProperty

    @get:Incremental
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputFile
    abstract val configFile : RegularFileProperty

    @get:Input
    abstract val sourceName : Property<String>

    @get:Input
    abstract val tenant : Property<String>

    @get:Input
    abstract val namespace : Property<String>

    @get:Input
    abstract val topicName : Property<String>

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        println(
                if (inputChanges.isIncremental) "Executing incrementally"
                else "Executing non-incrementally"
        )
        val configFileKey = when (orientation.get()) {
            Orientation.SOURCE -> "--source-config-file"
            Orientation.SINK -> "--sink-config-file"
            else -> "not a create orientation ${orientation}"
        }

        val typeKey = when (orientation.get()) {
            Orientation.SOURCE -> "--source-type"
            Orientation.SINK -> "--sink-type"
            else -> "not a create orientation ${orientation}"
        }

        val topicKey = when (orientation.get()) {
            Orientation.SOURCE -> "--destination-topic-name"
            Orientation.SINK -> "--inputs"
            else -> "not a create orientation ${orientation}"
        }
        project.exec {
            when
            {
                os.isLinux ->
                {
                    workingDir("/opt/services/apache-pulsar/latest")
                    if (type.isPresent)
                    {
                        commandLine("./bin/pulsar-admin",
                                orientation.get().title, "create",
                                typeKey, type.get(),
                                "--name", sourceName.get(),
                                "--tenant", tenant.get(),
                                "--namespace", namespace.get(),
                                topicKey, topicName.get(),
                                configFileKey, configFile.get())
                    }
                    else
                    {
                        commandLine("./bin/pulsar-admin",
                                orientation.get().title, "create",
                                "--classname", classname.get(),
                                "--archive", archive.get(),
                                "--name", sourceName.get(),
                                "--tenant", tenant.get(),
                                "--namespace", namespace.get(),
                                topicKey, topicName.get(),
                                configFileKey, configFile.get())
                    }
                }
                os.isWindows ->
                {
                    println("TODDO: not implemented")
                }
                else ->
                {
                    println("TODDO: ${os.familyName} not implemented")
                }
            }
        }

    }

}

