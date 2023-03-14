package com.github.linwancen.plugin.common.text

import java.util.regex.Pattern

object DocText {

    private val pattern: Pattern = Pattern.compile("<[^>]++>")

    fun addHtmlText(s: String?, vararg builders: StringBuilder): String? {
        val deleteHtml: String = pattern.matcher(s ?: return null).replaceAll("").trim()
        if (deleteHtml.isNotEmpty()) {
            builders.forEach { builder -> builder.append(deleteHtml).append(" ") }
        }
        return deleteHtml
    }
}