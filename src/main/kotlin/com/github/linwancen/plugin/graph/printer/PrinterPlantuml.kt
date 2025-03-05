package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.util.ExceptionUtil
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer


class PrinterPlantuml : Printer() {

    val sb = StringBuilder(
        """@startuml
hide empty circle
hide empty members
${if (DrawGraphAppState.of().lr) "left to right direction" else ""}
skinparam shadowing false
skinparam componentStyle rectangle
skinparam defaultTextAlignment center

"""
    )
    val js = StringBuilder()

    override fun beforeGroup(groupMap: MutableMap<String, String>) {
        label(groupMap, false)
        sb.append(" {\n")
    }

    override fun afterGroup(groupMap: MutableMap<String, String>) {
        sb.append("}\n\n")
    }

    override fun item(itemMap: MutableMap<String, String>) {
        label(itemMap, true)
        sb.append("\n")
    }

    private fun label(map: MutableMap<String, String>, isItem: Boolean) {
        val sign = sign(map["sign"] ?: return)
        if (isItem) sb.append("  ")
        sb.append("component $sign")
        if (map["name"] != null) {
            sb.append(" as \" ")
            addLine(map["@1"], sb, true)
            addLine(map["@2"], sb, true)
            addLine(map["@3"], sb, true)
            sb.append("${map["name"]}\"")
        }
        val id = "${if (isItem) "elem" else "cluster"}_$sign"
        js.append("  document.getElementById(\"$id\").onclick = function(){ navigate(\"${map["link"]}\") }\n")
    }

    override fun call(usageSign: String, callSign: String) {
        sb.append("\n${sign(usageSign)} --> ${sign(callSign)}")
    }

    override fun toSrc(relData: RelData): Pair<String, String> {
        printerData(relData)
        sb.append("\n@enduml")
        return Pair(sb.toString(), js.toString())
    }

    companion object {
        @JvmStatic
        fun build(data: PrinterData, func: Consumer<String>?) {
            object : Task.Backgroundable(data.project, "draw PlantUML") {
                override fun run(indicator: ProgressIndicator) {
                    val src = data.src ?: return
                    if (StringUtils.isBlank(src)) {
                        return
                    }
                    val path = DrawGraphAppState.of().tempPath
                    try {
                        File(path).mkdirs()

                        val plantumlPath = "$path/plantuml.puml"
                        Files.write(Path.of(plantumlPath), src.toByteArray(StandardCharsets.UTF_8))
                        if (func == null) {
                            return
                        }
                        val svgFile = "$path/plantuml.svg"
                        val svgOut = FileOutputStream(svgFile)
                        val reader = SourceStringReader(src)
                        val svgDesc = reader.outputImage(svgOut, FileFormatOption(FileFormat.SVG))
                        val svg = Files.readString(Path.of(svgFile), StandardCharsets.UTF_8)
                        func.accept(
                            // language="html"
                            """
<!-- <embed src="file:///${path}/plantuml.svg" type="image/svg+xml" /> -->
$svg
<br>
$svgDesc
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

  // keep it for the same name class/method
${data.js ?: ""}

   let elements = document.getElementsByTagName("g");
    for (let g of elements) {
      const id = g.getAttribute("id");
      if(id == null) {
        continue;
      }
      if(g.onclick != null) {
        continue;
      }
      if (id.startsWith("elem_")){
        g.onclick = function() { navigate(id.substring(5).replace('_', '#'))};
      } else if (id.startsWith("cluster_")) {
        g.onclick = function() { navigate(id.substring(8).replace('_', '#'))};
      }
    }
  }
</script>
<button onclick='openDevtools()'>openDevtools</button>
<br>
"""
                        )
                        val pngFile = "$path/plantuml.png"
                        val pngOut = FileOutputStream(pngFile)
                        reader.outputImage(pngOut, FileFormatOption(FileFormat.PNG))
                    } catch (e: Exception) {
                        func?.accept(
                            // language="html"
                            """
<embed src="file:///${path}/plantuml.svg" type="image/svg+xml" />
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