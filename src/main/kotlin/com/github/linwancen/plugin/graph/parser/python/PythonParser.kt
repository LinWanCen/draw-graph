package com.github.linwancen.plugin.graph.parser.python

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.jetbrains.python.PythonLanguage
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyReferenceExpression

class PythonParser : ParserLang<PyFunction>() {

    override fun id(): String {
        return PythonLanguage.INSTANCE.id
    }

    override fun funClass(): Class<PyFunction> {
        return PyFunction::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: PyFunction): Boolean {
        return "__init__" == func.name
    }

    override fun toSign(func: PyFunction): String? {
        return func.qualifiedName
    }

    override fun funMap(funMap: MutableMap<String, String>, func: PyFunction) {
        funMap["name"] = "${func.name}"
        PythonComment.addDocParam(func.structuredDocString, funMap)
    }

    override fun classMap(func: PyFunction, relData: RelData): MutableMap<String, String>? {
        val psiClass = func.containingClass ?: return null
        val classMap = mutableMapOf<String, String>()
        psiClass.name?.let { classMap["name"] = it }
        psiClass.qualifiedName?.let { classMap["sign"] = it }
        PythonComment.addDocParam(psiClass.structuredDocString, classMap)
        return classMap
    }

    override fun callList(func: PyFunction, call: Boolean): List<PyFunction> {
        return Call.find(func, call, PyReferenceExpression::class.java)
    }
}