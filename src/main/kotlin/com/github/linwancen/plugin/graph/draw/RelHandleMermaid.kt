package com.github.linwancen.plugin.graph.draw

class RelHandleMermaid : RelHandle() {
    init {
        handlers["mermaid"] = this
    }

    val sb = StringBuilder()

    override fun beforeGroup(groupMap: MutableMap<String, String>) {
        sb.append("subgraph ")
        item(groupMap)
        sb.append("direction LR\n")
    }

    override fun afterGroup(groupMap: MutableMap<String, String>) {
        sb.append("end\n")
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

    override fun build() : String? {
        val src = sb.toString()
        if (src.isBlank()) {
            return null
        }
        return """
<pre class="mermaid">

graph LR
$src
</pre>
<!-- https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.js -->
<script src="file:///C:/Program Files/mermaid.js"></script>
<script src="file:///D:/Program Files/mermaid.js"></script>
<script src="file:///Applications/mermaid.js"></script>
<script src="file:///var/lib/mermaid.js"></script>
<script src="file:///usr/lib/mermaid.js"></script>
<script type="module">
  mermaid.initialize({ startOnLoad: true });
</script>
"""
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
        private fun deleteSymbol(input: String) = canNotUseSymbol.replace(input, " ")

        @JvmStatic
        private fun addLine(s: String?, sb: StringBuilder) {
            if (s != null) {
                sb.append("${s.replace("\"","")}\\n")
            }
        }
    }
}