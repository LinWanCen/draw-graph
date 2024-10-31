package com.github.linwancen.plugin.graph.parser

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.slf4j.LoggerFactory

abstract class Parser {

    protected abstract fun id(): String

    protected abstract fun srcImpl(project: Project, relData: RelData, files: Array<out VirtualFile>)

    open fun callImpl(project: Project, relData: RelData, psiElement: PsiElement) {}

    open fun nameToElementImpl(project: Project, name: String): PsiElement? {
        return null
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        val SERVICES = mutableMapOf<String, Parser>()

        init {
            val epn: ExtensionPointName<Parser> = ExtensionPointName.create("com.github.linwancen.drawgraph.parser")
            val extensionList = epn.extensionList
            log.info("load graph parser epn {}", extensionList)
            extensionList.forEach {
                log.info("load graph parser {} -> {}", it.id(), it)
                SERVICES[it.id()] = it
            }
        }

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
        fun call(project: Project, relData: RelData, psiElement: PsiElement) {
            var language = psiElement.language
            language.baseLanguage?.let { language = it }
            val usageService = SERVICES[language.id] ?: return
            usageService.callImpl(project, relData, psiElement)
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

    fun regCall(
        callSetMap: Map<String, Set<String>>,
        relData: RelData,
        all: Boolean = false,
    ) {
        for ((usage, callList) in callSetMap) {
            for (call in callList) {
                if (all || callSetMap.containsKey(call)) {
                    relData.regCall(usage, call)
                }
            }
        }
    }

    fun regUsage(
        callSetMap: Map<String, Set<String>>,
        usageSetMap: Map<String, Set<String>>,
        relData: RelData,
        all: Boolean = false,
    ) {
        for ((call, usageList) in usageSetMap) {
            for (usage in usageList) {
                if (all || callSetMap.containsKey(usage)) {
                    relData.regCall(usage, call)
                }
            }
        }
    }
}