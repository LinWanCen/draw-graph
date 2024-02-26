package com.github.linwancen.plugin.graph.parser.java

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier

/**
 * modifierList.hasModifierProperty(PsiModifier.*)
 */
object JavaModifier {
    /**
     * + public - private # protected ~ package private(default)
     * S static O Override A abstract C Constructor F final
     * [PlantUML Class Diagram](https://plantuml.com/en/class-diagram)
     */
    fun symbol(method: PsiMethod): String {
        val modifierList = method.modifierList
        return when {
            modifierList.hasModifierProperty(PsiModifier.PUBLIC) -> '+'
            modifierList.hasModifierProperty(PsiModifier.PRIVATE) -> '-'
            modifierList.hasModifierProperty(PsiModifier.PROTECTED) -> '#'
            else -> '~'
        } + when {
            modifierList.hasModifierProperty(PsiModifier.STATIC) -> "S"
            method.hasAnnotation("java.lang.Override") -> "O"
            method.isConstructor -> "C"
            modifierList.hasModifierProperty(PsiModifier.ABSTRACT) -> "A"
            method.hasModifierProperty(PsiModifier.FINAL) -> "F"
            else -> ""
        }
    }
}
