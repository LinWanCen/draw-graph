package com.github.linwancen.plugin.graph.printer

import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState

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
        return canNotUseSymbol.replace(input, "_")
    }

    protected open fun addLine(s: String?, sb: StringBuilder, newLineEscape: Boolean = false) {
        if (!DrawGraphAppState.of().doc) {
            return
        }
        var docLine = s?.replace("\"", "") ?: return
        if (docLine.length > 20) {
            docLine = docLine.substring(0, 20)
        }
        docLine = "${docLine.trim()}\n "
        if (newLineEscape) {
            docLine = docLine.replace("\n", "\\n")
        }
        sb.append(docLine)
    }

    abstract fun toSrc(relData: RelData): Pair<String, String>

    protected fun printerData(relData: RelData) {
        // TODO 统一 filter
        relData.parentChildMap
            .filter { !relData.childSet.contains(it.key) }
            .forEach { printerChildren(relData, it.key, it.value) }
        // PlantUML cannot re def
        relData.itemMap
            .filter { !relData.parentChildMap.containsKey(it.key) }
            .filter { !relData.childSet.contains(it.key) }
            .forEach { item(it.value) }
        // PlantUML must def before
        relData.callSet.forEach { call(it.first, it.second) }
    }

    private fun printerChildren(relData: RelData, parent: String, children: Set<String>) {
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
         * [not support english symbol #4138](https://github.com/mermaid-js/mermaid/issues/4138)
         * PlantUML cannot use "-#$,"`
         * "-" should in first
         */
        @JvmStatic
        val canNotUseSymbol = Regex("[-#$,。？！，、；：“”‘’`（）《》【】~@()|'\"<>{}\\[\\]\\\\/ ]")
    }
}