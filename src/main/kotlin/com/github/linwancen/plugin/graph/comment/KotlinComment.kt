package com.github.linwancen.plugin.graph.comment

import org.jetbrains.kotlin.kdoc.psi.api.KDoc

object KotlinComment {
    fun addDocParam(docComment: KDoc?, map: MutableMap<String, String>) {
        if (docComment == null) {
            return
        }
        addDescription(docComment, map)
    }

    private fun addDescription(docComment: KDoc, map: MutableMap<String, String>) {
        map["@0"] = docComment.getDefaultSection().getContent()
        map["@1"] = docComment.getDefaultSection().getContent()
    }
}