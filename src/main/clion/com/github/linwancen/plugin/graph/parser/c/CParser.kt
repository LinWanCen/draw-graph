package com.github.linwancen.plugin.graph.parser.c

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.CommentUtils
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.cidr.lang.OCLanguage
import com.jetbrains.cidr.lang.psi.OCDeclarator
import com.jetbrains.cidr.lang.psi.OCFunctionDefinition
import com.jetbrains.cidr.lang.psi.OCReferenceElement

class CParser : ParserLang<OCFunctionDefinition>() {

    override fun id(): String {
        return OCLanguage.getInstance().id
    }

    override fun funClass(): Class<OCFunctionDefinition> {
        return OCFunctionDefinition::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: OCFunctionDefinition): Boolean {
        return false
    }

    override fun toSign(func: OCFunctionDefinition): String {
        return "${func.namespaceQualifier?.name ?: ""}#${func.name}"
    }

    override fun funMap(funMap: MutableMap<String, String>, func: OCFunctionDefinition) {
        val v = CModifier.symbol(func)
        funMap["name"] = "$v ${func.name}"
        CommentUtils.childComment(func, funMap)
    }

    override fun classMap(func: OCFunctionDefinition, relData: RelData): MutableMap<String, String>? {
        val psiClass = func.namespaceQualifier ?: return null
        val classMap = mutableMapOf<String, String>()
        psiClass.name?.let { classMap["sign"] = it }
        psiClass.name?.let { classMap["name"] = it }
        return classMap
    }

    override fun callList(func: OCFunctionDefinition, call: Boolean): List<OCFunctionDefinition> {
        return if (call) Call.findRefs(PsiTreeUtil.findChildrenOfAnyType(func, OCReferenceElement::class.java))
            .filterIsInstance<OCDeclarator>()
            .map { it.parent }
            .filterIsInstance<OCFunctionDefinition>()
              else Call.find(func, false, funClass())
    }
}