package com.github.linwancen.plugin.graph.parser.groovy

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.parser.java.JavaParserUtils
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import org.jetbrains.plugins.groovy.GroovyLanguage
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod
import org.slf4j.LoggerFactory

class GroovyParser : ParserLang<GrMethod>() {
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("load GroovyParser")
        SERVICES[GroovyLanguage.id] = this
    }

    override fun funClass(): Class<GrMethod> {
        return GrMethod::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: GrMethod): Boolean {
        return JavaParserUtils.skipFun(state, func)
    }

    override fun toSign(func: GrMethod): String? {
        val clazz = func.containingClass ?: return "#${func.name}"
        return "${clazz.qualifiedName ?: return null}#${func.name}"
    }

    override fun funMap(funMap: MutableMap<String, String>, func: GrMethod) {
        JavaParserUtils.funMap(func, funMap)
    }

    override fun classMap(func: GrMethod, relData: RelData): MutableMap<String, String>? {
        return JavaParserUtils.classMap(func)
    }

    override fun callList(func: GrMethod): List<GrMethod> {
        return Call.find(func, GrReferenceExpression::class.java)
    }
}