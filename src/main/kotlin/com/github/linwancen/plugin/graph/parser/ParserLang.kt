package com.github.linwancen.plugin.graph.parser

import com.github.linwancen.plugin.common.TaskTool
import com.github.linwancen.plugin.common.text.Skip
import com.github.linwancen.plugin.graph.parser.relfile.RelFile
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil

abstract class ParserLang<F : PsiElement> : Parser() {

    protected abstract fun funClass(): Class<F>

    /**
     * Constructor or get/set
     */
    protected abstract fun skipFun(state: DrawGraphProjectState, func: F): Boolean

    protected abstract fun toSign(func: F): String?

    protected abstract fun funMap(funMap: MutableMap<String, String>, func: F)

    protected abstract fun classMap(func: F, relData: RelData): MutableMap<String, String>?

    protected abstract fun callList(func: F, call: Boolean): List<F>

    /** interface impl */
    protected open fun fileCall(
        callSetMap: MutableMap<String, MutableSet<String>>,
        usageSetMap: MutableMap<String, MutableSet<String>>,
        psiFile: PsiFile,
    ) {
    }

    override fun srcImpl(project: Project, relData: RelData, files: List<VirtualFile>, indicator: ProgressIndicator?) {
        val state = DrawGraphProjectState.of(project)
        val callSetMap = mutableMapOf<String, MutableSet<String>>()
        val psiManager = PsiManager.getInstance(project)
        val relFiles = if (files.size != 1) files else RelFile.relFileOf(project, files)
        val parserMap = parserMap()
        val taskTool = if (indicator == null) null else TaskTool(indicator, relFiles.size)
        for ((index, file) in relFiles.withIndex()) {
            if (taskTool != null) {
                taskTool.beforeNext(index, file.path) ?: return
            }
            val psiFile = psiManager.findFile(file) ?: continue
            val usageService = findParser(psiFile, parserMap) ?: continue
            if (usageService is ParserLang<*>) {
                usageService.fileImpl(psiFile, state, file, relData, callSetMap)
            }
        }
        regCall(callSetMap, relData)
    }

    fun fileImpl(
        psiFile: PsiFile, state: DrawGraphProjectState, file: VirtualFile,
        relData: RelData, callSetMap: MutableMap<String, MutableSet<String>>
    ) {
        val funcs = PsiTreeUtil.findChildrenOfType(psiFile, funClass())
        for (func in funcs) {
            val sign = funcSign(state, func, file, relData) ?: continue
            // override fun
            val callSet = callSetMap.computeIfAbsent(sign) { mutableSetOf() }
            callSet.addAll(
                callList(func, true)
                    .filter { !skipFun(state, it) }
                    .mapNotNull { toSign(it) }
                    .filter { !Skip.skip(it, state.includePattern, state.excludePattern) })
        }
    }

    private fun funcSign(state: DrawGraphProjectState, func: F, file: VirtualFile?, relData: RelData): String? {
        if (skipFun(state, func)) {
            return null
        }
        var path = file?.path
        val inJar = path == null || path.contains('!')
        if (inJar && state.skipLib && !state.autoLoad) {
            return null
        }
        val sign = toSign(func) ?: return null
        if (Skip.skip(sign, state.includePattern, state.excludePattern)) {
            return null
        }
        val funMap = mutableMapOf<String, String>()
        funMap["sign"] = sign
        val srcFun = CommentUtils.byteToSrc(func)
        funMap(funMap, srcFun)
        val name = funMap["name"]
        val classMap = classMap(srcFun, relData)
        if (inJar) {
            classMap?.let { path = it["sign"] }
        }
        path?.let { funMap["link"] = "$path#${name?.substring(name.lastIndexOf(' ') + 1) ?: ""}" }
        if (classMap == null) {
            relData.regParentChild(funMap)
        } else {
            path?.let { classMap["link"] = "$path#" }
            relData.regParentChild(classMap, funMap)
        }
        return sign
    }

    override fun callImpl(
        project: Project,
        relData: RelData,
        psiElement: PsiElement,
        isCall: Boolean,
        indicator: ProgressIndicator?
    ) {
        val state = DrawGraphProjectState.of(project)
        val basePath = project.basePath
        val callSetMap = mutableMapOf<String, MutableSet<String>>()
        val func = PsiTreeUtil.getParentOfType(psiElement, funClass(), false) ?: return
        funcSign(state, func, func.containingFile?.virtualFile, relData) ?: return
        recursiveCall(1, func, isCall, mutableSetOf(), indicator) { _, usage, call ->
            val callFile = call.containingFile?.virtualFile
            if (state.skipLib && callFile?.path?.startsWith(basePath ?: "") == false) {
                return@recursiveCall
            }
            val usageSign = toSign(usage) ?: return@recursiveCall
            val callSign = funcSign(state, call, callFile, relData) ?: return@recursiveCall
            val callSet = callSetMap.computeIfAbsent(if (isCall) usageSign else callSign) { mutableSetOf() }
            callSet.add(if (isCall) callSign else usageSign)
        }
        regCall(callSetMap, relData, true)
    }

    open fun recursiveCall(
        level: Int,
        usage: F,
        isCall: Boolean,
        set: MutableSet<F>,
        indicator: ProgressIndicator?,
        callBack: (level: Int, usage: F, call: F) -> Unit,
    ) {
        if (!set.add(usage)) {
            return
        }
        val callList = callList(usage, isCall)
        val taskTool = if (indicator == null) null else TaskTool(indicator, callList.size)
        for ((index, call) in callList.withIndex()) {
            if (taskTool != null) {
                taskTool.beforeNext(index, "$level $call") ?: return
            }
            callBack.invoke(level, usage, call)
            recursiveCall(level + 1, call, isCall, set, indicator, callBack)
        }
    }
}