package com.github.linwancen.plugin.graph.parser.java

import com.github.linwancen.plugin.graph.parser.ParserUtils
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.javadoc.PsiDocComment

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
        val docComment = supperMethodDoc(func)
        JavaComment.addDocParam(docComment, funMap)
    }

    @JvmStatic
    fun supperMethodDoc(psiMethod: PsiMethod): PsiDocComment? {
        val docComment = psiMethod.docComment
        if (docComment != null) {
            return docComment
        }
        val superMethods: Array<PsiMethod>
        try {
            superMethods = psiMethod.findSuperMethods()
        } catch (e: Exception) {
            return null
        }
        for (superMethod in superMethods) {
            // .class
            val navElement: PsiElement
            try {
                navElement = superMethod.navigationElement
            } catch (e: Exception) {
                continue
            }
            if (navElement is PsiMethod) {
                val superDoc = navElement.docComment
                if (superDoc != null) {
                    return superDoc
                }
            }
        }
        return null
    }

    @JvmStatic
    fun sign(func: PsiMethod): String? {
        val clazz = func.containingClass ?: return "#${func.name}"
        return "${clazz.qualifiedName ?: return null}#${func.name}${params(clazz, func)}"
    }

    @JvmStatic
    fun params(clazz: PsiClass, func: PsiMethod): String {
        val its = clazz.findMethodsByName(func.name, false)
        if (its.size == 1) {
            return ""
        }
        return func.parameterList.parameters.joinToString(prefix = "(", separator = ",", postfix = ")") {
            it.type.toString().substringAfterLast(':')
        }
    }

    @JvmStatic
    fun funMapWithoutDoc(func: PsiMethod, funMap: MutableMap<String, String>) {
        val v = JavaModifier.symbol(func)
        funMap["name"] = "$v ${func.name}${ParserUtils.signParams(funMap)}"
        JavaAnno.addAnno(func, funMap)
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
        JavaAnno.addAnno(psiClass, classMap)
        return Pair(psiClass, classMap)
    }
}