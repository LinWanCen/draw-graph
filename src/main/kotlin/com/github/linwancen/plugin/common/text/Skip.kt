package com.github.linwancen.plugin.common.text

import java.util.regex.Pattern

object Skip {
    fun skip(text: String, include: Pattern, exclude: Pattern): Boolean {
        return if (exclude(text, exclude)) {
            true
        } else !include(text, include)
    }

    fun include(text: String, include: Pattern): Boolean {
        return if (include.pattern().isEmpty()) {
            true
        } else include.matcher(text).find()
    }

    fun exclude(text: String, exclude: Pattern): Boolean {
        return if (exclude.pattern().isEmpty()) {
            false
        } else exclude.matcher(text).find()
    }
}