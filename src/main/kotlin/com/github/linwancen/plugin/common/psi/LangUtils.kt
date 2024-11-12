package com.github.linwancen.plugin.common.psi

import com.intellij.psi.PsiElement

object LangUtils {

    @JvmStatic
    fun matchBaseLanguageId(psiElement: PsiElement, vararg languages: String): String? {
        var language = psiElement.language
        while (true) {
            val languageId = language.id
            if (languages.contains(languageId)) {
                return languageId
            }
            language = language.baseLanguage ?: return null
        }
    }
}