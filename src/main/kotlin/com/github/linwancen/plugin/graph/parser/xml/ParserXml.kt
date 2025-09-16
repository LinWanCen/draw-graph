package com.github.linwancen.plugin.graph.parser.xml

import com.github.linwancen.plugin.graph.parser.Parser
import com.github.linwancen.plugin.graph.parser.RelData
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile

/**
 * for pom.xml
 */
class ParserXml : Parser() {

    companion object {
        private const val POM_FILE = "pom.xml"
    }

    override fun id(): String {
       return XMLLanguage.INSTANCE.id
    }

    override fun srcImpl(project: Project, relData: RelData, files: List<VirtualFile>, indicator: ProgressIndicator?) {
        val callSetMap = mutableMapOf<String, MutableSet<String>>()
        val psiFiles = if (files.size == 1 && POM_FILE == files[0].name) {
            val path = files[0].path
            val dir = path.substring(0, path.length - POM_FILE.length)
            FilenameIndex.getFilesByName(project, POM_FILE, GlobalSearchScope.projectScope(project)).toList()
                .filter { it.virtualFile.path.startsWith(dir) }
        } else {
            files.mapNotNull { PsiManager.getInstance(project).findFile(it) }.filter { POM_FILE == it.name }
        }
        for (psiFile in psiFiles) {
            if (psiFile !is XmlFile) {
                continue
            }
            RelServicePom.parsePom(psiFile, callSetMap, relData)
        }
        regCall(callSetMap, relData)
    }
}