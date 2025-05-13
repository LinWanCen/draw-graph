package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.common.text.Skip
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.BiConsumer

class RelData2Effect {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun save(project: Project, relData: RelData, func: BiConsumer<Set<String>, String>) {
        object : Task.Backgroundable(project, "draw effect") {
            override fun run(indicator: ProgressIndicator) {
                val path = DrawGraphAppState.of().tempPath
                val appState = DrawGraphAppState.of()
                val projectState = DrawGraphProjectState.of(project)
                try {
                    File(path).mkdirs()
                    val haveCall = relData.callSet.map { it.second }.toSet()
                    val noCall = relData.callSet.map { it.first }.filter { it !in haveCall }.toSet()
                    val sb = StringBuilder()
                    noCall.forEach {
                        if (Skip.skip(it, projectState.effectIncludePattern, projectState.effectExcludePattern)) {
                            return@forEach
                        }
                        val map = relData.itemMap[it]
                        sb.append(it).append('\t').append(map?.get("@1") ?: "")
                        // effect anno value
                        if (map != null) {
                            for (anno in appState.effectAnnoArr) {
                                if (anno.isBlank()) {
                                    continue
                                }
                                val effectAnno = map[anno]
                                if (effectAnno != null) {
                                    sb.append('\t').append(effectAnno)
                                    break
                                }
                            }
                        }
                        sb.append('\n')
                    }
                    val s = sb.toString()
                    val dotPath = "$path/effect.txt"
                    Files.write(Path.of(dotPath), s.toByteArray(StandardCharsets.UTF_8))
                    func.accept(noCall, s)
                } catch (e: Throwable) {
                    log.info("RelData2Effect fail", e)
                }
            }
        }.queue()
    }
}