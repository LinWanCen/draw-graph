package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
import com.github.linwancen.plugin.graph.settings.Setting
import com.github.linwancen.plugin.graph.ui.DrawGraphBundle
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
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
        sb.append("click '${sign(itemMap["sign"] ?: return)}' call navigate(\"${itemMap["link"]}\")")
        sb.append("\n\n")
    }

    override fun call(usageSign: String, callSign: String) {
        sb.append("'${sign(usageSign)}' --> '${sign(callSign)}'\n")
    }

    override fun sign(input: String): String {
        val deleteKeyword = keyword.replace(input, "$1_")
        return canNotUseSymbol.replace(deleteKeyword, "_")
    }

    override fun toSrc(relData: RelData): Pair<String, String> {
        printerData(relData)
        return Pair(sb.toString(), "")
    }

    companion object {
        /**
         * [parse error with word graph #4079](https://github.com/mermaid-js/mermaid/issues/4079)
         */
        @JvmStatic
        val keyword = Regex("\\b(graph|end|parent)\\b")

        @JvmStatic
        fun build(data: PrinterData, func: Consumer<String>) {
            object : Task.Backgroundable(data.project, "draw Mermaid") {
                override fun run(indicator: ProgressIndicator) {
                    val src = data.src ?: return
                    if (StringUtils.isBlank(src)) {
                        return
                    }
                    // language="html"
                    val offline = temp(
                        src, """
<!-- https://cdn.jsdelivr.net/npm/mermaid@9.4.3/dist/mermaid.js -->
<!-- change it in DrawGraphSetting.properties -->
<script src="${if (DrawGraphAppState.of().online) Setting.message("mermaid_js_link") else DrawGraphAppState.of().mermaidLink}"></script>
"""
                    )
                    // language="html"
                    val online = temp(
                        src, """
<script src="${Setting.message("mermaid_js_link")}"></script>
"""
                    )
                    val path = DrawGraphAppState.of().tempPath
                    try {
                        val onlinePath = "$path/mermaid-online.html"
                        File(onlinePath).parentFile.mkdirs()
                        Files.write(Path.of(onlinePath), online.toByteArray(StandardCharsets.UTF_8))
                        val offlinePath = "$path/mermaid-offline.html"
                        File(offlinePath).parentFile.mkdirs()
                        Files.write(Path.of(offlinePath), offline.toByteArray(StandardCharsets.UTF_8))
                        func.accept(offline)
                        return
                    } catch (e: Exception) {
                        func.accept("${offline}\n${ExceptionUtil.getThrowableText(e)}")
                    }
                }
            }.queue()
        }

        private fun temp(src: String?, mermaidLink: String?): String {
            // language="html"
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
<button onclick='openDevtools()'>openDevtools</button>
<br>
${DrawGraphBundle.message("mermaid.msg")}
"""
            return online
        }
    }
}