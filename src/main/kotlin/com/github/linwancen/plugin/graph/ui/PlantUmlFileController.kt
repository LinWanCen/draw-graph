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
        if (files.size == 1 && files[0].name.endsWith(".puml")) {
            DumbService.getInstance(project).runReadActionInSmartMode {
                val psiFile = PsiManager.getInstance(project).findFile(files[0])
                    ?: return@runReadActionInSmartMode
                runInEdt {
                    val plantumlSrc = psiFile.text
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
}