package com.github.linwancen.plugin.graph.ui

import com.github.linwancen.plugin.graph.parser.Parser
import com.github.linwancen.plugin.graph.printer.PrinterGraphviz
import com.github.linwancen.plugin.graph.printer.PrinterMermaid
import com.github.linwancen.plugin.graph.printer.PrinterPlantuml
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager

object RelController {

    @JvmStatic
    val lastFilesMap = mutableMapOf<Project, Array<VirtualFile>>()

    @JvmStatic
    fun reload(project: Project) {
        forFile(project, lastFilesMap[project] ?: return, false)
    }

    /**
     * call by Action and Listener
     */
    @JvmStatic
    fun forFile(project: Project, files: Array<VirtualFile>, fromAction: Boolean) {
        lastFilesMap[project] = files
        if (fromAction) {
            // let it init
            ToolWindowManager.getInstance(project).getToolWindow("Graph")?.activate(null)
        }
        val window = GraphWindowFactory.winMap[project] ?: return
        if (!fromAction && !window.toolWindow.isVisible) {
            return
        }
        // for before 201.6668.113
        buildSrc(project, window, files)
    }

    @JvmStatic
    fun buildSrc(project: Project, window: GraphWindow, files: Array<VirtualFile>) {
        val mermaid = PrinterMermaid()
        val graphviz = PrinterGraphviz()
        val plantuml = PrinterPlantuml()
        Parser.src(project, arrayOf(mermaid, graphviz, plantuml), files)

        window.toolWindow.activate(null)
        window.mermaidSrc.text = mermaid.src()
        window.graphvizSrc.text = graphviz.src()
        window.plantumlSrc.text = plantuml.src()
        PrinterMermaid.build(window.mermaidSrc.text, project) { window.mermaidHtml.text = it; }
        PrinterGraphviz.build(window.graphvizSrc.text, project) { window.graphvizHtml.text = it; }
        PrinterPlantuml.build(window.plantumlSrc.text, project) { window.plantumlHtml.text = it; }
        window.load()
    }
}