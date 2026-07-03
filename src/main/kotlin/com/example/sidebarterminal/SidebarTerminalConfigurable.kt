package com.example.sidebarterminal

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel

class SidebarTerminalConfigurable(private val project: Project) : BoundConfigurable("Sidebar Terminal") {

    override fun createPanel(): DialogPanel {
        val settings = SidebarTerminalSettings.getInstance(project)
        return panel {
            row("Startup command:") {
                textField()
                    .bindText(settings.state::startupCommand)
                    .columns(40)
                    .comment("Executed automatically when the Sidebar Terminal tool window is opened. Requires reopening the tool window (or the project) to take effect.")
            }
        }
    }
}
