package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.common.file.SysPath
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.ui.DrawGraphBundle
import com.intellij.execution.CommandLineUtil
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.ExceptionUtil
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer


class PrinterGraphviz : Printer() {

    val sb = StringBuilder(
        """digraph{
rankdir=LR
fontname = "Microsoft YaHei,Consolas"
node [shape = "record", style="rounded,filled", fillcolor = "#F1F1F1", fontname = "Microsoft YaHei,Consolas"]
edge [arrowhead = "empty", fontname = "Microsoft YaHei,Consolas"]
graph [compound=true]

"""
    )

    override fun beforeGroup(groupMap: MutableMap<String, String>) {
        sb.append(
            "subgraph \"cluster_${sign(groupMap["sign"] ?: return)}\" {\n" +
                    "edge [\"dir\"=\"none\"]\n"
                    + "graph [style=\"rounded\"]\n"
        )
        label(groupMap, false)
        sb.append("\n")
    }

    override fun afterGroup(groupMap: MutableMap<String, String>) {
        sb.append("}\n\n")
    }

    override fun item(itemMap: MutableMap<String, String>) {
        sb.append("\"cluster_${sign(itemMap["sign"] ?: return)}\"")
        label(itemMap, true)
        sb.append("\n")
    }

    private fun label(map: Map<String, String>, isItem: Boolean) {
        if (map["name"] != null) {
            if (isItem) {
                sb.append('[')
            }
            sb.append("label =\" ")
            addLine(map["@1"], sb)
            addLine(map["@2"], sb)
            addLine(map["@3"], sb)
            sb.append("${map["name"]}\"")
            if (isItem) {
                sb.append(']')
            }
        }
    }

    override fun call(usageSign: String, callSign: String) {
        sb.append("\"cluster_${sign(usageSign)}\" -> \"cluster_${sign(callSign)}\"\n")
    }

    override fun toSrc(relData: RelData): String {
        printerData(relData)
        sb.append("\n}")
        return sb.toString()
    }

    override fun toHtml(src: String, project: Project, func: Consumer<String>) {
        build(src, project, func)
    }

    companion object {

        @JvmStatic
        fun build(src: String?, project: Project, func: Consumer<String>) {
            if (StringUtils.isBlank(src ?: return)) {
                return
            }
            val paths = SysPath.lib() ?: return
            object : Task.Backgroundable(project, "draw Graphviz") {
                override fun run(indicator: ProgressIndicator) {
                    for (path in paths) {
                        try {
                            val dotPath = "${path}draw-graph/graphviz.dot"
                            File(dotPath).parentFile.mkdirs()
                            Files.write(Path.of(dotPath), src.toByteArray(StandardCharsets.UTF_8))
                            val commandLine =
                                CommandLineUtil.toCommandLine("dot", arrayListOf("-Tsvg", "-Tpng", "-O", dotPath))
                            val generalCommandLine = GeneralCommandLine(commandLine)
                            generalCommandLine.charset = StandardCharsets.UTF_8
                            generalCommandLine.setWorkDirectory(path)
                            val commandLineOutputStr = ScriptRunnerUtil.getProcessOutput(generalCommandLine)
                            func.accept(
                                """
<embed src="file:///${path}draw-graph/graphviz.dot.svg" type="image/svg+xml" />
<br>
${DrawGraphBundle.message("graphviz.msg")}
<br>
$commandLineOutputStr
"""
                            )
                            return
                        } catch (e: Exception) {

                            func.accept(
                                """
<embed src="file:///${path}draw-graph/graphviz.dot.svg" type="image/svg+xml" />
<br>
${DrawGraphBundle.message("graphviz.msg")}
<br>
${ExceptionUtil.getThrowableText(e)}
"""
                            )
                        }
                    }
                }
            }.queue()
        }
    }
}