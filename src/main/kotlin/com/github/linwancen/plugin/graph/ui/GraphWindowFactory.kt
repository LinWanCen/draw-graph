package com.github.linwancen.plugin.graph.ui

import com.github.linwancen.plugin.graph.printer.InstallMermaid
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class GraphWindowFactory : ToolWindowFactory {

    override fun init(toolWindow: ToolWindow) {
        val title: String = DrawGraphBundle.message("window", arrayOfNulls<Any>(0))
        toolWindow.stripeTitle = title
        toolWindow.title = title
    }

    companion object {
        @JvmStatic
        val winMap = mutableMapOf<Project, GraphWindow>()
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        InstallMermaid.checkAndInstall()
        val authorWindow = GraphWindow(project, toolWindow)
        val iterator = winMap.iterator()
        for (entry in iterator) {
            if (entry.key.isDisposed) {
                iterator.remove()
            }
        }
        winMap[project] = authorWindow
        // 222.2680.4 delete SERVICE
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(authorWindow.mainPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}