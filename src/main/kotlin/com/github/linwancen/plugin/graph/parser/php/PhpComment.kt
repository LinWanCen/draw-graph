package com.github.linwancen.plugin.graph.parser.php

import com.github.linwancen.plugin.common.psi.PsiUnSaveUtils
import com.github.linwancen.plugin.graph.parser.CommentUtils
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment

object PhpComment {
    fun addDocParam(docComment: PhpDocComment?, map: MutableMap<String, String>) {
        if (docComment == null) {
            return
        }
        addDescription(docComment, map)
    }

    private fun addDescription(docComment: PhpDocComment, map: MutableMap<String, String>) {
        val doc = CommentUtils.doc(PsiUnSaveUtils.getText(docComment))
        map["@0"] = doc
        CommentUtils.split(doc, map)
    }
}