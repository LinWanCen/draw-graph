package com.github.linwancen.plugin.graph.parser.kotlin

import com.github.linwancen.plugin.graph.parser.CommentUtils
import org.jetbrains.kotlin.kdoc.psi.api.KDoc

object KotlinComment {
    fun addDocParam(docComment: KDoc?, map: MutableMap<String, String>) {
        if (docComment == null) {
            return
        }
        addDescription(docComment, map)
    }

    private fun addDescription(docComment: KDoc, map: MutableMap<String, String>) {
        val content = docComment.getDefaultSection().getContent()
        val doc = CommentUtils.doc(content)
        map["@0"] = doc
        CommentUtils.split(doc, map)
    }
}