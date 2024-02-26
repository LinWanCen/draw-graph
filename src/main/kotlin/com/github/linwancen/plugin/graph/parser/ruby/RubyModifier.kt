package com.github.linwancen.plugin.graph.parser.ruby

import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod

object RubyModifier {
    /**
     * C constructor x deprecated
     */
    fun symbol(func: RMethod): String {
        val sb = StringBuilder()
        if (func.isConstructor) {
            sb.append("C")
        }
        if (func.isDeprecated) {
            sb.append("x")
        }
        return sb.toString()
    }
}
