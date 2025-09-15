package com.github.linwancen.plugin.graph.ui

import com.github.linwancen.plugin.common.psi.PsiUnSaveUtils
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object HtmlFileController {

    @JvmStatic
    fun forHtmlFiles(project: Project, window: GraphWindow, files: Array<VirtualFile>) {
        if (files.size == 1 && files[0].name.endsWith(".html")) {
            DumbService.getInstance(project).runReadActionInSmartMode {
                val src = PsiUnSaveUtils.fileText(project, files[0]) ?: return@runReadActionInSmartMode
                forHtmlSrc(window, src)
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