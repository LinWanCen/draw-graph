package com.github.linwancen.plugin.graph.parser.java

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.slf4j.LoggerFactory

open class JavaParser : ParserLang<PsiMethod>() {
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("load JavaParser")
        SERVICES[JavaLanguage.INSTANCE.id] = this
    }

    override fun funClass(): Class<PsiMethod> {
        return PsiMethod::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: PsiMethod): Boolean {
        return JavaParserUtils.skipFun(state, func)
    }

    override fun toSign(func: PsiMethod): String? {
        val clazz = func.containingClass ?: return "#${func.name}"
        return "${clazz.qualifiedName ?: return null}#${func.name}"
    }

    override fun funMap(funMap: MutableMap<String, String>, func: PsiMethod) {
        JavaParserUtils.funMap(func, funMap)
    }

    override fun classMap(func: PsiMethod, relData: RelData): MutableMap<String, String>? {
        return JavaParserUtils.classMap(func)
    }

    override fun callList(func: PsiMethod): List<PsiMethod> {
        return Call.find(func, PsiJavaCodeReferenceElement::class.java)
    }

    override fun fileCall(
        callSetMap: MutableMap<String, MutableSet<String>>,
        usageSetMap: MutableMap<String, MutableSet<String>>,
        psiFile: PsiFile
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