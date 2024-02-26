package com.github.linwancen.plugin.graph.parser.scala

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.parser.java.JavaParserUtils
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import org.jetbrains.plugins.scala.ScalaLanguage
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScReferenceExpression
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition
import org.slf4j.LoggerFactory

class ScalaParser : ParserLang<ScFunctionDefinition>() {
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("load ScalaParser")
        SERVICES[ScalaLanguage.INSTANCE.id] = this
    }

    override fun funClass(): Class<ScFunctionDefinition> {
        return ScFunctionDefinition::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: ScFunctionDefinition): Boolean {
        return JavaParserUtils.skipFun(state, func)
    }

    override fun toSign(func: ScFunctionDefinition): String {
        return "${func.containingClass?.qualifiedName ?: ""}#${func.name}"
    }

    override fun funMap(funMap: MutableMap<String, String>, func: ScFunctionDefinition) {
        JavaParserUtils.funMapWithoutDoc(func, funMap)
        ScalaComment.addDocParam(func.docComment, funMap)
    }

    override fun classMap(func: ScFunctionDefinition, relData: RelData): MutableMap<String, String>? {
        val (psiClass, classMap) = JavaParserUtils.classMapWithoutDoc(func) ?: return null
        ScalaComment.addDocParam(psiClass.docComment, classMap)
        return classMap
    }

    override fun callList(func: ScFunctionDefinition): List<ScFunctionDefinition> {
        return Call.find(func, ScReferenceExpression::class.java)
    }
}