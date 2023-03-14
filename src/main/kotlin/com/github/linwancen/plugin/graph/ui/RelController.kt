package com.github.linwancen.plugin.graph.ui

import com.github.linwancen.plugin.graph.draw.RelHandleMermaid
import com.github.linwancen.plugin.graph.rel.RelService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager

object RelController {

    @JvmStatic
    val filesMap = mutableMapOf<Project, Array<VirtualFile>>()

    @JvmStatic
    fun reload(project: Project) {
        forFile(project, filesMap[project] ?: return)
    }

    @JvmStatic
    fun forFile(project: Project, files: Array<VirtualFile>) {
        filesMap[project] = files
        val s = src(project, files) ?: return
        if (files.size > 1) {
            // let it init
            ToolWindowManager.getInstance(project).getToolWindow("Graph")?.activate(null)
        }
        val window = GraphWindowFactory.winMap[project] ?: return
        if (files.size == 1 && !window.toolWindow.isVisible) {
            return
        }
        // for before 201.6668.113
        window.toolWindow.activate(null)
        if (window.mermaidSrc.text != s) {
            window.mermaidSrc.text = s
        }
    }

    @JvmStatic
    fun src(project: Project, files: Array<VirtualFile>): String? {
        val mermaidRelHandle = RelHandleMermaid()
        RelService.src(project, arrayOf(mermaidRelHandle), files)
        return mermaidRelHandle.build()
    }
}