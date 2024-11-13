package com.github.linwancen.plugin.graph.ui

import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.printer.PrinterData
import com.github.linwancen.plugin.graph.printer.PrinterGraphviz
import com.github.linwancen.plugin.graph.printer.PrinterMermaid
import com.github.linwancen.plugin.graph.printer.PrinterPlantuml
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.Project

object RelDataController {

    @JvmStatic
    fun dataToWindow(
        project: Project,
        window: GraphWindow,
        relData: RelData,
    ) {
        val (plantumlSrc, plantumlJs) = PrinterPlantuml().toSrc(relData)
        val (mermaidSrc, _) = PrinterMermaid().toSrc(relData)
        val (graphvizSrc, graphvizJs) = PrinterGraphviz().toSrc(relData)
        runInEdt {
            window.toolWindow.activate(null)
            // don't stop when src does not change, update file when open multi project
            window.mermaidSrc.text = mermaidSrc
            window.plantumlSrc.text = plantumlSrc
            window.graphvizSrc.text = graphvizSrc
            PrinterPlantuml.build(PrinterData(plantumlSrc, plantumlJs, project)) {
                runInEdt {
                    if (window.plantumlHtml.text != it) {
                        window.plantumlHtml.text = it
                        if (window.plantumlBrowser != null) window.plantumlBrowser?.load(it)
                    }
                }
            }
            PrinterMermaid.build(PrinterData(mermaidSrc, null, project)) {
                runInEdt {
                    if (window.mermaidHtml.text != it) {
                        window.mermaidHtml.text = it
                        if (window.mermaidBrowser != null) window.mermaidBrowser?.load(it)
                    }
                }
            }
            PrinterGraphviz.build(PrinterData(graphvizSrc, graphvizJs, project)) {
                runInEdt {
                    if (window.graphvizHtml.text != it) {
                        window.graphvizHtml.text = it
                        if (window.graphvizBrowser != null) window.graphvizBrowser?.load(it)
                    }
                }
            }
        }
    }
}