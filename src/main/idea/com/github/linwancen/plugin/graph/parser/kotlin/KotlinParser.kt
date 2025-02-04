package com.github.linwancen.plugin.graph.parser.kotlin

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

class KotlinParser : ParserLang<KtNamedFunction>() {

    override fun id(): String {
        return KotlinLanguage.INSTANCE.id
    }

    override fun funClass(): Class<KtNamedFunction> {
        return KtNamedFunction::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: KtNamedFunction): Boolean {
        return false
    }

    override fun toSign(func: KtNamedFunction): String? {
        val classOrObject = func.containingClassOrObject ?: return "#${func.name}"
        return "${classOrObject.fqName ?: return null}#${func.name}"
    }

    override fun funMap(funMap: MutableMap<String, String>, func: KtNamedFunction) {
        val v = KotlinModifier.symbol(func)
        funMap["name"] = "$v ${func.name}"
        KotlinComment.addDocParam(func.docComment, funMap)
    }

    override fun classMap(func: KtNamedFunction, relData: RelData): MutableMap<String, String>? {
        val psiClass = func.containingClassOrObject ?: return null
        val classMap = mutableMapOf<String, String>()
        psiClass.name?.let { classMap["name"] = it }
        psiClass.fqName?.let { classMap["sign"] = it.asString() }
        KotlinComment.addDocParam(psiClass.docComment, classMap)
        return classMap
    }

    override fun callList(func: KtNamedFunction, call: Boolean): List<KtNamedFunction> {
        return Call.find(func, call, KtNameReferenceExpression::class.java)
    }
}