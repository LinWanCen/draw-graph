package com.github.linwancen.plugin.graph.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.github.linwancen.plugin.graph.settings.ProjectState",
    storages = [Storage("draw-graph-settings/DrawGraphProjectState.xml")]
)
class DrawGraphProjectState : PersistentStateComponent<DrawGraphProjectState?> {
    var autoLoad : Boolean = true

    override fun getState(): DrawGraphProjectState {
        return this
    }

    override fun loadState(state: DrawGraphProjectState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        @JvmStatic
        fun of(project: Project? = null): DrawGraphProjectState {
            val manager = project ?: ApplicationManager.getApplication()
            return manager.getService(DrawGraphProjectState::class.java) ?: return DrawGraphProjectState()
        }
    }
}