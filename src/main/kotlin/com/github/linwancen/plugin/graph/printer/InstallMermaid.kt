package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
import com.intellij.execution.CommandLineUtil
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * on GraphWindowFactory
 */
object InstallMermaid {
    private val LOG = LoggerFactory.getLogger(this::class.java)

    @JvmStatic
    fun checkAndInstall() {
        val src = InstallMermaid.javaClass.getResourceAsStream("/jcef/mermaid.js") ?: return
        val file = File(DrawGraphAppState.of().tempPath, "mermaid.js")
        try {
            if (!file.exists()) {
                Files.copy(src, file.toPath())
            }
        } catch (e: Exception) {
            DrawGraphAppState.of().online = true
            LOG.warn(
                "can not copy mermaid.js to {} {} {}",
                SystemUtils.OS_NAME, SystemUtils.OS_VERSION, file.absolutePath, e
            )
        }
    }

    @JvmStatic
    fun openDir(project: Project) {
        object : Task.Backgroundable(project, "draw open dir") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val tempPath = DrawGraphAppState.of().tempPath
                    val parameters = arrayListOf("/c", "start", tempPath)
                    val commandLine = CommandLineUtil.toCommandLine("cmd", parameters)
                    val generalCommandLine = GeneralCommandLine(commandLine)
                    generalCommandLine.charset = StandardCharsets.UTF_8
                    generalCommandLine.setWorkDirectory(tempPath)
                    ScriptRunnerUtil.getProcessOutput(generalCommandLine)
                    return
                } catch (_: Exception) {
                    // ignore
                }
            }
        }.queue()
    }
}