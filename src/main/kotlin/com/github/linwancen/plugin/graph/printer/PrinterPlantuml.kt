package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.common.file.SysPath
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
import com.github.linwancen.plugin.graph.ui.DrawGraphBundle
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
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

    val sb = StringBuilder("""@startuml
hide empty circle
hide empty members
${if (DrawGraphAppState.of().lr) "left to right direction" else ""}
skinparam shadowing false
skinparam componentStyle rectangle
skinparam defaultTextAlignment center

""")

    override fun beforeGroup(groupMap: MutableMap<String, String>) {
        label(groupMap)
        sb.append(" {\n")
    }

    override fun afterGroup(groupMap: MutableMap<String, String>) {
        sb.append("}\n\n")
    }

    override fun item(itemMap: MutableMap<String, String>) {
        label(itemMap)
        sb.append("\n")
    }

    private fun label(map: MutableMap<String, String>) {
        sb.append("component ${sign(map["sign"] ?: return)}")
        if (map["name"] != null) {
            sb.append(" as \" ")
            addLine(map["@1"], sb)
            addLine(map["@2"], sb)
            addLine(map["@3"], sb)
            sb.append("${map["name"]}\"")
        }
    }

    override fun call(usageSign: String, callSign: String) {
        sb.append("${sign(usageSign)} --> ${sign(callSign)}\n")
    }

    override fun toSrc(relData: RelData): String {
        printerData(relData)
        sb.append("\n@enduml")
        return sb.toString()
    }

    override fun toHtml(src: String, project: Project, func: Consumer<String>) {
        PrinterGraphviz.build(src, project, func)
    }

    companion object {
        @JvmStatic
        fun build(src: String?, project: Project, func: Consumer<String>) {
            object : Task.Backgroundable(project, "draw PlantUML") {
                override fun run(indicator: ProgressIndicator) {
                    if (StringUtils.isBlank(src ?: return)) {
                        return
                    }
                    val paths = SysPath.lib() ?: return
                    for (path in paths) {
                        if (!File(path).exists()) {
                            continue
                        }
                        try {
                            val plantumlPath = "${path}draw-graph/plantuml.puml"
                            val svgOut = FileOutputStream("${path}draw-graph/plantuml.svg")
                            val pngOut = FileOutputStream("${path}draw-graph/plantuml.png")
                            File(plantumlPath).parentFile.mkdirs()
                            Files.write(Path.of(plantumlPath), src.toByteArray(StandardCharsets.UTF_8))
                            val reader = SourceStringReader(src)
                            val svgDesc = reader.outputImage(svgOut, FileFormatOption(FileFormat.SVG))
                            val pngDesc = reader.outputImage(pngOut, FileFormatOption(FileFormat.PNG))
                            func.accept(
                                """
<embed src="file:///${path}draw-graph/plantuml.svg" type="image/svg+xml" />
<br>
$svgDesc
<br>
$pngDesc
<br>
${DrawGraphBundle.message("graphviz.msg")}
<br>
"""
                            )
                            return
                        } catch (e: Exception) {
                            func.accept(
                                """
<embed src="file:///${path}draw-graph/plantuml.svg" type="image/svg+xml" />
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