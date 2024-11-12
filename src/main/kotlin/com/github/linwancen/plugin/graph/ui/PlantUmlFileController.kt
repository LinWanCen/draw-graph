package com.github.linwancen.plugin.graph.ui

import com.github.linwancen.plugin.common.psi.LangUtils
import com.github.linwancen.plugin.graph.printer.PrinterData
import com.github.linwancen.plugin.graph.printer.PrinterPlantuml
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

object PlantUmlFileController {
    @JvmStatic
    val languages = arrayOf("RegExp", "JSON", "yaml")

    @JvmStatic
    fun forPlantUMLFiles(project: Project, window: GraphWindow, files: Array<VirtualFile>) {
        if (files.size != 1) return
        val name = files[0].name
        DumbService.getInstance(project).runReadActionInSmartMode {
            val psiFile = PsiManager.getInstance(project).findFile(files[0]) ?: return@runReadActionInSmartMode
            val src = psiFile.text
            if (name.endsWith(".puml") || name.endsWith(".plantuml")) {
                forPlantUMLSrc(project, window, src)
            } else {
                forPlantUMLSupportFile(psiFile, project, window, src)
            }
        }
    }

    @JvmStatic
    fun forPlantUMLSupportFile(psiFile: PsiElement, project: Project, window: GraphWindow, src: String?) {
        val languageId = LangUtils.matchBaseLanguageId(psiFile, *languages) ?: return
        forPlantUMLSrc(project, window, "@start$languageId\n$src\n@end$languageId")
    }

    @JvmStatic
    fun forPlantUMLSrc(project: Project, window: GraphWindow, plantumlSrc: String?) {
        runInEdt {
            window.toolWindow.activate(null)
            window.plantumlSrc.text = plantumlSrc
            PrinterPlantuml.build(PrinterData(plantumlSrc, null, project)) {
                runInEdt {
                    window.plantumlHtml.text = it
                    if (window.plantumlBrowser != null) window.plantumlBrowser?.load(it)
                }
            }
        }
    }
}