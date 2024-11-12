package com.github.linwancen.plugin.graph.action

import com.github.linwancen.plugin.graph.ui.DrawGraphBundle
import com.github.linwancen.plugin.graph.ui.RelController
import com.intellij.ide.actions.CopyReferenceAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.psi.PsiFile

object ElementAction : CopyReferenceAction() {

    @JvmStatic
    val languages = arrayOf("RegExp", "JSON", "yaml", "HTML")

    @JvmStatic
    val INJECTED_PSI_ELEMENT: DataKey<PsiFile> = DataKey.create("\$injected\$.psi.File")


    override fun update(e: AnActionEvent) {
        e.presentation.text = DrawGraphBundle.message("element.graph")
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val psiFile = event.getData(INJECTED_PSI_ELEMENT) ?: return
        RelController.forInjectedElement(project, psiFile)
    }
}