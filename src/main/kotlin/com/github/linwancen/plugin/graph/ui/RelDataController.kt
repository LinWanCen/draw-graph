package com.github.linwancen.plugin.graph.ui

import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.printer.*
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
import com.github.linwancen.plugin.graph.ui.webview.Browser
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.Project
import javax.swing.JTextArea

object RelDataController {

    @JvmStatic
    fun dataToWindow(
        project: Project,
        window: GraphWindow,
        relData: RelData,
        isCall: Boolean,
    ) {
        RelData2Effect().save(project, relData, isCall) { set, s ->
            runInEdt {
                window.toolWindow.activate(null)
                window.effect.text = s
            }
        }
        RelData2SQLite().save(project, relData)

        val (plantumlSrc, plantumlJs) = PrinterPlantuml().toSrc(relData)
        val (mermaidSrc, _) = PrinterMermaid().toSrc(relData)
        val (graphvizSrc, graphvizJs) = PrinterGraphviz().toSrc(relData)
        val limit = DrawGraphAppState.of().limit
        if (relData.itemMap.size > limit) {
            val it = "itemMap.size: ${relData.itemMap.size} > limit: $limit\n<br>" +
                    "callSet.size: ${relData.callSet.size}\n<br>" +
                    "parentChildMap.size: ${relData.parentChildMap.size}\n<br>" +
                    "childSet.size: ${relData.childSet.size}"
            runInEdt {
                window.toolWindow.activate(null)
                window.mermaidSrc.text = it
                window.plantumlSrc.text = it
                window.graphvizSrc.text = it
            }
            update(it, window.plantumlHtml, window.plantumlBrowser)
            update(it, window.mermaidHtml, window.mermaidBrowser)
            update(it, window.graphvizHtml, window.graphvizBrowser)
            PrinterPlantuml.build(PrinterData(plantumlSrc, plantumlJs, project), null)
            PrinterMermaid.build(PrinterData(mermaidSrc, null, project), null)
            PrinterGraphviz.build(PrinterData(graphvizSrc, graphvizJs, project), null)
            return
        }
        runInEdt {
            window.toolWindow.activate(null)
            // don't stop when src does not change, update file when open multi project
            window.mermaidSrc.text = mermaidSrc
            window.plantumlSrc.text = plantumlSrc
            window.graphvizSrc.text = graphvizSrc
        }
        PrinterPlantuml.build(PrinterData(plantumlSrc, plantumlJs, project)) {
            update(it, window.plantumlHtml, window.plantumlBrowser)
        }
        PrinterMermaid.build(PrinterData(mermaidSrc, null, project)) {
            update(it, window.mermaidHtml, window.mermaidBrowser)
        }
        PrinterGraphviz.build(PrinterData(graphvizSrc, graphvizJs, project)) {
            update(it, window.graphvizHtml, window.graphvizBrowser)
        }
    }

    private fun update(src: String, jTextArea: JTextArea, browser: Browser?) {
        runInEdt {
            if (jTextArea.text != src) {
                jTextArea.text = src
                browser?.load(src)
            }
        }
    }
}