package com.github.linwancen.plugin.graph.ui

import com.github.linwancen.plugin.graph.parser.Parser
import com.github.linwancen.plugin.graph.parser.RelData
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import org.slf4j.LoggerFactory

object RelController {
    private val LOG = LoggerFactory.getLogger(this::class.java)

    @JvmStatic
    val lastFilesMap = mutableMapOf<Project, Array<VirtualFile>>()
    val lastElementMap = mutableMapOf<Project, PsiElement>()
    val lastCallUsageMap = mutableMapOf<Project, Boolean>()

    @JvmStatic
    fun reload(project: Project) {
        lastFilesMap[project]?.let { forFile(project, it, false) }
        lastElementMap[project]?.let { forElement(project, it, lastCallUsageMap[project] ?: return) }
    }

    /**
     * call by Action and Listener and reload()
     */
    @JvmStatic
    fun forFile(project: Project, files: Array<VirtualFile>, fromAction: Boolean) {
        try {
            lastElementMap.remove(project)
            lastCallUsageMap.remove(project)
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
            LOG.info("RelController.forFile() catch Throwable but log to record.", e)
        }
    }

    /**
     *
     */
    @JvmStatic
    fun buildSrc(project: Project, window: GraphWindow, files: Array<VirtualFile>) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                if (files.size > 1) {
                    window.closeAutoLoad()
                }
                val relData = RelData()
                DumbService.getInstance(project).runReadActionInSmartMode {
                    Parser.src(project, relData, files)
                }
                if (relData.itemMap.isEmpty()) {
                    PlantUmlFileController.plantUml(project, window, files)
                    HtmlFileController.html(project, window, files)
                    return@executeOnPooledThread
                }
                RelDataController.dataToWindow(project, window, relData)
            } catch (e: Throwable) {
                LOG.info("RelController.buildSrc() catch Throwable but log to record.", e)
            }
        }
    }

    /**
     * call by Action and Listener and reload()
     */
    @JvmStatic
    fun forElement(project: Project, psiElement: PsiElement, call: Boolean) {
        try {
            lastFilesMap.remove(project)
            lastElementMap[project] = psiElement
            lastCallUsageMap[project] = call
            ToolWindowManager.getInstance(project).getToolWindow("Graph")?.activate(null)
            val window = GraphWindowFactory.winMap[project] ?: return
            object : Task.Backgroundable(project, "draw ${if (call) "call" else "usage"}") {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        window.closeAutoLoad()
                        val relData = RelData()
                        DumbService.getInstance(project).runReadActionInSmartMode {
                            if (call) {
                                Parser.call(project, relData, psiElement)
                            }
                        }
                        RelDataController.dataToWindow(project, window, relData)
                    } catch (e: Throwable) {
                        LOG.info("RelController.forElement() Thread catch Throwable but log to record.", e)
                    }
                }
            }.queue()
        } catch (e: Throwable) {
            LOG.info("RelController.forElement() catch Throwable but log to record.", e)
        }
    }
}