package com.github.linwancen.plugin.common.psi

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import java.util.regex.Pattern

object PsiUnSaveUtils {

    @JvmStatic
    val LINE_END_PATTERN: Pattern = Pattern.compile("\r|\n|\r\n")

    @JvmStatic
    fun getText(element: PsiElement): String {
        try {
            val psiFile = element.containingFile ?: return element.text
            val doc = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return element.text
            return doc.getText(element.textRange)
        } catch (_: Throwable) {
            return element.text
        }
    }

    @JvmStatic
    fun fileText(project: Project, file: VirtualFile): String? {
        return FileDocumentManager.getInstance().getDocument(file)?.text
            ?: PsiManager.getInstance(project).findFile(file)?.text
    }
}