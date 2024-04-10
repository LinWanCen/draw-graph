package com.github.linwancen.plugin.graph.ui

import com.github.linwancen.plugin.graph.parser.Parser
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.printer.PrinterData
import com.github.linwancen.plugin.graph.printer.PrinterGraphviz
import com.github.linwancen.plugin.graph.printer.PrinterMermaid
import com.github.linwancen.plugin.graph.printer.PrinterPlantuml
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import org.slf4j.LoggerFactory

object RelController {
    private val LOG = LoggerFactory.getLogger(this::class.java)

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
        try {
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
        } catch (e: Throwable) {
            LOG.info("RelController catch Throwable but log to record.", e)
        }
    }

    @JvmStatic
    fun buildSrc(project: Project, window: GraphWindow, files: Array<VirtualFile>) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val relData = RelData()
            DumbService.getInstance(project).runReadActionInSmartMode {
                Parser.src(project, relData, files)
            }
            if (relData.itemMap.isEmpty()) {
                return@executeOnPooledThread
            }
            val (mermaidSrc, _) = PrinterMermaid().toSrc(relData)
            val (graphvizSrc, graphvizJs) = PrinterGraphviz().toSrc(relData)
            val (plantumlSrc, plantumlJs) = PrinterPlantuml().toSrc(relData)
            runInEdt {
                window.toolWindow.activate(null)
                if (plantumlSrc == window.plantumlSrc.text) {
                    return@runInEdt
                }
                window.mermaidSrc.text = mermaidSrc
                window.graphvizSrc.text = graphvizSrc
                window.plantumlSrc.text = plantumlSrc
                PrinterMermaid.build(PrinterData(mermaidSrc, null, project)) {
                    runInEdt {
                        window.mermaidHtml.text = it
                        if (window.mermaidBrowser != null) window.mermaidBrowser?.load(it)
                    }
                }
                PrinterGraphviz.build(PrinterData(graphvizSrc, graphvizJs, project)) {
                    runInEdt {
                        window.graphvizHtml.text = it
                        if (window.graphvizBrowser != null) window.graphvizBrowser?.load(it)
                    }
                }
                PrinterPlantuml.build(PrinterData(plantumlSrc, plantumlJs, project)) {
                    runInEdt {
                        window.plantumlHtml.text = it
                        if (window.plantumlBrowser != null) window.plantumlBrowser?.load(it)
                    }
                }
            }
        }
    }
}