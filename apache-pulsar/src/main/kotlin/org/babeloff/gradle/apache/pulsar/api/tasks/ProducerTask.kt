package org.example.gradle.api.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.work.Incremental

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter


open class ProducerTask : DefaultTask()
{
    @Incremental
    @PathSensitive(PathSensitivity.NAME_ONLY)
    @InputFile
    val inFile : RegularFileProperty = project.objects.fileProperty()

    @OutputFile
    val outFile : RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun produce()
    {
        val message = "Here is a sample message! from the producer"
        val output = outFile.get().asFile
        this.writeText( output, message)
    }

    private fun writeText( file : File,  text: String)
    {
        FileWriter(file.absolutePath).use { fileWriter ->
            PrintWriter(fileWriter).use { printWriter ->
                printWriter.write(text)
            }
        }
    }
}