package com.github.linwancen.plugin.common.file

import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory

object SysPath {
    private val LOG = LoggerFactory.getLogger(this::class.java)

    @JvmStatic
    fun lib(): Array<String>? {
        return when {
            SystemUtils.IS_OS_WINDOWS -> {
                arrayOf(
                    "D:/",
                    "C:/Users/Public/",
                )
            }

            SystemUtils.IS_OS_MAC -> {
                arrayOf(
                    "/Applications/",
                )
            }

            SystemUtils.IS_OS_LINUX -> {
                arrayOf(
                    "/var/lib/",
                    "/usr/lib/",
                )
            }

            else -> {
                LOG.error("can not support {} {}", SystemUtils.OS_NAME, SystemUtils.OS_VERSION)
                null
            }
        }
    }
}