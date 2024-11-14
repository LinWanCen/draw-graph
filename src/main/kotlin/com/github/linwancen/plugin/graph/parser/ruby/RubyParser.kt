package com.github.linwancen.plugin.graph.parser.ruby

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RDotReference
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier

class RubyParser : ParserLang<RMethod>() {

    override fun id(): String {
        return RubyLanguage.INSTANCE.id
    }

    override fun funClass(): Class<RMethod> {
        return RMethod::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: RMethod): Boolean {
        return func.isConstructor && func.arguments.isEmpty()
    }

    override fun toSign(func: RMethod): String? {
        PsiTreeUtil.getParentOfType(func, RClass::class.java)?.let {
            val qualifiedName = it.qualifiedName ?: return null
            return "${qualifiedName}#${func.name}"
        }
        return "#${func.name}"
    }

    override fun funMap(funMap: MutableMap<String, String>, func: RMethod) {
        val v = RubyModifier.symbol(func)
        funMap["name"] = "$v ${func.name}"
    }

    override fun classMap(func: RMethod, relData: RelData): MutableMap<String, String>? {
        val psiClass = PsiTreeUtil.getParentOfType(func, RClass::class.java) ?: return null
        val classMap = mutableMapOf<String, String>()
        classMap["sign"] = psiClass.qualifiedName.toString()
        classMap["name"] = psiClass.name
        return classMap
    }

    override fun callList(func: RMethod, call: Boolean): List<RMethod> {
        return Call.find(func, call, RDotReference::class.java, RIdentifier::class.java)
    }
}