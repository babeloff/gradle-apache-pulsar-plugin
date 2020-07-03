package org.ensample.gradle.api.tasks

import org.apache.log4j.LogManager
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.Incremental
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.io.PrintWriter

open class GreetingTask : DefaultTask() {
    companion object {
        val logger = LogManager.getLogger(GreetingTask::class.java)
    }

    @Input
    val message: Property<String> = project.objects.property(String::class.java)

    @Input
    val recipient: Property<String> = project.objects.property(String::class.java)

    @Incremental
    @PathSensitive(PathSensitivity.NAME_ONLY)
    @InputFile
    val configFile: RegularFileProperty = project.objects.fileProperty()

    @Incremental
    @PathSensitive(PathSensitivity.NAME_ONLY)
    @InputFile
    val inFile: RegularFileProperty = project.objects.fileProperty()

    @get:OutputDirectory
    val outDir: DirectoryProperty = project.objects.directoryProperty()

    @TaskAction
    fun greet() {
        val configFile = configFile.get().asFile
        logger.info("configuration file = $configFile")

        val inFile = inFile.get().asFile
        logger.info("input file = $inFile")

        val outDir = outDir.get().asFile
        logger.info("output dir = $outDir")

        FileReader(inFile).use { fileReader ->
            BufferedReader(fileReader).use { buffReader ->
                val className = buffReader.readLine().trim()
                //val srcFile =  File(outDir, "${className}.java")

                outDir.mkdirs()

                FileWriter(inFile.absolutePath).use { fileWriter ->
                    PrintWriter(fileWriter).use { printWriter ->
                        printWriter.write("public class $className { }")
                    }
                }

            }
        }

        System.out.printf("%s, %s, %s!\n", this.message, this.recipient, this.outDir)
    }

}

