package com.github.linwancen.plugin.graph.parser

object ParserUtils {

    /**
     * for overloading method
     * Java、Kotlin、Groovy、Scala、C++、TypeScript
     */
    fun signParams(funMap: MutableMap<String, String>): String {
        val sign = funMap["sign"] ?: return ""
        val i = sign.indexOf('(')
        if (i < 0) {
            return ""
        }
        return sign.substring(i)
    }
}