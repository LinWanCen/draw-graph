# draw-graph

![Build](https://github.com/LinWanCen/draw-graph/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/21242-draw-graph.svg)](https://plugins.jetbrains.com/plugin/21242-draw-graph)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/21242-draw-graph.svg)](https://plugins.jetbrains.com/plugin/21242-draw-graph)

## Plugin description 插件介绍

<!-- Plugin description -->
Method call usage graph and maven dependency graph, click to navigate

生成 方法调用图 和 Maven 依赖图，点击跳转

- Java, Kotlin, Groovy, Scala
- C/C++/OC, Python, Go, Rust, Ruby
- JS/TS, PHP

#### English Desc

##### How to Use

One file call graph or all pom.xml dep graph:
1. Open <kbd>Graph</kbd> ToolWindow at Right
2. Open pom.xml/.java/.py... file

Multi file call graph or partial pom.xml dep graph:
1. Select multi files in the same language
2. Open Right Click Menu
3. Select <kbd>Call Graph</kbd>

Install [Graphviz] and set xx/bin to env Path for PlantUML and Graphviz.

##### About

Only support 2020.2+ because mermaid.js only use in jcef(chrome),  
2020.1- is JavaFx WebView.

##### My Plugin
- Show doc comment in the Project view Tree, line End, json etc.: [Show Comment]
- show line count for file / method, show children count for dir in project view：[Line Num]
- Method call usage graph and maven dependency graph: [Draw Graph]
- Find author/comment of multiple files or lines and export Find: [Find Author]
- Auto sync coverage and capture coverage during debug: [Sync Coverage]

---

#### 中文描述

##### 用法

单个文件调用图 或 所有 pom.xml 依赖图：
1. 打开右边的<kbd>图</kbd>工具栏
2. 打开 pom.xml/.java/.py 等文件

多个文件调用图 或 部分 pom.xml 依赖图：
1. 选择多个同语言的文件
2. 打开右键菜单
3. 选择<kbd>调用图</kbd>

安装 [Graphviz] 并设置 bin 目录为环境变量以便使用 PlantUML 和 Graphviz。

##### 关于

只支持 2020.2 以上因为 mermaid.js 只能在 jcef(chrome) 中使用，  
2020.1 以下用的是 JavaFx WebView。

##### 我的项目
- 在文件树、行末、JSON 显示注释：[Show Comment]
- 在文件树显示行数、文件数：[Line Num]
- 生成 方法调用图 和 Maven 依赖图：[Draw Graph]
- 查找多个文件或行的作者 与 导出搜索：[Find Author]
- 自动同步覆盖率 和 调试中抓取覆盖率：[Sync Coverage]

---

#### 支持

如果对你有所帮助，可以通过群或文章等形式分享给大家，在插件市场好评，
或者给本项目 [本项目 GitHub 主页][Draw Graph GitHub] 一个 Star，您的支持是项目前进的动力。

[Graphviz]: https://graphviz.org/download/
[Show Comment]: https://plugins.jetbrains.com/plugin/18553-show-comment
[Line Num]: https://plugins.jetbrains.com/plugin/23300-line-num
[Draw Graph]: https://plugins.jetbrains.com/plugin/21242-draw-graph
[Find Author]: https://plugins.jetbrains.com/plugin/20557-find-author
[Sync Coverage]: https://plugins.jetbrains.com/plugin/20780-sync-coverage
[Draw Graph GitHub]: https://github.com/LinWanCen/draw-graph

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "draw-graph"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/LinWanCen/draw-graph/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

[Changelog 更新说明](CHANGELOG.md)

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template