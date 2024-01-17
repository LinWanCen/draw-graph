package com.github.linwancen.plugin.graph.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.github.linwancen.plugin.graph.settings.DrawGraphAppState",
    storages = [Storage("draw-graph-settings/DrawGraphAppState.xml")]
)
class DrawGraphAppState : PersistentStateComponent<DrawGraphAppState?>, AbstractDrawGraphState() {
    override fun getState(): DrawGraphAppState {
        return this
    }

    override fun loadState(state: DrawGraphAppState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        @JvmStatic
        fun of(project: Project? = null): DrawGraphAppState {
            val manager = project ?: ApplicationManager.getApplication()
            return manager.getService(DrawGraphAppState::class.java) ?: return DrawGraphAppState()
        }
    }
}