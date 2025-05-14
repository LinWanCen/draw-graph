package com.github.linwancen.plugin.graph.parser.java

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.search.searches.OverridingMethodsSearch
import com.intellij.psi.util.PsiTreeUtil

open class JavaParser : ParserLang<PsiMethod>() {

    override fun id(): String {
        return JavaLanguage.INSTANCE.id
    }

    override fun nameToElementImpl(project: Project, name: String): PsiElement? {
        val scope = GlobalSearchScope.allScope(project)
        if (name.contains('.')) {
            return JavaPsiFacade.getInstance(project).findClass(name, scope)
        }
        val classes = PsiShortNamesCache.getInstance(project).getClassesByName(name, scope)
        if (classes.isNotEmpty()) {
            return classes[0]
        }
        return null
    }

    override fun funClass(): Class<PsiMethod> {
        return PsiMethod::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: PsiMethod): Boolean {
        return JavaParserUtils.skipFun(state, func)
    }

    override fun toSign(func: PsiMethod): String? {
        return JavaParserUtils.sign(func)
    }

    override fun funMap(funMap: MutableMap<String, String>, func: PsiMethod) {
        JavaParserUtils.funMap(func, funMap)
    }

    override fun classMap(func: PsiMethod, relData: RelData): MutableMap<String, String>? {
        return JavaParserUtils.classMap(func)
    }

    override fun callList(func: PsiMethod, call: Boolean): List<PsiMethod> {
        val find = Call.find(func, call, PsiJavaCodeReferenceElement::class.java)
        val path = func.containingFile?.virtualFile?.path ?: return find
        if (path.contains('!')) {
            // not project fun
            return find
        }
        val scope = GlobalSearchScope.projectScope(func.project)
        val override = if (call) {
            OverridingMethodsSearch.search(func, scope, true).filterNotNull()
        } else {
            func.findSuperMethods().toList()
        }
        if (override.isEmpty()) {
            return find
        }
        val list = mutableListOf<PsiMethod>()
        list.addAll(find)
        list.addAll(override)
        return list
    }

    override fun fileCall(
        callSetMap: MutableMap<String, MutableSet<String>>,
        usageSetMap: MutableMap<String, MutableSet<String>>,
        psiFile: PsiFile,
    ) {
        if (!DrawGraphAppState.of().impl) {
            return
        }
        val psiClasses = PsiTreeUtil.findChildrenOfType(psiFile, PsiClass::class.java)
        for (psiClass in psiClasses) {
            val interfaces = psiClass.interfaces
            if (interfaces.isNotEmpty()) {
                val map = mutableMapOf<String, String>()
                for (method in psiClass.methods) {
                    map[method.name] = toSign(method) ?: continue
                }
                for (clazz in interfaces) {
                    for (method in clazz.methods) {
                        val implSign = map[method.name]
                        if (implSign != null) {
                            val sign = toSign(method) ?: continue
                            val list = usageSetMap.computeIfAbsent(implSign) { mutableSetOf() }
                            list.add(sign)
                        }
                    }
                }
            }
        }
    }
}