package org.example.gradle.api.tasks

import org.apache.log4j.LogManager
import org.example.gradle.api.pulsar.Action
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import org.gradle.work.InputChanges


abstract class PulsarAdminFunctionTask : DefaultTask()
{
    companion object
    {
        val logger = LogManager.getLogger(PulsarAdminTask::class.java)
        val os = OperatingSystem.current()
    }

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
        project.exec {
            when
            {
                os.isLinux ->
                {
                    workingDir("/opt/services/apache-pulsar/latest")
                    commandLine("./bin/pulsar-admin",
                            "functions", action.get().title,
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

