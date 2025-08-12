package com.github.linwancen.plugin.graph.parser.js

import com.github.linwancen.plugin.common.psi.PsiUnSaveUtils
import com.github.linwancen.plugin.graph.parser.CommentUtils
import com.intellij.lang.javascript.documentation.JSDocumentationUtils
import com.intellij.lang.javascript.psi.JSFunction

object JsComment {
    fun addDocParam(docComment: JSFunction?, map: MutableMap<String, String>) {
        if (docComment == null) {
            return
        }
        addDescription(docComment, map)
    }

    private fun addDescription(docComment: JSFunction, map: MutableMap<String, String>) {
        val psiComment = JSDocumentationUtils.findDocCommentWider(docComment) ?: return
        val text = PsiUnSaveUtils.getText(psiComment)
        val doc = CommentUtils.doc(text)
        map["@0"] = doc
        CommentUtils.split(doc, map)
    }
}