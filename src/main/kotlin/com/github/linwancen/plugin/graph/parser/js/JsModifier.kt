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
        if (func.attributeList?.hasModifier(JSAttributeList.ModifierType.STATIC) == true) {
            sb.append("S")
        }
        if (func.attributeList?.hasModifier(JSAttributeList.ModifierType.OVERRIDE) == true) {
            sb.append("O")
        }
        if (func.attributeList?.hasModifier(JSAttributeList.ModifierType.ABSTRACT) == true) {
            sb.append("A")
        }
        if (func.isConstructor) {
            sb.append("C")
        }
        if (func.attributeList?.hasModifier(JSAttributeList.ModifierType.FINAL) == true) {
            sb.append("F")
        }
        if (func.attributeList?.hasModifier(JSAttributeList.ModifierType.DYNAMIC) == true) {
            sb.append("D")
        }
        return sb.toString()
    }
}
