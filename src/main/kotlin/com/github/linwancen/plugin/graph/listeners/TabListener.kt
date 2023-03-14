package com.github.linwancen.plugin.graph.listeners

import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.github.linwancen.plugin.graph.ui.RelController
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile

object TabListener : FileEditorManagerListener {
    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        val project = source.project
        if (DrawGraphProjectState.of(project).autoLoad) {
            RelController.forFile(project, arrayOf(file))
        }
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        val project = event.manager.project
        if (DrawGraphProjectState.of(project).autoLoad) {
            RelController.forFile(project, arrayOf(event.newFile ?: return))
        }
    }
}