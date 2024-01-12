package com.github.linwancen.plugin.graph.parser

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import org.jetbrains.kotlin.idea.search.declarationsSearch.isOverridableElement

object ParserJavaModifier {
    /**
     * + public - private # protected ~ package private(default)
     * S static O Override A abstract C Constructor
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
            method.isOverridableElement() -> "O"
            method.isConstructor -> "C"
            modifierList.hasModifierProperty(PsiModifier.ABSTRACT) -> "A"
            else -> ""
        }
    }
}
