package com.github.linwancen.plugin.graph.settings

import java.util.regex.Pattern

open class AbstractDrawGraphState {
    @Transient
    var includePattern = Pattern.compile("")!!
        private set

    @Transient
    var excludePattern = Pattern.compile("(?:get|set|is)\\w")!!
        private set

    fun getInclude(): String {
        return includePattern.pattern()
    }

    fun setInclude(include: String) {
        this.includePattern = Pattern.compile(include)
    }

    fun getExclude(): String {
        return excludePattern.pattern()
    }

    fun setExclude(exclude: String) {
        this.excludePattern = Pattern.compile(exclude)
    }

    fun resetAbstract(default: AbstractDrawGraphState) {
        setInclude(default.getInclude())
        setExclude(default.getExclude())
    }
}