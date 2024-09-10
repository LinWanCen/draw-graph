package com.github.linwancen.plugin.graph.action

import com.github.linwancen.plugin.graph.ui.DrawGraphBundle
import com.github.linwancen.plugin.graph.ui.RelController
import com.intellij.ide.actions.CopyReferenceAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

object FileMethodCallAction : CopyReferenceAction() {

    override fun update(e: AnActionEvent) {
        e.presentation.text = DrawGraphBundle.message("file.method.call.graph")
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val files = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return
        RelController.forFile(project, files, true)
    }
}