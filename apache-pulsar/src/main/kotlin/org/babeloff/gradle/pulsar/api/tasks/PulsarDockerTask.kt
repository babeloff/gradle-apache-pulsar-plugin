package org.babeloff.gradle.pulsar.api.tasks

import org.apache.log4j.LogManager
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.work.InputChanges
import org.gradle.internal.os.OperatingSystem

abstract class PulsarDockerTask : DefaultTask()
{
    companion object
    {
        val logger = LogManager.getLogger(PulsarAdminTask::class.java)
        val os = OperatingSystem.current()
    }

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        println(
                if (inputChanges.isIncremental) "Executing incrementally"
                else "Executing non-incrementally"
        )
        project.exec {exec ->
            when
            {
                os.isLinux ->
                {
                    exec.commandLine("c:\\Program Files\\Docker\\Docker\\resources\\bin\\docker.exe",
                        "run", // -it
                        "-p", "6650:6650",
                        "-p", "8080:8080",
                        "-v", "\"c:/users/fredr/data:/pulsar/data\".ToLower()",
                        "apachepulsar/pulsar:2.4.2",
                        "bin/pulsar", "standalone")
                }
                os.isWindows ->
                {
                    exec.commandLine("c:\\Program Files\\Docker\\Docker\\resources\\bin\\docker.exe",
                            "run", // -it
                            "-p", "6650:6650",
                            "-p", "8080:8080",
                            "-v", "\"c:/users/fredr/data:/pulsar/data\".ToLower()",
                            "apachepulsar/pulsar:2.4.2",
                            "bin/pulsar", "standalone")
                }
                else ->
                {
                    println("TODDO: ${os.familyName} not implemented")
                }
            }
        }

    }

}

