package com.github.linwancen.plugin.graph.parser.rust

import org.rust.lang.core.psi.RsFunction

object RustModifier {
    /**
     * + public
     * <br>A abstract
     */
    fun symbol(func: RsFunction): String {
        val sb = StringBuilder()
        if (func.isPublic) {
            sb.append("+")
        }
        if (func.isAbstract) {
            sb.append("A")
        }
        return sb.toString()
    }
}
