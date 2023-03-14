package com.github.linwancen.plugin.graph.rel

import com.github.linwancen.plugin.graph.comment.JavaComment
import com.github.linwancen.plugin.graph.draw.RelHandle
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import org.slf4j.LoggerFactory

object RelServiceJava : RelService() {
    private val LOG = LoggerFactory.getLogger(this::class.java)

    init {
        LOG.info("RelService load {}", this.javaClass.simpleName)
        SERVICES[JavaLanguage.INSTANCE.id] = this
    }


    override fun srcImpl(project: Project, relHandle: Array<out RelHandle>, files: Array<out VirtualFile>) {
        // use method support same sign
        val callListMap = mutableMapOf<PsiMethod, List<PsiMethod>>()
        for (file in files) {
            val psiFile = PsiManager.getInstance(project).findFile(file) ?: return
            relHandle.forEach { it.beforePsiFile(psiFile) }
            if (psiFile !is PsiJavaFile) {
                continue
            }
            val classes = psiFile.classes
            classes.forEach { psiClass ->
                val classMap = mutableMapOf<String, String>()
                psiClass.name?.let { classMap["name"] = it }
                psiClass.qualifiedName?.let { classMap["sign"] = it }
                JavaComment.addDocParam(psiClass.docComment, classMap)
                relHandle.forEach { it.beforeGroup(classMap) }
                val methods = psiClass.methods
                for (method in methods) {
                    val methodMap = mutableMapOf<String, String>()
                    methodMap["name"] = method.name
                    methodMap["sign"] = "${psiClass.qualifiedName}.${method.name}"
                    JavaComment.addDocParam(method.docComment, methodMap)
                    relHandle.forEach { it.item(methodMap) }

                    val callList = callList(method)
                    // even empty list should put for `callListMap.containsKey(call)`
                    callListMap[method] = callList
                }
                relHandle.forEach { it.afterGroup(classMap) }
            }
            relHandle.forEach { it.afterPsiFile(psiFile) }
        }
        for ((usage, callList) in callListMap) {
            for (call in callList) {
                if (callListMap.containsKey(call)) {
                    val usageSign = "${usage.containingClass?.qualifiedName}.${usage.name}"
                    val callSign = "${call.containingClass?.qualifiedName}.${call.name}"
                    relHandle.forEach { it.call(usageSign, callSign) }
                }
            }
        }
    }

}