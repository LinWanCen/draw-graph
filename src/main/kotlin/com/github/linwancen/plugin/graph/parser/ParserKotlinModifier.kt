package com.github.linwancen.plugin.graph.parser

import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction

object ParserKotlinModifier {
    /**
     * + public - private # protected ~ package private(default)
     * S static o open O Override A abstract C Constructor
     * [PlantUML Class Diagram](https://plantuml.com/en/class-diagram)
     */
    fun symbol(method: KtNamedFunction): String {
        val modifierList = method.modifierList
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
        return sb.toString();
    }
}
