package com.github.linwancen.plugin.graph.draw

import com.intellij.psi.PsiFile

abstract class RelHandle {
    val handlers = mutableMapOf<String, RelHandle>()

    open fun beforePsiFile(psiFile: PsiFile) {
        // impl it
    }
    open fun afterPsiFile(psiFile: PsiFile) {
        // impl it
    }
    open fun beforeGroup(groupMap: MutableMap<String, String>) {
        // impl it
    }
    open fun afterGroup(groupMap: MutableMap<String, String>) {
        // impl it
    }
    open fun item(itemMap: MutableMap<String, String>) {
        // impl it
    }
    open fun call(usageSign: String, callSign: String) {
        // impl it
    }

    open fun build(): String? {
        return null
    }
}