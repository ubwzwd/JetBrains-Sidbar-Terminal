package com.example.sidebarterminal

import com.intellij.icons.AllIcons
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.terminal.ui.TerminalWidget
import com.intellij.ui.content.ContentFactory
import org.jetbrains.plugins.terminal.LocalTerminalDirectRunner
import org.jetbrains.plugins.terminal.ShellStartupOptions
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import javax.swing.SwingUtilities
import javax.swing.text.JTextComponent

class SidebarTerminalToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        addTerminalTab(project, toolWindow)

        val newTabAction = object : DumbAwareAction("New Terminal Tab", "Open a new terminal tab", AllIcons.General.Add) {
            override fun actionPerformed(e: AnActionEvent) {
                addTerminalTab(project, toolWindow)
            }
        }
        (toolWindow as? ToolWindowEx)?.setTabActions(newTabAction)
    }

    private fun addTerminalTab(project: Project, toolWindow: ToolWindow) {
        // 每个 tab 独立的生命周期：关闭 tab 时结束对应的 shell 进程和 Esc 拦截器
        val tabDisposable = Disposer.newDisposable("SidebarTerminalTab")

        val runner = LocalTerminalDirectRunner.createTerminalRunner(project)
        val startupOptions = ShellStartupOptions.Builder()
            .workingDirectory(project.basePath)
            .build()
        val widget = runner.startShellTerminalWidget(tabDisposable, startupOptions, true)

        val command = SidebarTerminalSettings.getInstance(project).state.startupCommand
        if (command.isNotBlank()) {
            // 在 shell 会话就绪后自动执行；未就绪前会排队等待
            widget.sendCommandToExecute(command)
        }

        registerEscapeInterceptor(widget, tabDisposable)

        val contentManager = toolWindow.contentManager
        val title = "Terminal ${contentManager.contentCount + 1}"
        val content = ContentFactory.getInstance().createContent(widget.component, title, false)
        content.setDisposer(tabDisposable)
        content.isCloseable = true
        contentManager.addContent(content)
        contentManager.setSelectedContent(content)
        widget.requestFocus()
    }

    /**
     * 默认情况下 IDE 会把工具窗口里的 Esc 处理为 "Focus Editor"（跳回编辑器），
     * 终端收不到按键。在 IdeEventQueue 上挂事件分发器——键盘事件进入 IDE 的第一站，
     * 早于所有 IDE 动作匹配——只要焦点在本终端内就消费掉 Esc，并把 ESC (0x1B)
     * 直接写入 tty，让 claude 这类 TUI 程序正常响应。
     */
    private fun registerEscapeInterceptor(widget: TerminalWidget, parentDisposable: Disposable) {
        IdeEventQueue.getInstance().addDispatcher(
            { event ->
                if (event is KeyEvent && isEscape(event) && isFocusInTerminal(widget)) {
                    if (event.id == KeyEvent.KEY_PRESSED) {
                        widget.ttyConnectorAccessor.executeWithTtyConnector { tty ->
                            tty.write(byteArrayOf(0x1B))
                        }
                    }
                    event.consume()
                    true
                } else {
                    false
                }
            },
            parentDisposable,
        )
    }

    private fun isEscape(event: KeyEvent): Boolean =
        event.keyCode == KeyEvent.VK_ESCAPE || event.keyChar.code == 0x1B

    private fun isFocusInTerminal(widget: TerminalWidget): Boolean {
        val focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner ?: return false
        // 终端内的搜索输入框等文本组件里，Esc 保持默认行为（关闭搜索）
        if (focusOwner is JTextComponent) return false
        return SwingUtilities.isDescendingFrom(focusOwner, widget.component)
    }
}
