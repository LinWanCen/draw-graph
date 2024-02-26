package com.github.linwancen.plugin.graph.parser

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity.RequiredForSmartMode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

abstract class Parser : RequiredForSmartMode {
    override fun runActivity(project: Project) {
        // only load
    }

    protected abstract fun srcImpl(project: Project, relData: RelData, files: Array<out VirtualFile>)

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
    }

    fun regCall(callListMap: Map<String, List<String>>, relData: RelData) {
        for ((usage, callList) in callListMap) {
            for (call in callList) {
                if (callListMap.containsKey(call)) {
                    relData.regCall(usage, call)
                }
            }
        }
    }
}