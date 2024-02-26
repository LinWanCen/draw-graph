package com.github.linwancen.plugin.graph.parser.java

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiMethod
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

    override fun toSign(func: PsiMethod): String {
        return "${func.containingClass?.qualifiedName ?: ""}#${func.name}"
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
}