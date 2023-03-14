package com.github.linwancen.plugin.graph.rel.xml

import com.github.linwancen.plugin.graph.draw.RelHandle
import com.github.linwancen.plugin.graph.rel.RelService
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
object RelServiceXml : RelService() {
    private val LOG = LoggerFactory.getLogger(this::class.java)

    init {
        LOG.info("RelService load {}", this.javaClass.simpleName)
        SERVICES[XMLLanguage.INSTANCE.id] = this
    }

    private const val POM_FILE = "pom.xml"

    override fun srcImpl(project: Project, relHandle: Array<out RelHandle>, files: Array<out VirtualFile>) {
        // use method support same sign
        val callListMap = mutableMapOf<String, List<String>>()
        val psiFiles = if (files.size == 1 && POM_FILE == files[0].name) {
            FilenameIndex.getFilesByName(project, POM_FILE, GlobalSearchScope.projectScope(project)).toList()
        } else {
            files.mapNotNull { PsiManager.getInstance(project).findFile(it) }.filter { POM_FILE == it.name }
        }
        for (psiFile in psiFiles) {
            relHandle.forEach { it.beforePsiFile(psiFile) }
            if (psiFile !is XmlFile) {
                continue
            }
            RelServicePom.parsePom(psiFile, callListMap, relHandle)
        }
        for ((usage, callList) in callListMap) {
            for (call in callList) {
                if (callListMap.containsKey(call)) {
                    relHandle.forEach { it.call(usage, call) }
                }
            }
        }
    }
}