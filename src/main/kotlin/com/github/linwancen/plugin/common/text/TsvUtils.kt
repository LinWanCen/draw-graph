package com.github.linwancen.plugin.common.text

import com.github.linwancen.plugin.common.psi.PsiUnSaveUtils
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

object TsvUtils {

    @JvmStatic
    fun load(project: Project, end: String): MutableMap<String, String> {
        val mapperTableMap = mutableMapOf<String, String>()
        DumbService.getInstance(project).runReadActionInSmartMode {
            val projectScope = GlobalSearchScope.projectScope(project)
            val files = FilenameIndex.getAllFilesByExt(project, "tsv", projectScope)
            for (file in files) {
                if (file.name.endsWith(end)) {
                    val text = PsiUnSaveUtils.fileText(project, file)
                    for (line in PsiUnSaveUtils.LINE_END_PATTERN.split(text)) {
                        val split = line.split('\t', limit = 2)
                        if (split.size == 2) {
                            mapperTableMap[split[0]] = split[1]
                        }
                    }
                }
            }
        }
        return mapperTableMap
    }
}