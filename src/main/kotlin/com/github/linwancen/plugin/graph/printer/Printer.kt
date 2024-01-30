package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.graph.parser.RelData
import com.intellij.openapi.project.Project
import java.util.function.Consumer

abstract class Printer {
    protected open fun beforeGroup(groupMap: MutableMap<String, String>) {
        // impl it
    }

    protected open fun afterGroup(groupMap: MutableMap<String, String>) {
        // impl it
    }

    protected open fun item(itemMap: MutableMap<String, String>) {
        // impl it
    }

    protected open fun call(usageSign: String, callSign: String) {
        // impl it
    }

    protected open fun sign(input: String): String {
        val deleteKeyword = keyword.replace(input, "$1_")
        return canNotUseSymbol.replace(deleteKeyword, "_")
    }

    protected open fun addLine(s: String?, sb: StringBuilder) {
        if (s != null) {
            var docLine = s.replace("\"", "")
            if (docLine.length > 20) {
                docLine = docLine.substring(0, 20)
            }
            sb.append("${docLine}\\n")
        }
    }

    abstract fun toSrc(relData: RelData): String

    abstract fun toHtml(src: String, project: Project, func: Consumer<String>)

    protected fun printerData(relData: RelData) {
        relData.parentChildMap.filter { !relData.childSet.contains(it.key) }
            .forEach { printerChildren(relData, it.key, it.value) }
        // PlantUML can not re def
        relData.itemMap.filter { !relData.parentChildMap.containsKey(it.key) }
            .filter { !relData.childSet.contains(it.key) }
            .forEach { item(it.value) }
        // PlantUML must def before
        relData.callList.forEach { call(it.first, it.second) }
    }

    private fun printerChildren(relData: RelData, parent: String, children: List<String>) {
        val parentMap = relData.itemMap[parent] ?: return
        beforeGroup(parentMap)
        for (child in children) {
            val strings = relData.parentChildMap[child]
            if (strings == null) {
                item(relData.itemMap[child] ?: continue)
            } else {
                printerChildren(relData, child, strings)
            }
        }
        afterGroup(parentMap)
    }

    companion object {
        /**
         * [parse error with word graph #4079](https://github.com/mermaid-js/mermaid/issues/4079)
         */
        @JvmStatic
        private val keyword = Regex("\\b(graph|end|parent)\\b")

        /**
         * [support not english symbol #4138](https://github.com/mermaid-js/mermaid/issues/4138)
         * PlantUML can not use -#
         */
        @JvmStatic
        private val canNotUseSymbol = Regex("[-#。？！，、；：“”‘’（）《》【】~@()|'\"<{}\\[\\]]")
    }
}