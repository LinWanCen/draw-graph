package com.github.linwancen.plugin.graph.parser.xml

import com.github.linwancen.plugin.graph.printer.Printer
import com.github.linwancen.plugin.graph.parser.Parser
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import org.slf4j.LoggerFactory

/**
 * for pom.xml
 */
class ParserXml : Parser() {
    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val POM_FILE = "pom.xml"
    }

    init {
        log.info("RelService load {}", this.javaClass.simpleName)
        SERVICES[XMLLanguage.INSTANCE.id] = this
    }

    override fun srcImpl(project: Project, printer: Array<out Printer>, files: Array<out VirtualFile>) {
        // use method support same sign
        val callListMap = mutableMapOf<String, List<String>>()
        val psiFiles = if (files.size == 1 && POM_FILE == files[0].name) {
            FilenameIndex.getFilesByName(project, POM_FILE, GlobalSearchScope.projectScope(project)).toList()
        } else {
            files.mapNotNull { PsiManager.getInstance(project).findFile(it) }.filter { POM_FILE == it.name }
        }
        for (psiFile in psiFiles) {
            printer.forEach { it.beforePsiFile(psiFile) }
            if (psiFile !is XmlFile) {
                continue
            }
            RelServicePom.parsePom(psiFile, callListMap, printer)
        }
        for ((usage, callList) in callListMap) {
            for (call in callList) {
                if (callListMap.containsKey(call)) {
                    printer.forEach { it.call(usage, call) }
                }
            }
        }
    }
}