package org.babeloff.gradle.pulsar.api.tasks

import org.apache.log4j.LogManager
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.InputChanges
import org.gradle.internal.os.OperatingSystem
import org.example.gradle.api.pulsar.Action
import org.example.gradle.api.pulsar.Orientation


abstract class PulsarAdminTask : DefaultTask()
{

    companion object
    {
        val logger = LogManager.getLogger(PulsarAdminTask::class.java)
        val os = OperatingSystem.current()
    }

    @get:Input
    abstract val orientation : Property<Orientation>

    @get:Input
    abstract val action : Property<Action>

    @get:Input
    abstract val sourceName : Property<String>

    @get:Input
    abstract val tenant : Property<String>

    @get:Input
    abstract val namespace : Property<String>

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        println(
                if (inputChanges.isIncremental) "Executing incrementally"
                else "Executing non-incrementally"
        )
        project.exec { exec ->
            when
            {
                os.isLinux ->
                {
                    exec.workingDir("/opt/services/apache-pulsar/latest")
                    exec.commandLine("./bin/pulsar-admin",
                            orientation.get().title, action.get().title,
                            "--name", sourceName.get(),
                            "--tenant", tenant.get(),
                            "--namespace", namespace.get())
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

