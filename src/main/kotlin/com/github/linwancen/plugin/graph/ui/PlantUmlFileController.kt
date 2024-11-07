package com.github.linwancen.plugin.graph.ui

import com.github.linwancen.plugin.graph.printer.PrinterData
import com.github.linwancen.plugin.graph.printer.PrinterPlantuml
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

object PlantUmlFileController {

    @JvmStatic
    fun plantUml(project: Project, window: GraphWindow, files: Array<VirtualFile>) {
        if (files.size != 1) return
        val name = files[0].name
        DumbService.getInstance(project).runReadActionInSmartMode {
            val psiFile = PsiManager.getInstance(project).findFile(files[0])
                ?: return@runReadActionInSmartMode
            var plantumlSrc = psiFile.text
            var support = name.endsWith(".puml") || name.endsWith(".plantuml")
            val language = psiFile.language
            val languageId = (language.baseLanguage ?: language).id
            val languages = arrayOf("json", "yaml", "regexp")
            if (languages.contains(languageId.toLowerCase())) {
                support = true
                plantumlSrc = "@start$languageId\n$plantumlSrc\n@end$languageId"
            }
            if (!support) {
                return@runReadActionInSmartMode
            }
            runInEdt {
                window.toolWindow.activate(null)
                if (plantumlSrc == window.plantumlSrc.text) {
                    return@runInEdt
                }
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
}