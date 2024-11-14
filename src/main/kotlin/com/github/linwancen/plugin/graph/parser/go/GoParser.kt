package com.github.linwancen.plugin.graph.parser.go

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.goide.GoLanguage
import com.goide.psi.GoFunctionDeclaration
import com.goide.psi.GoReferenceExpression

class GoParser : ParserLang<GoFunctionDeclaration>() {

    override fun id(): String {
        return GoLanguage.INSTANCE.id
    }

    override fun funClass(): Class<GoFunctionDeclaration> {
        return GoFunctionDeclaration::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: GoFunctionDeclaration): Boolean {
        return false
    }

    override fun toSign(func: GoFunctionDeclaration): String {
        return "${func.qualifiedName}"
    }

    override fun funMap(funMap: MutableMap<String, String>, func: GoFunctionDeclaration) {
        funMap["name"] = "${func.name}"
        GoComment.addDocParam(func, funMap)
    }

    override fun classMap(func: GoFunctionDeclaration, relData: RelData): MutableMap<String, String>? {
        val psiClass = func.containingFile.`package` ?: return null
        val funcSign = func.qualifiedName ?: return null
        val classMap = mutableMapOf<String, String>()
        psiClass.name?.let { classMap["name"] = it }
        classMap["sign"] = funcSign.substring(0, funcSign.lastIndexOf("."))
        GoComment.addDocParam(psiClass, classMap)
        return classMap
    }

    override fun callList(func: GoFunctionDeclaration, call: Boolean): List<GoFunctionDeclaration> {
        return Call.find(func, call, GoReferenceExpression::class.java)
    }
}