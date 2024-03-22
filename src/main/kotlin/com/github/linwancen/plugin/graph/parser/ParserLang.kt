package com.github.linwancen.plugin.graph.parser

import com.github.linwancen.plugin.common.text.Skip
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import java.util.regex.Pattern

abstract class ParserLang<F : PsiElement> : Parser() {
    override fun runActivity(project: Project) {
        // only load
    }

    protected abstract fun funClass(): Class<F>

    /**
     * Constructor or get/set
     */
    protected abstract fun skipFun(state: DrawGraphProjectState, func: F): Boolean

    protected abstract fun toSign(func: F): String?

    protected abstract fun funMap(funMap: MutableMap<String, String>, func: F)

    protected abstract fun classMap(func: F, relData: RelData): MutableMap<String, String>?

    protected abstract fun callList(func: F): List<F>

    protected open fun fileCall(
        callSetMap: MutableMap<String, MutableSet<String>>,
        usageSetMap: MutableMap<String, MutableSet<String>>,
        psiFile: PsiFile
    ) {}

    override fun srcImpl(project: Project, relData: RelData, files: Array<out VirtualFile>) {
        val state = DrawGraphProjectState.of(project)
        val callSetMap = mutableMapOf<String, MutableSet<String>>()
        val usageSetMap = mutableMapOf<String, MutableSet<String>>()
        val psiManager = PsiManager.getInstance(project)
        val relFiles = mvc(files, project)
        for (file in relFiles) {
            val psiFile = psiManager.findFile(file) ?: continue
            val funcs = PsiTreeUtil.findChildrenOfType(psiFile, funClass())
            for (func in funcs) {
                if (skipFun(state, func)) {
                    continue
                }
                val sign = toSign(func) ?: continue
                if (Skip.skip(sign, state.includePattern, state.excludePattern)) {
                    continue
                }
                val funMap = mutableMapOf<String, String>()
                funMap["sign"] = sign
                funMap["filePath"] = file.path
                funMap(funMap, func)
                val classMap = classMap(func, relData)
                if (classMap == null) {
                    relData.regParentChild(funMap)
                } else {
                    relData.regParentChild(classMap, funMap)
                }
                // overload fun
                val callSet = callSetMap.computeIfAbsent(sign) { mutableSetOf() }
                callSet.addAll(callList(func)
                    .filter { !skipFun(state, it) }
                    .mapNotNull { toSign(it) }
                    .filter { !Skip.skip(it, state.includePattern, state.excludePattern) })
            }
            fileCall(callSetMap, usageSetMap, psiFile)
        }
        regCall(callSetMap, relData)
        regUsage(callSetMap, usageSetMap, relData)
    }

    companion object {
        @JvmStatic
        private val mvcPattern = Pattern.compile("^I?([A-Z]\\w+)(?:Controller|Service|ServiceImpl|Mapper)\\.java$")

        @JvmStatic
        private fun mvc(files: Array<out VirtualFile>, project: Project): Array<out VirtualFile> {
            if (!DrawGraphAppState.of().mvc) {
                return files
            }
            if (files.size != 1) {
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
            return relFiles.toTypedArray()
        }
    }
}