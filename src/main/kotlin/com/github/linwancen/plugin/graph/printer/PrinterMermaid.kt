package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
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
        sb.append("\nsubgraph ")
        label(groupMap, false)
        sb.append("  direction  ${if (DrawGraphAppState.of().lr) "LR" else "TB"}\n")
    }

    override fun afterGroup(groupMap: MutableMap<String, String>) {
        sb.append("end\n")
    }

    override fun item(itemMap: MutableMap<String, String>) {
        label(itemMap, true)
    }

    private fun label(itemMap: MutableMap<String, String>, isItem: Boolean) {
        sb.append("  '${sign(itemMap["sign"] ?: return)}'")
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
        val link = itemMap["link"]?.replace(")", "") ?: ""
        sb.append("  click '${sign(itemMap["sign"] ?: return)}' call navigate(\"$link\")")
        sb.append("\n\n")
    }

    override fun call(usageSign: String, callSign: String) {
        sb.append("\n'${sign(usageSign)}' --> '${sign(callSign)}'")
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
        fun build(data: PrinterData, func: Consumer<String>?) {
            object : Task.Backgroundable(data.project, "draw Mermaid") {
                override fun run(indicator: ProgressIndicator) {
                    val src = data.src ?: return
                    if (StringUtils.isBlank(src)) {
                        return
                    }
                    // language="html"
                    val appState = DrawGraphAppState.of()
                    val offline = temp(
                        src, """
<!-- https://cdn.jsdelivr.net/npm/mermaid@9.4.3/dist/mermaid.js -->
<!-- change it in DrawGraphSetting.properties -->
<script src="${appState.mermaidOffline}"></script>
"""
                    )
                    // language="html"
                    val online = temp(
                        src, """
<script src="${appState.mermaidOnline}"></script>
"""
                    )
                    val path = appState.tempPath
                    try {
                        if (appState.online) {
                            func?.accept(online)
                        } else {
                            func?.accept(offline)
                        }

                        File(path).mkdirs()
                        val offlinePath = "$path/mermaid-offline.html"
                        val onlinePath = "$path/mermaid-online.html"
                        Files.write(Path.of(offlinePath), offline.toByteArray(StandardCharsets.UTF_8))
                        Files.write(Path.of(onlinePath), online.toByteArray(StandardCharsets.UTF_8))
                    } catch (e: Exception) {
                        func?.accept("${offline}\n${ExceptionUtil.getThrowableText(e)}")
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