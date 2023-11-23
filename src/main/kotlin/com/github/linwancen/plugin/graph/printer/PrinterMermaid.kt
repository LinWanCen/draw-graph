package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.common.file.SysPath
import com.github.linwancen.plugin.graph.ui.DrawGraphBundle
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

class PrinterMermaid : Printer() {

    val sb = StringBuilder()

    override fun beforeGroup(groupMap: MutableMap<String, String>) {
        sb.append("subgraph ")
        item(groupMap)
        sb.append("direction LR\n")
    }

    override fun afterGroup(groupMap: MutableMap<String, String>) {
        sb.append("end\n\n")
    }

    override fun item(itemMap: MutableMap<String, String>) {
        sb.append(sign(itemMap["sign"] ?: return))
        if (itemMap["name"] != null) {
            sb.append("[\"")
            addLine(itemMap["@1"], sb)
            addLine(itemMap["@2"], sb)
            addLine(itemMap["@3"], sb)
            sb.append("${itemMap["name"]}\"]")
        }
        sb.append("\n")
    }

    override fun call(usageSign: String, callSign: String) {
        sb.append("${sign(usageSign)} --> ${sign(callSign)}\n")
    }

    override fun src(): String? {
        if (sb.isBlank()) {
            return null
        }
        sb.insert(0, "graph LR\n")
        return sb.toString()
    }

    companion object {
        /**
         * [parse error with word graph #4079](https://github.com/mermaid-js/mermaid/issues/4079)
         */
        @JvmStatic
        val keyword = Regex("\\b(graph|end)\\b")

        @JvmStatic
        private fun sign(input: String) = "'${deleteSymbol(keyword.replace(input, "$1_"))}'"

        /**
         * [support not english symbol #4138](https://github.com/mermaid-js/mermaid/issues/4138)
         */
        @JvmStatic
        val canNotUseSymbol = Regex("[。？！，、；：“”‘’（）《》【】~@()|'\"<{}\\[\\]]")
        private fun deleteSymbol(input: String) = canNotUseSymbol.replace(input, "_")

        @JvmStatic
        private fun addLine(s: String?, sb: StringBuilder) {
            if (s != null) {
                sb.append("${s.replace("\"","")}\\n")
            }
        }

        @JvmStatic
        fun build(src : String?, project: Project, func : Consumer<String>) {
            if (StringUtils.isBlank(src ?: return)) {
                return
            }
            val offline = """
<pre class="mermaid">

$src
</pre>
<!-- https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.js -->
<script src="file:///D:/draw-graph/mermaid.js"></script>
<script src="file:///C:/Users/Public/draw-graph/mermaid.js"></script>
<script src="file:///Applications/draw-graph/mermaid.js"></script>
<script src="file:///var/lib/draw-graph/mermaid.js"></script>
<script src="file:///usr/lib/draw-graph/mermaid.js"></script>
<script type="module">
  mermaid.initialize({ startOnLoad: true });
</script>
${DrawGraphBundle.message("mermaid.msg")}
"""
            val online = """
<pre class="mermaid">

graph LR
$src
</pre>
<!-- https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.js -->
<script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.js"></script>
<script type="module">
  mermaid.initialize({ startOnLoad: true });
</script>
${DrawGraphBundle.message("mermaid.msg")}
"""
            val paths = SysPath.lib() ?: return
            object : Task.Backgroundable(project, "draw Mermaid") {
                override fun run(indicator: ProgressIndicator) {
                    for (path in paths) {
                        try {
                            val onlinePath = path + "draw-graph/mermaid-online.html"
                            File(onlinePath).parentFile.mkdirs()
                            Files.write(Path.of(onlinePath), online.toByteArray(StandardCharsets.UTF_8))
                            val offlinePath = path + "draw-graph/mermaid-offline.html"
                            File(offlinePath).parentFile.mkdirs()
                            Files.write(Path.of(offlinePath), offline.toByteArray(StandardCharsets.UTF_8))
                            func.accept(offline)
                            return
                        } catch (e: Exception) {
                            func.accept("${offline}\n${ExceptionUtil.getThrowableText(e)}")
                        }
                    }
                }
            }.queue()
        }
    }
}