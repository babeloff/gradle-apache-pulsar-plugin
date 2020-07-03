package org.babeloff.gradle.pulsar.api.tasks

import org.apache.log4j.LogManager
import org.apache.pulsar.common.functions.FunctionConfig
import org.apache.pulsar.functions.LocalRunner
//import org.apache.pulsar.functions.LocalRunner
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
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

abstract class PulsarFunctionTask : DefaultTask()
{
    companion object
    {
        val logger = LogManager.getLogger(PulsarAdminTask::class.java)
        val os = OperatingSystem.current()
    }

    @get:Optional
    @get:Incremental
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputFile
    abstract val archive: RegularFileProperty

    @get:Optional
    @get:Input
    abstract val classname: Property<String>

    @get:Input
    abstract val functionName : Property<String>

    @get:Input
    abstract val tenant : Property<String>

    @get:Input
    abstract val namespace : Property<String>

    @get:Input
    abstract val inputTopics : ListProperty<String>

    @get:Input
    abstract val outputTopic : Property<String>

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

        val cfg = FunctionConfig.builder()
        .runtimeFlags(null)
        .tenant( tenant.get() )
        .namespace( namespace.get() )
        .name( functionName.get() )
        .className( classname.get() )
        .inputs( inputTopics.get() )
        .customSerdeInputs( null)
        .topicsPattern( null)
        .customSchemaInputs( null)

        /**
         * A generalized way of specifying inputs
         */
        cfg.inputSpecs( null)

        cfg.output( outputTopic.get() )

        /**
         * Represents either a builtin schema type (eg: 'avro', 'json', ect)
         * or the class name for a Schema implementation
         */
        cfg.outputSchemaType( "json")

        cfg.outputSerdeClassName( null)
        cfg.logTopic( null)
        cfg.processingGuarantees( null)
        cfg.retainOrdering( null)
        cfg.userConfig( configMap.get() )

        cfg.secrets( null)
        cfg.runtime( FunctionConfig.Runtime.JAVA)
        cfg.autoAck( null)
        cfg.maxMessageRetries( null)
        cfg.deadLetterTopic( null)
        cfg.subName( null)
        cfg.parallelism( 1 )
        cfg.resources( null)
        cfg.fqfn( null)
        cfg.windowConfig( null)
        cfg.timeoutMs( null)
        cfg.jar( archive.get().asFile.canonicalPath )
        cfg.py( null)
        cfg.go( null)
        // Whether the subscriptions the functions created/used should be deleted when the functions is deleted
        cfg.cleanupSubscription(null)

        val localRunner = LocalRunner
                .builder()
                .functionConfig(cfg.build())
                .build()
        try
        {
            println("running $cfg")
            localRunner.start(true)
        }
        catch (ex: Throwable) {
            println("caught a throwable ${ex.localizedMessage}")
        }
        finally {
            println("done running the process")
        }
    }

}

