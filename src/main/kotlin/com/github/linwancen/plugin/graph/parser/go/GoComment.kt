package com.github.linwancen.plugin.graph.parser.go

import com.github.linwancen.plugin.graph.parser.CommentUtils
import com.goide.documentation.GoDocumentationProvider
import com.intellij.psi.PsiElement

object GoComment {
    fun addDocParam(docComment: PsiElement?, map: MutableMap<String, String>) {
        if (docComment == null) {
            return
        }
        addDescription(docComment, map)
    }

    private fun addDescription(docComment: PsiElement, map: MutableMap<String, String>) {
        val comments = GoDocumentationProvider.getCommentsForElement(docComment)
        val commentText = GoDocumentationProvider.getCommentText(comments, false)
        val doc = CommentUtils.doc(commentText)
        map["@0"] = doc
        CommentUtils.split(doc, map)
    }
}