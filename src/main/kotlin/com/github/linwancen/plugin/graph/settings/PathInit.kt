package com.github.linwancen.plugin.graph.settings

import org.apache.commons.lang3.SystemUtils
import java.io.File

object PathInit {

    val path = when {
        SystemUtils.IS_OS_WINDOWS -> validPath("temp_path_windows")
        SystemUtils.IS_OS_MAC -> validPath("temp_path_mac")
        SystemUtils.IS_OS_LINUX -> validPath("temp_path_linux")
        else -> validPath("temp_path_linux")
    }

    private fun validPath(pathKey: String): String? {
        val paths = Setting.message(pathKey).split(';')
        for (path in paths) {
            val file = File(path, "draw-graph")

            try {
                file.mkdirs()
            } catch (_: Exception) {
            }

            if (file.exists()) {
                return path
            }
        }
        return null
    }
}