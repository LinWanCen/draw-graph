package com.github.linwancen.plugin.graph.parser.java

import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.psi.PsiModifierListOwner

open class JavaAnno {
    companion object {
        private val INSTANCE = JavaAnno()

        fun addAnno(psiAnnotationOwner: PsiModifierListOwner?, map: MutableMap<String, String>) {
            INSTANCE.addAnno(psiAnnotationOwner, map)
        }
    }

    open fun addAnno(psiAnnotationOwner: PsiModifierListOwner?, map: MutableMap<String, String>) {
        psiAnnotationOwner ?: return
        for (anno in psiAnnotationOwner.annotations) {
            val annoName = anno.qualifiedName
            for (it in anno.parameterList.attributes) {
                val value = it.literalValue ?: continue
                map["$annoName#${it.name ?: "value"}"] = value
            }
        }
        val state = DrawGraphProjectState.of(psiAnnotationOwner.project)
        for (key in state.annoDocArr) {
            map[key]?.let { map["@1"] = it; return@addAnno }
        }
    }
}