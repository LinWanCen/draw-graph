package com.github.linwancen.plugin.graph.parser.python

import com.jetbrains.python.psi.StructuredDocString

object PythonComment {
    fun addDocParam(docComment: StructuredDocString?, map: MutableMap<String, String>) {
        if (docComment == null) {
            return
        }
        addDescription(docComment, map)
    }

    private fun addDescription(docComment: StructuredDocString, map: MutableMap<String, String>) {
        map["@0"] = docComment.summary
        map["@1"] = docComment.summary
    }
}