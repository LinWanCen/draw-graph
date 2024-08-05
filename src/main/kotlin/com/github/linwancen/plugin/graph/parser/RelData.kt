package com.github.linwancen.plugin.graph.parser

class RelData {
    val parentChildMap = mutableMapOf<String, MutableSet<String>>()
    /** child print in parentChildMap root, so filter */
    val childSet = mutableSetOf<String>()
    val itemMap = mutableMapOf<String, MutableMap<String, String>>()
    val callSet = mutableSetOf<Pair<String, String>>()

    fun regParentChild(parentMap: MutableMap<String, String>, childMap: MutableMap<String, String>? = null) {
        val parent = parentMap["sign"] ?: return
        itemMap[parent] = parentMap
        val child = childMap?.get("sign") ?: return
        itemMap.putIfAbsent(child, childMap)
        parentChildMap.computeIfAbsent(parent) { mutableSetOf() }.add(child)
        childSet.add(child)
    }

    fun regCall(usageSign: String, callSign: String) {
        callSet.add(Pair(usageSign, callSign))
    }
}