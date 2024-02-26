package com.github.linwancen.plugin.graph.parser.php

import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.Method

object PhpModifier {
    /**
     * + public - private # protected ~ package private(default)
     * S static A abstract F final D dynamic
     * [PlantUML Class Diagram](https://plantuml.com/en/class-diagram)
     */
    fun symbol(func: Function): String {
        if (func !is Method) {
            return ""
        }
        val sb = StringBuilder()
        val modifier = func.modifier
        sb.append(when {
            modifier.isPublic -> "+"
            modifier.isPrivate -> "-"
            modifier.isProtected -> "#"
            else -> "~"
        })
        if (modifier.isStatic) {
            sb.append("S")
        }
        if (modifier.isAbstract) {
            sb.append("A")
        }
        if (modifier.isFinal) {
            sb.append("F")
        }
        if (modifier.isDynamic) {
            sb.append("D")
        }
        return sb.toString()
    }
}
