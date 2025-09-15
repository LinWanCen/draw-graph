package com.github.linwancen.plugin.graph.ui

import com.github.linwancen.plugin.common.psi.LangUtils
import com.github.linwancen.plugin.common.psi.PsiUnSaveUtils
import com.github.linwancen.plugin.common.vfile.ChildFileUtils
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
    val lastCallUsageMap = mutableMapOf<Project, Boolean?>()

    @JvmStatic
    fun reload(project: Project) {
        lastFilesMap[project]?.let { forFile(project, it, false) }
        lastElementMap[project]?.let {
            val call = lastCallUsageMap[project]
            if (call != null) {
                forElement(project, it, lastCallUsageMap[project] ?: return)
            } else {
                forInjectedElement(project, it)
            }
        }
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
            if (files.size == 1 && !files[0].isDirectory) {
                ApplicationManager.getApplication().executeOnPooledThread {
                    buildSrc(project, window, files, null)
                }
            } else {
                object : Task.Backgroundable(project, "draw parse") {
                    override fun run(indicator: ProgressIndicator) {
                        buildSrc(project, window, files, indicator)
                    }
                }.queue()
            }
        } catch (e: Throwable) {
            LOG.info("RelController.forFile() catch Throwable but log to record.", e)
        }
    }

    @JvmStatic
    private fun buildSrc(project: Project, window: GraphWindow, files: Array<VirtualFile>, indicator: ProgressIndicator?) {
        try {
            val fileList = if (files.size == 1 && !files[0].isDirectory) {
                files.toList()
            } else {
                window.closeAutoLoad()
                val map = ChildFileUtils.recurExtChildFile(files)
                map.remove("class")
                val mostExt = ChildFileUtils.mostExt(map) ?: return
                val mostExtChildFile = mostExt.value
                when (mostExt.key) {
                    "java" -> map["kt"]?.let(mostExtChildFile::addAll)
                    "kt" -> map["java"]?.let(mostExtChildFile::addAll)
                    else -> LOG.info("most ext:{} size:{}", mostExt.key, mostExtChildFile.size)
                }
                mostExtChildFile
            }
            val relData = RelData()
            DumbService.getInstance(project).runReadActionInSmartMode {
                Parser.src(project, relData, fileList, indicator)
            }
            if (relData.itemMap.isEmpty()) {
                PlantUmlFileController.forPlantUMLFiles(project, window, files)
                HtmlFileController.forHtmlFiles(project, window, files)
                return
            }
            RelDataController.dataToWindow(project, window, relData, true)
        } catch (e: Throwable) {
            LOG.info("RelController.buildSrc() catch Throwable but log to record.", e)
        }
    }

    /**
     * call by Action and reload()
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
                            Parser.call(project, relData, psiElement, call, indicator)
                        }
                        RelDataController.dataToWindow(project, window, relData, call)
                    } catch (e: Throwable) {
                        LOG.info("RelController.forElement() Thread catch Throwable but log to record.", e)
                    }
                }
            }.queue()
        } catch (e: Throwable) {
            LOG.info("RelController.forElement() catch Throwable but log to record.", e)
        }
    }

    /**
     * call by Action and reload()
     */
    @JvmStatic
    fun forInjectedElement(project: Project, psiFile: PsiElement) {
        try {
            lastFilesMap.remove(project)
            lastElementMap[project] = psiFile
            lastCallUsageMap.remove(project)
            val window = GraphWindowFactory.winMap[project] ?: return
            DumbService.getInstance(project).runReadActionInSmartMode {
                val src = PsiUnSaveUtils.getText(psiFile)
                if (LangUtils.matchBaseLanguageId(psiFile, "HTML") != null) {
                    HtmlFileController.forHtmlSrc(window, src)
                    return@runReadActionInSmartMode
                }
                PlantUmlFileController.forPlantUMLSupportFile(psiFile, project, window, src)
            }
        } catch (e: Throwable) {
            LOG.info("RelController.forInjectedElement() catch Throwable but log to record.", e)
        }
    }
}