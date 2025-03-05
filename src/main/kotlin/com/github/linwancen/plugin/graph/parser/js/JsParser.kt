package com.github.linwancen.plugin.graph.parser.js

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSReferenceExpression

open class JsParser : ParserLang<JSFunction>() {

    override fun id(): String {
        return JavascriptLanguage.INSTANCE.id
    }

    override fun funClass(): Class<JSFunction> {
        return JSFunction::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: JSFunction): Boolean {
        if (func.isConstructor && func.parameters.isEmpty()) {
            return true
        }
        if (!state.skipGetSetIs) {
            return false
        }
        val name = func.name ?: return false
        return name.startsWith("get") || name.startsWith("get")
    }

    override fun toSign(func: JSFunction): String {
        return func.qualifiedName.toString()
    }

    override fun funMap(funMap: MutableMap<String, String>, func: JSFunction) {
        val v = JsModifier.symbol(func)
        funMap["name"] = "$v ${func.name}"
        JsComment.addDocParam(func, funMap)
    }

    override fun classMap(func: JSFunction, relData: RelData): MutableMap<String, String>? {
        val psiClass = func.namespace ?: return null
        val classMap = mutableMapOf<String, String>()
        psiClass.name.let { classMap["name"] = it }
        psiClass.qualifiedName.let { classMap["sign"] = it }
        return classMap
    }

    override fun callList(func: JSFunction, call: Boolean): List<JSFunction> {
        // not support JSProperty fun yet
        return Call.find(func, call, JSReferenceExpression::class.java)
    }
}