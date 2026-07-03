package com.example.sidebarterminal

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(name = "SidebarTerminalSettings", storages = [Storage("sidebarTerminal.xml")])
class SidebarTerminalSettings : PersistentStateComponent<SidebarTerminalSettings.State> {

    class State {
        var startupCommand: String = "claude"
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(project: Project): SidebarTerminalSettings = project.service()
    }
}
