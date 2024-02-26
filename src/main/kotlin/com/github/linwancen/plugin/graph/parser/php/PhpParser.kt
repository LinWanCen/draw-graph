package com.github.linwancen.plugin.graph.parser.php

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.jetbrains.php.lang.PhpLanguage
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpReference
import org.slf4j.LoggerFactory

class PhpParser : ParserLang<Function>() {
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("load PhpParser")
        SERVICES[PhpLanguage.INSTANCE.id] = this
    }

    override fun funClass(): Class<Function> {
        return Function::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: Function): Boolean {
        return func.name.startsWith("__")
    }

    override fun toSign(func: Function): String {
        return func.fqn
    }

    override fun funMap(funMap: MutableMap<String, String>, func: Function) {
        val v = PhpModifier.symbol(func)
        funMap["name"] = "$v ${func.name}"
        PhpComment.addDocParam(func.docComment, funMap)
    }

    override fun classMap(func: Function, relData: RelData): MutableMap<String, String>? {
        val classMap = mutableMapOf<String, String>()
        if (func is Method) {
            func.containingClass?.let {
                classMap["sign"] = it.fqn
                classMap["name"] = it.name
                PhpComment.addDocParam(it.docComment, classMap)
                return classMap
            }
        }
        if (func.namespaceName == "\\") {
            return null
        }
        classMap["sign"] = func.namespaceName
        classMap["name"] = func.namespaceName
        return classMap
    }

    override fun callList(func: Function): List<Function> {
        return Call.find(func, PhpReference::class.java)
    }
}