package com.github.linwancen.plugin.graph.draw

import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files

/**
 * on GraphWindowFactory
 */
object InstallMermaid {
    private val LOG = LoggerFactory.getLogger(this::class.java)

    fun checkAndInstall() {
        var paths: Array<String>? = null
        if (SystemUtils.IS_OS_WINDOWS) {
            paths = arrayOf(
                "C:/Users/Public/draw-graph/mermaid.js",
                "D:/draw-graph/mermaid.js",
            )
        } else if (SystemUtils.IS_OS_MAC) {
            paths = arrayOf(
                "/Applications/draw-graph/mermaid.js",
            )
        } else if (SystemUtils.IS_OS_LINUX) {
            paths = arrayOf(
                "/var/lib/draw-graph/mermaid.js",
                "/usr/lib/draw-graph/mermaid.js",
            )
        }
        if (paths != null) {
            copyWhenNotExists(paths)
        } else {
            LOG.error("can not support {} {}", SystemUtils.OS_NAME, SystemUtils.OS_VERSION)
        }
    }

    private fun copyWhenNotExists(paths: Array<String>) {
        for (path in paths) {
            val file = File(path)
            if (file.exists()) {
                return
            }
        }
        val src = InstallMermaid.javaClass.getResourceAsStream("/jcef/mermaid.js") ?: return
        for (path in paths) {
            val file = File(path)
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
}