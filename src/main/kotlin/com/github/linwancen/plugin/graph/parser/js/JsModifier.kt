package com.github.linwancen.plugin.graph.parser.js

import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList

/**
 * func.hasModifier(JSAttributeList.ModifierType.*)
 */
object JsModifier {
    /**
     * S static O Override A abstract C Constructor F final D dynamic
     * [PlantUML Class Diagram](https://plantuml.com/en/class-diagram)
     */
    fun symbol(func: JSFunction): String {
        val sb = StringBuilder()
        if (func.hasModifier(JSAttributeList.ModifierType.STATIC)) {
            sb.append("S")
        }
        if (func.hasModifier(JSAttributeList.ModifierType.OVERRIDE)) {
            sb.append("O")
        }
        if (func.hasModifier(JSAttributeList.ModifierType.ABSTRACT)) {
            sb.append("A")
        }
        if (func.isConstructor) {
            sb.append("C")
        }
        if (func.hasModifier(JSAttributeList.ModifierType.FINAL)) {
            sb.append("F")
        }
        if (func.hasModifier(JSAttributeList.ModifierType.DYNAMIC)) {
            sb.append("D")
        }
        return sb.toString()
    }
}
