package com.github.linwancen.plugin.graph.listeners

import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.github.linwancen.plugin.graph.ui.RelController
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.DumbService

object TabListener : FileEditorManagerListener {

    override fun selectionChanged(event: FileEditorManagerEvent) {
        val project = event.manager.project
        if (DumbService.isDumb(project)) {
            return
        }
        if (DrawGraphProjectState.of(project).autoLoad) {
            RelController.forFile(project, arrayOf(event.newFile ?: return), false)
        }
    }
}