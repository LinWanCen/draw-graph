package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.common.file.SysPath
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
        val paths: Array<String> = SysPath.lib() ?: return
        for (path in paths) {
            val file = File(path + "draw-graph/mermaid.js")
            if (file.exists()) {
                return
            }
        }
        val src = InstallMermaid.javaClass.getResourceAsStream("/jcef/mermaid.js") ?: return
        for (path in paths) {
            val file = File("${path}draw-graph/mermaid.js")
            try {
                file.parentFile.mkdirs()
                try {
                    file.parentFile.mkdirs()
                    Files.copy(src, file.toPath())
                    return
                } catch (e: Exception) {
                    LOG.error(
                        "can not copy mermaid.js to {} {} {}",
                        SystemUtils.OS_NAME, SystemUtils.OS_VERSION, file.absolutePath, e
                    )
                }
            } catch (e: Exception) {
                LOG.error(
                    "can not mkdirs for mermaid.js {} {} {}",
                    SystemUtils.OS_NAME, SystemUtils.OS_VERSION, file.absolutePath, e
                )
            }
        }
    }

    @JvmStatic
    fun openDir(project: Project) {
        val paths: Array<String> = SysPath.lib() ?: return
        object : Task.Backgroundable(project, "draw open dir") {
            override fun run(indicator: ProgressIndicator) {
                for (path in paths) {
                    try {
                        val commandLine = when {
                            SystemUtils.IS_OS_WINDOWS -> {
                                CommandLineUtil.toCommandLine("cmd", arrayListOf("/c", "start", "${path}draw-graph"))
                            }

                            SystemUtils.IS_OS_MAC -> {
                                CommandLineUtil.toCommandLine("open", arrayListOf("${path}draw-graph"))
                            }

                            SystemUtils.IS_OS_LINUX -> {
                                CommandLineUtil.toCommandLine("nautilus", arrayListOf("${path}draw-graph"))
                            }

                            else -> {
                                LOG.error("can not support {} {}", SystemUtils.OS_NAME, SystemUtils.OS_VERSION)
                                return
                            }
                        }
                        val generalCommandLine = GeneralCommandLine(commandLine)
                        generalCommandLine.charset = StandardCharsets.UTF_8
                        generalCommandLine.setWorkDirectory(path)
                        ScriptRunnerUtil.getProcessOutput(generalCommandLine)
                        return
                    } catch (ignore: Exception) {
                        // ignore
                    }
                }
            }
        }.queue()
    }
}