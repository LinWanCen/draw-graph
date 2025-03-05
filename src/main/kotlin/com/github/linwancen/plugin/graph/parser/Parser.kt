package com.github.linwancen.plugin.graph.parser

import com.intellij.lang.Language
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.slf4j.LoggerFactory
import kotlin.reflect.full.createInstance

abstract class Parser {

    protected abstract fun id(): String

    protected abstract fun srcImpl(
        project: Project,
        relData: RelData,
        files: List<VirtualFile>,
        indicator: ProgressIndicator?
    )

    open fun callImpl(
        project: Project,
        relData: RelData,
        psiElement: PsiElement,
        isCall: Boolean,
        indicator: ProgressIndicator?
    ) {}

    open fun nameToElementImpl(project: Project, name: String): PsiElement? {
        return null
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)

        fun parserMap(): MutableMap<String, Parser> {
            val epn: ExtensionPointName<Parser> = ExtensionPointName.create("com.github.linwancen.drawgraph.parser")
            val parserList = epn.extensionList
            log.info("load graph parser epn {}", parserList)
            val parserMap = mutableMapOf<String, Parser>()
            parserList.forEach {
                log.info("load graph parser {} -> {}", it.id(), it)
                parserMap[it.id()] = it
            }
            // for 2024.2
            if (parserMap["kotlin"] == null) {
                try {
                    val kotlin = Class.forName("com.github.linwancen.plugin.graph.parser.kotlin.KotlinParser").kotlin
                    val parser = kotlin.createInstance() as Parser
                    parserMap["kotlin"] = parser
                    log.info("add load graph parser kotlin -> {}", parser)
                } catch (_: Exception) {
                }
            }
            return parserMap
        }

        @JvmStatic
        fun findParser(element: PsiElement, map: Map<String, Parser>): Parser? {
            var language: Language = element.language
            while (true) {
                val impl: Parser? = map[language.id]
                if (impl != null) {
                    return impl
                }
                language = language.baseLanguage ?: return null
            }
        }

        /**
         * need DumbService.getInstance(project).runReadActionInSmartMo
         */
        @JvmStatic
        fun src(project: Project, relData: RelData, files: List<VirtualFile>, indicator: ProgressIndicator?) {
            val parserMap = parserMap()
            for (file in files) {
                val psiFile = PsiManager.getInstance(project).findFile(file) ?: continue
                val usageService = findParser(psiFile, parserMap) ?: continue
                // not one by one because pom.xml find all files
                usageService.srcImpl(project, relData, files, indicator)
                // only one lang srcImpl all and return
                return
            }
        }

        /**
         * need DumbService.getInstance(project).runReadActionInSmartMo
         */
        @JvmStatic
        fun call(
            project: Project,
            relData: RelData,
            psiElement: PsiElement,
            call: Boolean,
            indicator: ProgressIndicator
        ) {
            val parserMap = parserMap()
            var language = psiElement.language
            language.baseLanguage?.let { language = it }
            val usageService = parserMap[language.id] ?: return
            usageService.callImpl(project, relData, psiElement, call, indicator)
        }

        /**
         * need DumbService.getInstance(project).runReadActionInSmartMo
         */
        @JvmStatic
        fun nameToElement(project: Project, name: String): PsiElement? {
            val epn: ExtensionPointName<Parser> = ExtensionPointName.create("com.github.linwancen.drawgraph.parser")
            val parserList = epn.extensionList
            for (it in parserList) {
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