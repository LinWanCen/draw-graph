package com.github.linwancen.plugin.graph.ui

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

object HtmlFileController {

    @JvmStatic
    fun forHtmlFiles(project: Project, window: GraphWindow, files: Array<VirtualFile>) {
        if (files.size == 1 && files[0].name.endsWith(".html")) {
            DumbService.getInstance(project).runReadActionInSmartMode {
                val psiFile = PsiManager.getInstance(project).findFile(files[0]) ?: return@runReadActionInSmartMode
                val htmlSrc = psiFile.text
                forHtmlSrc(window, htmlSrc)
            }
        }
    }

    @JvmStatic
    fun forHtmlSrc(window: GraphWindow, htmlSrc: String?) {
        runInEdt {
            window.toolWindow.activate(null)
            window.plantumlHtml.text = htmlSrc
            if (window.plantumlBrowser != null) window.plantumlBrowser?.load(htmlSrc)
            window.mermaidHtml.text = htmlSrc
            if (window.mermaidBrowser != null) window.mermaidBrowser?.load(htmlSrc)
            window.graphvizHtml.text = htmlSrc
            if (window.graphvizBrowser != null) window.graphvizBrowser?.load(htmlSrc)
        }
    }
}