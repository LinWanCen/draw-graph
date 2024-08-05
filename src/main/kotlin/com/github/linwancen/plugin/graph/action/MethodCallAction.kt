package com.github.linwancen.plugin.graph.action

import com.github.linwancen.plugin.graph.ui.DrawGraphBundle
import com.github.linwancen.plugin.graph.ui.RelController
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction

object MethodCallAction : DumbAwareAction() {

    override fun update(e: AnActionEvent) {
        e.presentation.text = DrawGraphBundle.message("method.call.graph")
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val psiElement = event.getData(CommonDataKeys.PSI_ELEMENT) ?: return
        RelController.forElement(project, psiElement, true)
    }
}