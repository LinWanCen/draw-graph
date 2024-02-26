package com.github.linwancen.plugin.graph.parser.kotlin

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * modifierList.hasModifier(KtTokens.*_KEYWORD)
 */
object KotlinModifier {
    /**
     * + public - private # protected ~ package private(default)
     * <br>S static o open O Override A abstract C Constructor
     * <br>[PlantUML Class Diagram](https://plantuml.com/en/class-diagram)
     */
    fun symbol(func: KtNamedFunction): String {
        val modifierList = func.modifierList
        modifierList ?: return ""
        val sb = StringBuilder()
        if (modifierList.hasModifier(KtTokens.PRIVATE_KEYWORD)) {
            sb.append("-")
        }
        if (modifierList.hasModifier(KtTokens.PROTECTED_KEYWORD)) {
            sb.append("#")
        }
        if (modifierList.hasModifier(KtTokens.INTERNAL_KEYWORD)) {
            sb.append("I")
        }
        if (modifierList.hasModifier(KtTokens.OPEN_KEYWORD)) {
            sb.append("o")
        }
        when {
            modifierList.hasModifier(KtTokens.ABSTRACT_KEYWORD) -> sb.append("A")
            modifierList.hasModifier(KtTokens.OVERRIDE_KEYWORD) -> sb.append("O")
        }
        return sb.toString()
    }
}
