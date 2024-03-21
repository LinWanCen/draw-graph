package com.github.linwancen.plugin.graph.parser

import com.github.linwancen.plugin.common.text.Skip
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil

abstract class ParserLang<F : PsiElement> : Parser() {
    override fun runActivity(project: Project) {
        // only load
    }

    protected abstract fun funClass(): Class<F>

    /**
     * Constructor or get/set
     */
    protected abstract fun skipFun(state: DrawGraphProjectState, func: F): Boolean

    protected abstract fun toSign(func: F): String

    protected abstract fun funMap(funMap: MutableMap<String, String>, func: F)

    protected abstract fun classMap(func: F, relData: RelData): MutableMap<String, String>?

    protected abstract fun callList(func: F): List<F>

    override fun srcImpl(project: Project, relData: RelData, files: Array<out VirtualFile>) {
        val state = DrawGraphProjectState.of(project)
        val callListMap = mutableMapOf<String, List<String>>()
        for (file in files) {
            val psiFile = PsiManager.getInstance(project).findFile(file) ?: continue
            val funcs = PsiTreeUtil.findChildrenOfType(psiFile, funClass())
            for (func in funcs) {
                if (skipFun(state, func)) {
                    continue
                }
                val sign = toSign(func)
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
                callListMap[sign] = callList(func)
                    .filter { !skipFun(state, it) }
                    .map { toSign(it) }
                    .filter { !Skip.skip(it, state.includePattern, state.excludePattern) }
            }
        }
        regCall(callListMap, relData)
    }
}