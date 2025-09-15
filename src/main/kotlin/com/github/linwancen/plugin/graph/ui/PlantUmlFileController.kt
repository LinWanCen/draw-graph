package com.github.linwancen.plugin.graph.ui

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.linwancen.plugin.common.psi.LangUtils
import com.github.linwancen.plugin.common.psi.PsiUnSaveUtils
import com.github.linwancen.plugin.common.text.ArrayNewLinePrinter
import com.github.linwancen.plugin.common.text.JsonValueParser
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
        val virtualFile = files[0]
        val name = virtualFile.name
        DumbService.getInstance(project).runReadActionInSmartMode {
            val src = PsiUnSaveUtils.fileText(project, files[0]) ?: return@runReadActionInSmartMode
            if (name.endsWith(".puml") || name.endsWith(".plantuml")) {
                forPlantUMLSrc(project, window, src)
            } else {
                val psiFile = PsiManager.getInstance(project).findFile(files[0]) ?: return@runReadActionInSmartMode
                forPlantUMLSupportFile(psiFile, project, window, src)
            }
        }
    }

    @JvmStatic
    fun forPlantUMLSupportFile(psiFile: PsiElement, project: Project, window: GraphWindow, src: String?) {
        val languageId = LangUtils.matchBaseLanguageId(psiFile, *languages) ?: return
        val code = if (!languageId.contains("JSON")) {
            src
        } else {
            val mapper = ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .setDefaultPrettyPrinter(ArrayNewLinePrinter())
            val jsonNode = mapper.readTree(src)
            val convert = JsonValueParser.convert(mapper, jsonNode)
            mapper.writeValueAsString(convert)
        }
        forPlantUMLSrc(project, window, "@start$languageId\n$code\n@end$languageId")
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