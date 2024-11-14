package com.github.linwancen.plugin.graph.parser

import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil

object Call {
    /**
     * ```mermaid
     * usage ..> call
     * ```
     */
    inline fun <reified F : PsiElement, R : PsiElement> find(func: F, call: Boolean, vararg refClass: Class<out R>) =
        if (call) findRefs(PsiTreeUtil.findChildrenOfAnyType(func, *refClass)).filterIsInstance<F>()
        // I don't know why cannot find usage for KotlinParser.callList in this project
        else ReferencesSearch.search(func).findAll().map {
            val parentOfType = PsiTreeUtil.getParentOfType(it.element, F::class.java)
            parentOfType
        }.filterIsInstance<F>()

    /**
     * ```mermaid
     * usage ..> call
     * ```
     */
    fun <R : PsiElement> findRefs(refs: Collection<R>) = refs
        .flatMap { it.references.toList() }
        .map {
            try {
                it.resolve()
                // byte to src, not used here, because class calls are many, very slow
                // resolve?.navigationElement ?: resolve
            } catch (_: Throwable) {
                // ignore
            }
        }
        .distinct()
}