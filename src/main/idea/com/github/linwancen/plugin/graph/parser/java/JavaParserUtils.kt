package com.github.linwancen.plugin.graph.parser.java

import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod

/**
 * java & scala
 */
object JavaParserUtils {
    @JvmStatic
    fun skipFun(state: DrawGraphProjectState, func: PsiMethod): Boolean {
        return (func.isConstructor && !func.hasParameters())
                || state.skipGetSetIs && GetSetIs.isGetSetIs(func, func.containingClass ?: return false)
    }

    @JvmStatic
    fun funMap(func: PsiMethod, funMap: MutableMap<String, String>) {
        funMapWithoutDoc(func, funMap)
        JavaComment.addDocParam(func.docComment, funMap)
    }

    @JvmStatic
    fun funMapWithoutDoc(func: PsiMethod, funMap: MutableMap<String, String>) {
        val v = JavaModifier.symbol(func)
        funMap["name"] = "$v ${func.name}"
    }

    @JvmStatic
    fun classMap(func: PsiMethod): MutableMap<String, String>? {
        val (psiClass, classMap) = classMapWithoutDoc(func) ?: return null
        JavaComment.addDocParam(psiClass.docComment, classMap)
        return classMap
    }

    @JvmStatic
    fun classMapWithoutDoc(func: PsiMethod): Pair<PsiClass, MutableMap<String, String>>? {
        val psiClass = func.containingClass ?: return null
        val classMap = mutableMapOf<String, String>()
        psiClass.qualifiedName?.let { classMap["sign"] = it }
        psiClass.name?.let { classMap["name"] = it }
        return Pair(psiClass, classMap)
    }
}