package com.github.linwancen.plugin.graph.parser

import com.github.linwancen.plugin.graph.printer.Printer
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity.RequiredForSmartMode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil

abstract class Parser : RequiredForSmartMode {
    override fun runActivity(project: Project) {
        // only load
    }

    protected abstract fun srcImpl(project: Project, printer: Array<out Printer>, files: Array<out VirtualFile>)

    companion object {
        @JvmStatic
        val SERVICES = mutableMapOf<String, Parser>()

        @JvmStatic
        fun src(project: Project, printer: Array<out Printer>, files: Array<out VirtualFile>) {
            for (file in files) {
                val psiFile = PsiManager.getInstance(project).findFile(file) ?: return
                val usageService = SERVICES[psiFile.language.id] ?: continue
                usageService.srcImpl(project, printer, files)
                return
            }
        }
    }

    /**
     * ```mermaid
     * usage ..> call
     * ```
     */
    inline fun <reified T : PsiElement> callList(method: T) =
        PsiTreeUtil
            .findChildrenOfType(method, PsiIdentifier::class.java)
            .mapNotNull { it.context }
            .flatMap { it.references.toList() }
            .map {
                try {
                    it.resolve()
                } catch (_: Throwable) {
                    // ignore
                }
            }
            .distinct()
            .filterIsInstance<T>()
}