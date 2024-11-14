package com.github.linwancen.plugin.graph.action

import com.github.linwancen.plugin.graph.ui.DrawGraphBundle
import com.github.linwancen.plugin.graph.ui.RelController
import com.intellij.ide.actions.CopyReferenceAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

object MethodUsageAction : CopyReferenceAction() {

    override fun update(e: AnActionEvent) {
        e.presentation.text = DrawGraphBundle.message("method.usage.graph")
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val psiElement = event.getData(CommonDataKeys.PSI_ELEMENT) ?: return
        RelController.forElement(project, psiElement, call())
    }

    private fun call() = false
}