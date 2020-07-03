package org.ensample.gradle.api.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.Incremental
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

open class ConsumerTask : DefaultTask() {
    @Incremental
    @PathSensitive(PathSensitivity.NAME_ONLY)
    @InputFile
    val inFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun consume() {
        val message = this.readText(inFile.get().asFile)
        System.out.printf("%s and written by the comsumer", message)
    }

    private fun readText(file: File): String {
        return FileReader(file).use { fileReader ->
            BufferedReader(fileReader).use { buffReader ->
                buffReader.readLine().trim()
            }
        }
    }
}