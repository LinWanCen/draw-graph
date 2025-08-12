package com.github.linwancen.plugin.graph.parser.java

import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiReference

open class JavaAnno {
    companion object {
        private val INSTANCE = JavaAnno()

        fun addAnno(psiAnnotationOwner: PsiModifierListOwner?, map: MutableMap<String, String>) {
            INSTANCE.addAnno(psiAnnotationOwner, map)
        }
    }

    open fun addAnno(psiAnnotationOwner: PsiModifierListOwner?, map: MutableMap<String, String>) {
        psiAnnotationOwner ?: return
        val eval = JavaPsiFacade.getInstance(psiAnnotationOwner.project).constantEvaluationHelper
        for (anno in psiAnnotationOwner.annotations) {
            val annoName = anno.qualifiedName
            for (it in anno.parameterList.attributes) {
                var value = it.literalValue
                // PsiField reference value
                if (value == null) {
                    val v = it.value
                    if (v !is PsiReference) {
                        continue
                    }
                    try {
                        val resolve = v.resolve()
                        if (resolve is PsiField) {
                            val initializer = resolve.initializer ?: continue
                            value = eval.computeConstantExpression(initializer).toString()
                        }
                    } catch (ignored: Throwable) {}
                }
                map["$annoName#${it.name ?: "value"}"] = value ?: continue
            }
        }
        val state = DrawGraphAppState.of()
        for (key in state.annoDocArr) {
            if (key.isBlank()) {
                continue
            }
            map[key]?.let { map["@1"] = it; return@addAnno }
        }
    }
}