# Sidebar Terminal

一个极简的 JetBrains 插件（GoLand / IDEA 等均可用）：在右侧边栏（和 Database 同一排）添加一个独立的
**Sidebar Terminal** 工具窗口，打开时自动执行一条配置好的命令。

## 功能

- 右侧边栏独立终端工具窗口，与编辑器区域完全分开
- 使用 IDE 内置终端组件，体验和自带 Terminal 一致（工作目录为项目根目录）
- 首次打开工具窗口时自动执行配置的启动命令

## 配置启动命令

`Settings/Preferences → Tools → Sidebar Terminal → Startup command`

修改后重新打开工具窗口（或重开项目）生效。

## 构建

需要 JDK 21（首次构建会下载 GoLand SDK，耗时较长）：

```bash
./gradlew buildPlugin
```

产物在 `build/distributions/sidebar-terminal-plugin-0.1.0.zip`。

## 安装

GoLand → `Settings → Plugins → ⚙️ → Install Plugin from Disk...` → 选择上面的 zip → 重启 IDE。

## 调试运行

```bash
./gradlew runIde
```

会启动一个装好本插件的沙箱 GoLand 实例。
