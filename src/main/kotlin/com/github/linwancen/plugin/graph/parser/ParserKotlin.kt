package com.github.linwancen.plugin.graph.parser

import com.github.linwancen.plugin.common.text.Skip
import com.github.linwancen.plugin.graph.comment.KotlinComment
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.slf4j.LoggerFactory

class ParserKotlin : Parser() {
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("load ParserKotlin")
        SERVICES[KotlinLanguage.INSTANCE.id] = this
    }

    override fun srcImpl(project: Project, relData: RelData, files: Array<out VirtualFile>) {
        val state = DrawGraphProjectState.of(project)
        // use method support same sign
        val callListMap = mutableMapOf<KtNamedFunction, List<KtNamedFunction>>()
        for (file in files) {
            val psiFile = PsiManager.getInstance(project).findFile(file) ?: return
            if (psiFile !is KtFile) {
                continue
            }
            val classes = PsiTreeUtil.findChildrenOfType(psiFile, KtClassOrObject::class.java)
            classes.forEach { psiClass ->
                val classMap = mutableMapOf<String, String>()
                psiClass.name?.let { classMap["name"] = it }
                psiClass.fqName?.let { classMap["sign"] = it.asString() }
                KotlinComment.addDocParam(psiClass.docComment, classMap)
                if (psiClass is KtClassOrObject) {
                    val methods = PsiTreeUtil.findChildrenOfType(psiFile, KtNamedFunction::class.java)
                    for (method in methods) {
                        val sign = "${psiClass.fqName}#${method.name}"
                        if (Skip.skip(sign, state.includePattern, state.excludePattern)) {
                            continue
                        }
                        val methodMap = mutableMapOf<String, String>()
                        methodMap["sign"] = sign
                        methodMap["name"] = method.name.toString()
                        KotlinComment.addDocParam(method.docComment, methodMap)
                        relData.regParentChild(classMap, methodMap)

                        val callList = PsiTreeUtil
                            .findChildrenOfType(method, KtNameReferenceExpression::class.java)
                            .flatMap { it.references.toList() }
                            .map {
                                try {
                                    it.resolve()
                                } catch (_: Throwable) {
                                    // ignore
                                }
                            }
                            .distinct()
                            .filterIsInstance<KtNamedFunction>()
                        // even empty list should put for `callListMap.containsKey(call)`
                        callListMap[method] = callList
                    }
                }
            }
        }
        for ((usage, callList) in callListMap) {
            for (call in callList) {
                if (callListMap.containsKey(call)) {
                    val usageSign = "${usage.containingClassOrObject?.fqName}#${usage.name}"
                    val callSign = "${call.containingClassOrObject?.fqName}#${call.name}"
                    val skip = Skip.skip(callSign, state.includePattern, state.excludePattern)
                    if (!skip) {
                        relData.regCall(usageSign, callSign)
                    }
                }
            }
        }
    }

}