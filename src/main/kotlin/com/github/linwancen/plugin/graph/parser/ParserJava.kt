package com.github.linwancen.plugin.graph.parser

import com.github.linwancen.plugin.common.text.Skip
import com.github.linwancen.plugin.graph.comment.JavaComment
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import org.slf4j.LoggerFactory

class ParserJava : Parser() {
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("load ParserJava")
        SERVICES[JavaLanguage.INSTANCE.id] = this
    }

    override fun srcImpl(project: Project, relData: RelData, files: Array<out VirtualFile>) {
        val state = DrawGraphProjectState.of(project)
        // use method support same sign
        val callListMap = mutableMapOf<PsiMethod, List<PsiMethod>>()
        for (file in files) {
            val psiFile = PsiManager.getInstance(project).findFile(file) ?: return
            if (psiFile !is PsiJavaFile) {
                continue
            }
            val classes = psiFile.classes
            classes.forEach { psiClass ->
                val classMap = mutableMapOf<String, String>()
                psiClass.name?.let { classMap["name"] = it }
                psiClass.qualifiedName?.let { classMap["sign"] = it }
                JavaComment.addDocParam(psiClass.docComment, classMap)
                val methods = psiClass.methods
                for (method in methods) {
                    val sign = "${psiClass.qualifiedName}#${method.name}"
                    if (Skip.skip(sign, state.includePattern, state.excludePattern)) {
                        continue
                    }
                    val methodMap = mutableMapOf<String, String>()
                    methodMap["sign"] = sign
                    val v = ParserJavaModifier.symbol(method)
                    methodMap["name"] = "$v ${method.name}"
                    JavaComment.addDocParam(method.docComment, methodMap)
                    if (!(method.isConstructor && !method.hasParameters())) {
                        relData.regParentChild(classMap, methodMap)
                    }

                    val callList = callList(method)
                    // even empty list should put for `callListMap.containsKey(call)`
                    callListMap[method] = callList
                }
            }
        }
        for ((usage, callList) in callListMap) {
            for (call in callList) {
                if (callListMap.containsKey(call)) {
                    val usageSign = "${usage.containingClass?.qualifiedName}#${usage.name}"
                    val callSign = "${call.containingClass?.qualifiedName}#${call.name}"
                    val skip = Skip.skip(callSign, state.includePattern, state.excludePattern)
                    val emptyConstructor = call.isConstructor && !call.hasParameters()
                    if (!skip && !emptyConstructor) {
                        relData.regCall(usageSign, callSign)
                    }
                }
            }
        }
    }

}