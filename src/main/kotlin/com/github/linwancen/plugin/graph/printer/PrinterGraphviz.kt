package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
import com.github.linwancen.plugin.graph.ui.DrawGraphBundle
import com.intellij.execution.CommandLineUtil
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
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
${if (DrawGraphAppState.of().lr) "rankdir=LR" else ""}
fontname = "Microsoft YaHei,Consolas"
node [shape = "record", style="rounded,filled", fillcolor = "#F1F1F1", fontname = "Microsoft YaHei,Consolas"]
edge [arrowhead = "empty", fontname = "Microsoft YaHei,Consolas"]
graph [compound=true]

"""
    )
    val js = StringBuilder()
    var nodeId = 1
    var clusterId = 1

    override fun beforeGroup(groupMap: MutableMap<String, String>) {
        sb.append(
            "subgraph \"cluster_${sign(groupMap["sign"] ?: return)}\" {\n" +
                    "  edge [\"dir\"=\"none\"]\n"
                    + "  graph [style=\"rounded\"]\n"
        )
        label(groupMap, false)
        sb.append("\n")
    }

    override fun afterGroup(groupMap: MutableMap<String, String>) {
        sb.append("}\n\n")
    }

    override fun item(itemMap: MutableMap<String, String>) {
        sb.append("  \"cluster_${sign(itemMap["sign"] ?: return)}\"")
        label(itemMap, true)
        sb.append("\n")
    }

    private fun label(map: Map<String, String>, isItem: Boolean) {
        map["name"] ?: return
        if (isItem) {
            sb.append('[')
        } else {
            sb.append("  ")
        }
        sb.append("label =\" ")
        addLine(map["@1"], sb, true)
        addLine(map["@2"], sb, true)
        addLine(map["@3"], sb, true)
        sb.append("${map["name"]}\"")
        if (isItem) {
            sb.append(']')
        }
        val id = if (isItem) "node${nodeId++}" else "clust${clusterId++}"
        js.append("  document.getElementById(\"$id\").onclick = function(){ navigate(\"${map["link"]}\") }\n")
    }

    override fun call(usageSign: String, callSign: String) {
        sb.append("\n\"cluster_${sign(usageSign)}\" -> \"cluster_${sign(callSign)}\"")
    }

    override fun toSrc(relData: RelData): Pair<String, String> {
        printerData(relData)
        sb.append("\n}")
        return Pair(sb.toString(), js.toString())
    }

    companion object {

        @JvmStatic
        fun build(data: PrinterData, func: Consumer<String>?) {
            object : Task.Backgroundable(data.project, "draw Graphviz") {
                override fun run(indicator: ProgressIndicator) {
                    val src = data.src ?: return
                    if (StringUtils.isBlank(src)) {
                        return
                    }
                    val path = DrawGraphAppState.of().tempPath
                    try {
                        File(path).mkdirs()
                        val dotPath = "$path/graphviz.dot"
                        Files.write(Path.of(dotPath), src.toByteArray(StandardCharsets.UTF_8))
                        if (func == null) {
                            return
                        }
                        val commandLine =
                            CommandLineUtil.toCommandLine("dot", arrayListOf("-Tsvg", "-Tpng", "-O", dotPath))
                        val generalCommandLine = GeneralCommandLine(commandLine)
                        generalCommandLine.charset = StandardCharsets.UTF_8
                        generalCommandLine.setWorkDirectory(path)
                        val commandLineOutputStr = ScriptRunnerUtil.getProcessOutput(generalCommandLine)
                        val svgFile = "$path/graphviz.dot.svg"
                        val svg = Files.readString(Path.of(svgFile), StandardCharsets.UTF_8)
                        func.accept(
                            // language="html"
                            """
<!-- <embed src="file:///$path/graphviz.dot.svg" type="image/svg+xml" /> -->
$svg
<br>
<script>
  function navigate(link) {
    callJava('navigate:' + link)
  }
  function openDevtools() {
    callJava('openDevtools')
  }
  function callJava(cmd) {
    window.java({
      request: cmd,
      onSuccess(response){
        console.log(response);
      },
      onFailure(error_code,error_message){
        console.log(error_code,error_message);
      }
    });
  }
  window.onload = function addEvent() {

${data.js ?: ""}
  }
</script>
<button onclick='openDevtools()'>openDevtools</button>
<br>
${DrawGraphBundle.message("graphviz.msg")}
<br>
$commandLineOutputStr
"""
                        )
                        return
                    } catch (e: Exception) {
                        func?.accept(
                            // language="html"
                            """
<embed src="file:///$path/graphviz.dot.svg" type="image/svg+xml" />
<br>
${DrawGraphBundle.message("graphviz.msg")}
<br>
${ExceptionUtil.getThrowableText(e)}
"""
                        )
                    }
                }
            }.queue()
        }
    }
}