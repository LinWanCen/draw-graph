package com.github.linwancen.plugin.graph.parser

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity.RequiredForSmartMode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

abstract class Parser : RequiredForSmartMode {
    override fun runActivity(project: Project) {
        // only load
    }

    protected abstract fun srcImpl(project: Project, relData: RelData, files: Array<out VirtualFile>)

    open fun nameToElementImpl(project: Project, name: String): PsiElement? {
        return null
    }

    companion object {
        @JvmStatic
        val SERVICES = mutableMapOf<String, Parser>()

        /**
         * need DumbService.getInstance(project).runReadActionInSmartMo
         */
        @JvmStatic
        fun src(project: Project, relData: RelData, files: Array<out VirtualFile>) {
            // not get child dir easy select by shift skip dir
            for (file in files) {
                val psiFile = PsiManager.getInstance(project).findFile(file) ?: continue
                var language = psiFile.language
                language.baseLanguage?.let { language = it }
                val usageService = SERVICES[language.id] ?: continue
                // not one by one because pom.xml find all files
                usageService.srcImpl(project, relData, files)
                // only one lang srcImpl all and return
                return
            }
        }

        /**
         * need DumbService.getInstance(project).runReadActionInSmartMo
         */
        @JvmStatic
        fun nameToElement(project: Project, name: String): PsiElement? {
            for (it in SERVICES.values) {
                val element = it.nameToElementImpl(project, name)
                if (element != null) {
                    return element
                }
            }
            return null
        }
    }

    fun regCall(callSetMap: Map<String, Set<String>>, relData: RelData) {
        for ((usage, callList) in callSetMap) {
            for (call in callList) {
                if (callSetMap.containsKey(call)) {
                    relData.regCall(usage, call)
                }
            }
        }
    }

    fun regUsage(callSetMap: Map<String, Set<String>>, usageSetMap: Map<String, Set<String>>, relData: RelData) {
        for ((call, usageList) in usageSetMap) {
            for (usage in usageList) {
                if (callSetMap.containsKey(usage)) {
                    relData.regCall(usage, call)
                }
            }
        }
    }
}