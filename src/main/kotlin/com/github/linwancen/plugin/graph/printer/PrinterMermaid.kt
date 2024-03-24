package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.common.file.SysPath
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
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

    val sb = StringBuilder("graph ${if (DrawGraphAppState.of().lr) "LR" else "TB"}\n\n")

    override fun beforeGroup(groupMap: MutableMap<String, String>) {
        sb.append("subgraph ")
        label(groupMap, false)
        sb.append("direction  ${if (DrawGraphAppState.of().lr) "LR" else "TB"}\n")
    }

    override fun afterGroup(groupMap: MutableMap<String, String>) {
        sb.append("end\n\n")
    }

    override fun item(itemMap: MutableMap<String, String>) {
        label(itemMap, true)
    }

    private fun label(itemMap: MutableMap<String, String>, isItem: Boolean) {
        sb.append("'${sign(itemMap["sign"] ?: return)}'")
        if (itemMap["name"] != null) {
            sb.append(if (isItem) "(" else "[")
            sb.append("\"")
            if (!isItem) {
                sb.append("${itemMap["name"]}\n")
            }
            addLine(itemMap["@1"], sb)
            addLine(itemMap["@2"], sb)
            addLine(itemMap["@3"], sb)
            if (isItem) {
                sb.append("${itemMap["name"]}")
            }
            sb.append("\"")
            sb.append(if (isItem) ")" else "]")
        }
        sb.append("\n")
         val name = if (isItem) {
            val onlyName = itemMap["name"]
            onlyName?.substring(onlyName.lastIndexOf(' ') + 1) ?: ""
        } else {
            ""
        }
        sb.append("click '${sign(itemMap["sign"] ?: return)}' call navigate(\"${itemMap["filePath"]}#${name}\")")
        sb.append("\n\n")
    }

    override fun call(usageSign: String, callSign: String) {
        sb.append("'${sign(usageSign)}' --> '${sign(callSign)}'\n")
    }

    override fun toSrc(relData: RelData): String {
        printerData(relData)
        return sb.toString()
    }

    override fun toHtml(src: String, project: Project, func: Consumer<String>) {
        PrinterGraphviz.build(src, project, func)
    }

    companion object {
        @JvmStatic
        fun build(src: String?, project: Project, func: Consumer<String>) {
            object : Task.Backgroundable(project, "draw Mermaid") {
                override fun run(indicator: ProgressIndicator) {
                    if (StringUtils.isBlank(src ?: return)) {
                        return
                    }
                    //language="html"
                    val offline = temp(src,"""
<!-- https://cdn.jsdelivr.net/npm/mermaid@9.4.3/dist/mermaid.js -->
<script src="file:///D:/draw-graph/mermaid.js"></script>
<script src="file:///C:/Users/Public/draw-graph/mermaid.js"></script>
<script src="file:///Applications/draw-graph/mermaid.js"></script>
<script src="file:///var/lib/draw-graph/mermaid.js"></script>
<script src="file:///usr/lib/draw-graph/mermaid.js"></script>
""")
                    //language="html"
                    val online = temp(src, """
<script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.js"></script>
""")
                    val paths = SysPath.lib() ?: return
                    for (path in paths) {
                        if (!File(path).exists()) {
                            continue
                        }
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

        private fun temp(src: String?, mermaidLink: String?): String {
            //language="html"
            val online = """<pre class="mermaid">

$src
</pre>
$mermaidLink
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
  const config = {
    startOnLoad: true,
    maxTextSize: Number.MAX_VALUE,
    flowchart: { useMaxWidth: true, htmlLabels: true, curve: 'cardinal' },
    securityLevel: 'loose',
  };
  mermaid.initialize(config);
</script>
<button onclick='openDevtools()'>openDevtools</button><br>
${DrawGraphBundle.message("mermaid.msg")}
"""
            return online
        }
    }
}