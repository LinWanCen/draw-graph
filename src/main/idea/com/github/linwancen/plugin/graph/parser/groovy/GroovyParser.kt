package com.github.linwancen.plugin.graph.parser.groovy

import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.parser.java.JavaParserUtils
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import org.jetbrains.plugins.groovy.GroovyLanguage
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod

class GroovyParser : ParserLang<GrMethod>() {

    override fun id(): String {
        return GroovyLanguage.id
    }

    override fun funClass(): Class<GrMethod> {
        return GrMethod::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: GrMethod): Boolean {
        return JavaParserUtils.skipFun(state, func)
    }

    override fun toSign(func: GrMethod): String? {
        return JavaParserUtils.sign(func)
    }

    override fun funMap(funMap: MutableMap<String, String>, func: GrMethod) {
        JavaParserUtils.funMap(func, funMap)
    }

    override fun classMap(func: GrMethod, relData: RelData): MutableMap<String, String>? {
        return JavaParserUtils.classMap(func)
    }

    override fun callList(func: GrMethod, call: Boolean): List<GrMethod> {
        return Call.find(func, call, GrReferenceExpression::class.java)
    }
}