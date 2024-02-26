package com.github.linwancen.plugin.graph.parser.python

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.jetbrains.python.PythonLanguage
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyReferenceExpression
import org.slf4j.LoggerFactory

class PythonParser : ParserLang<PyFunction>() {
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("load PythonParser")
        SERVICES[PythonLanguage.INSTANCE.id] = this
    }

    override fun funClass(): Class<PyFunction> {
        return PyFunction::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: PyFunction): Boolean {
        return "__init__" == func.name
    }

    override fun toSign(func: PyFunction): String {
        return "${func.containingClass?.qualifiedName ?: ""}#${func.name}"
    }

    override fun funMap(funMap: MutableMap<String, String>, func: PyFunction) {
        val v = PythonModifier.symbol(func)
        funMap["name"] = "$v ${func.name}"
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

    override fun callList(func: PyFunction): List<PyFunction> {
        return Call.find(func, PyReferenceExpression::class.java)
    }
}