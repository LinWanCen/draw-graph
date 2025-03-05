package com.github.linwancen.plugin.graph.parser.relfile

import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import java.util.regex.Pattern

object RelFileMvc {
    @JvmStatic
    private val mvcPattern = Pattern.compile("^I?([A-Z]\\w+)(?:Controller|Service|ServiceImpl|Mapper)\\.java$")

    @JvmStatic
    fun relFileOf(project: Project, files: List<VirtualFile>): List<VirtualFile> {
        if (!DrawGraphAppState.of().mvc) {
            return files
        }
        val matcher = mvcPattern.matcher(files[0].name)
        if (!matcher.find()) {
            return files
        }
        val prefix = matcher.group(1) ?: return files
        val relFiles = mutableListOf<VirtualFile>()
        val scope = GlobalSearchScope.projectScope(project)
        relFiles.addAll(FilenameIndex.getVirtualFilesByName(project, "${prefix}Controller.java", scope))
        relFiles.addAll(FilenameIndex.getVirtualFilesByName(project, "${prefix}Service.java", scope))
        relFiles.addAll(FilenameIndex.getVirtualFilesByName(project, "I${prefix}Service.java", scope))
        relFiles.addAll(FilenameIndex.getVirtualFilesByName(project, "${prefix}ServiceImpl.java", scope))
        relFiles.addAll(FilenameIndex.getVirtualFilesByName(project, "${prefix}Mapper.java", scope))
        return relFiles
    }
}