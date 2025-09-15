package com.github.linwancen.plugin.common.psi

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import java.util.regex.Pattern

object PsiUnSaveUtils {

    @JvmStatic
    val LINE_END_PATTERN: Pattern = Pattern.compile("\r|\n|\r\n")

    @JvmStatic
    fun getText(element: PsiElement): String {
        try {
            val psiFile = element.containingFile ?: return element.text
            val doc = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return element.text
            val range = element.textRange
            return doc.text.substring(range.startOffset, range.endOffset)
        } catch (_: Throwable) {
            return element.text
        }
    }
}