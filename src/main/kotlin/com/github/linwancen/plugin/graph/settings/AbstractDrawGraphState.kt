package com.github.linwancen.plugin.graph.settings

import com.github.linwancen.plugin.common.psi.PsiUnSaveUtils
import java.util.*
import java.util.regex.Pattern

open class AbstractDrawGraphState {

    var skipGetSetIs = true
    var skipLib = true
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

    @Transient
    var effectIncludePattern = Pattern.compile("")!!
        private set

    @Transient
    var effectExcludePattern = Pattern.compile("Test")!!
        private set

    @Transient
    var annoDocArr = listOf(
        "io.swagger.annotations.Api#value",
        "io.swagger.annotations.Api#tags",
        "io.swagger.annotations.ApiOperation#value",
        "io.swagger.v3.oas.annotations.Operation#summary",
        "io.swagger.v3.oas.annotations.tags.Tag#name",
        "io.swagger.v3.oas.annotations.tags.Tag#description",
        Setting.message("anno_doc"),
    )
        private set

    @Transient
    var effectAnnoArr = listOf(
        "org.springframework.web.bind.annotation.RequestMapping#value",
        "org.springframework.web.bind.annotation.GetMapping#value",
        "org.springframework.web.bind.annotation.PostMapping#value",
        "org.springframework.web.bind.annotation.PutMapping#value",
        "org.springframework.web.bind.annotation.DeleteMapping#value",
        "org.springframework.web.bind.annotation.PatchMapping#value",
        Setting.message("effect_anno"),
    )
        private set

    fun getInclude(): String {
        return includePattern.pattern()
    }

    fun setInclude(s: String) {
        this.includePattern = Pattern.compile(s)
    }

    fun getExclude(): String {
        return excludePattern.pattern()
    }

    fun setExclude(s: String) {
        this.excludePattern = Pattern.compile(s)
    }

    fun getOtherInclude(): String {
        return otherIncludePattern.pattern()
    }

    fun setOtherInclude(s: String) {
        this.otherIncludePattern = Pattern.compile(s)
    }

    fun getOtherExclude(): String {
        return otherExcludePattern.pattern()
    }

    fun setOtherExclude(s: String) {
        this.otherExcludePattern = Pattern.compile(s)
    }


    fun getEffectInclude(): String {
        return effectIncludePattern.pattern()
    }

    fun setEffectInclude(s: String) {
        this.effectIncludePattern = Pattern.compile(s)
    }

    fun getEffectExclude(): String {
        return effectExcludePattern.pattern()
    }

    fun setEffectExclude(s: String) {
        this.effectExcludePattern = Pattern.compile(s)
    }

    fun getAnnoDoc(): String {
        return annoDocArr.joinToString("\n")
    }

    fun setAnnoDoc(s: String) {
        this.annoDocArr = s.split(PsiUnSaveUtils.LINE_END_PATTERN)
    }

    fun getEffectAnno(): String {
        return effectAnnoArr.joinToString("\n")
    }

    fun setEffectAnno(s: String) {
        this.effectAnnoArr = s.split(PsiUnSaveUtils.LINE_END_PATTERN)
    }

    fun resetAbstract(default: AbstractDrawGraphState) {
        setInclude(default.getInclude())
        setExclude(default.getExclude())
        setOtherInclude(default.getOtherInclude())
        setOtherExclude(default.getOtherExclude())
        setEffectInclude(default.getEffectInclude())
        setEffectExclude(default.getEffectExclude())
        setAnnoDoc(default.getAnnoDoc())
        setEffectAnno(default.getEffectAnno())
    }
}