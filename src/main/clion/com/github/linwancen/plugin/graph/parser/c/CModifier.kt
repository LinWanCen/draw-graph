package com.github.linwancen.plugin.graph.parser.c

import com.jetbrains.cidr.lang.psi.OCFunctionDefinition

/**
 * func.is*
 */
object CModifier {
    /**
     * S static
     */
    fun symbol(func: OCFunctionDefinition): String {
        val sb = StringBuilder()
        if (func.isStatic) {
            sb.append("S")
        }
        return sb.toString()
    }
}
