package com.github.linwancen.plugin.graph.draw

import org.apache.commons.lang3.SystemUtils
import java.io.File
import java.nio.file.Files

/**
 * on GraphWindowFactory
 */
object InstallMermaid {
    fun checkAndInstall() {
        if (SystemUtils.IS_OS_WINDOWS) {
            val paths = arrayOf(
                "D:/Program Files/mermaid.js",
                "C:/Program Files/mermaid.js",
            )
            copyWhenNotExists(paths)
        } else if (SystemUtils.IS_OS_MAC) {
            val paths = arrayOf(
                "/Applications/mermaid.js",
            )
            copyWhenNotExists(paths)
        } else if (SystemUtils.IS_OS_LINUX) {
            val paths = arrayOf(
                "/var/lib/mermaid.js",
                "/usr/lib/mermaid.js",
            )
            copyWhenNotExists(paths)
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
            if (file.parentFile.exists()) {
                Files.copy(src, file.toPath())
                return
            }
        }
    }
}