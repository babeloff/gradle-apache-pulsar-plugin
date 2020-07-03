package org.babeloff.gradle.pulsar.api.tasks

import org.apache.log4j.LogManager
import org.apache.pulsar.common.io.SourceConfig
import org.apache.pulsar.functions.LocalRunner
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import org.gradle.work.Incremental
import org.gradle.work.InputChanges

abstract class PulsarSourceTask : DefaultTask()
{
    companion object
    {
        val logger = LogManager.getLogger(PulsarAdminTask::class.java)
        val os = OperatingSystem.current()
    }

    @get:Optional
    @get:Input
    abstract val classname: Property<String>

    @get:Optional
    @get:Incremental
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputFile
    abstract val archive: RegularFileProperty

    @get:Input
    abstract val sourceName : Property<String>

    @get:Input
    abstract val tenant : Property<String>

    @get:Input
    abstract val namespace : Property<String>

    @get:Input
    abstract val topicName : Property<String>

    @get:Input
    abstract val configMap : MapProperty<String, Any>

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        println(
                if (inputChanges.isIncremental)
                    "Executing incrementally"
                else
                    "Executing non-incrementally"
        )
        System.setProperty("PULSAR_HOME", "/opt/services/apache-pulsar/latest")

        val cfg = SourceConfig.builder()
        .archive(archive.get().asFile.canonicalPath)
        .className(classname.get())
        .name(sourceName.get())
        .tenant(tenant.get())
        .namespace(namespace.get())
        .topicName(topicName.get())
        .configs(configMap.get().orEmpty())
        .parallelism(1)

        val config = cfg.build()
        val localRunner = LocalRunner
                .builder()
                .sourceConfig(config)
                .build()
        try
        {
            println("running ${config.name}")
            localRunner.start(true)
        }
        catch (ex: Throwable) {
            println("caught a thowable ${ex.localizedMessage}")
        } finally {
            println("done running the process")
        }
    }

}

