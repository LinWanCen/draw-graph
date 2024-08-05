package com.github.linwancen.plugin.graph.settings

import java.util.*
import java.util.regex.Pattern

open class AbstractDrawGraphState {

    var skipGetSetIs = true
    var skipJar = true
    var lr = true
    var doc = "en" != Locale.getDefault().language
    var impl = true
    var mvc = true

    @Transient
    var includePattern = Pattern.compile("")!!
        private set

    @Transient
    var excludePattern = Pattern.compile("^(java)\\.")!!
        private set

    @Transient
    var otherIncludePattern = Pattern.compile("")!!
        private set

    @Transient
    var otherExcludePattern = Pattern.compile("")!!
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

    fun getOtherInclude(): String {
        return otherIncludePattern.pattern()
    }

    fun setOtherInclude(include: String) {
        this.otherIncludePattern = Pattern.compile(include)
    }

    fun getOtherExclude(): String {
        return otherExcludePattern.pattern()
    }

    fun setOtherExclude(exclude: String) {
        this.otherExcludePattern = Pattern.compile(exclude)
    }

    fun resetAbstract(default: AbstractDrawGraphState) {
        setInclude(default.getInclude())
        setExclude(default.getExclude())
        setOtherInclude(default.getOtherInclude())
        setOtherExclude(default.getOtherExclude())
    }
}