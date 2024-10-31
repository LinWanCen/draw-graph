package com.github.linwancen.plugin.graph.parser.scala

import com.github.linwancen.plugin.common.text.DocText
import com.github.linwancen.plugin.graph.parser.java.JavaComment
import com.intellij.psi.PsiElement
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.elementType
import org.jetbrains.plugins.scala.lang.scaladoc.lexer.ScalaDocTokenType
import org.jetbrains.plugins.scala.lang.scaladoc.psi.api.ScDocInlinedTag

class ScalaComment : JavaComment() {
    companion object {
        private val INSTANCE = ScalaComment()

        fun addDocParam(docComment: PsiDocComment?, map: MutableMap<String, String>) {
            INSTANCE.addDocParam(docComment, map)
        }
    }

    override fun addDocParam(docComment: PsiDocComment?, map: MutableMap<String, String>) {
        if (docComment == null) {
            return
        }
        addDescription(docComment.children.asSequence(), map)
        addTag(docComment, map)
    }

    /**
     * @return is a new line
     */
    override fun appendElementText(element: PsiElement, all: StringBuilder, currLine: StringBuilder): Boolean {
        if (element.text.contains("\n") && currLine.isNotEmpty()) {
            return true
        }
        if (element is ScDocInlinedTag) {
            val children = element.children
            if (children.size >= 2) {
                DocText.addHtmlText(children[children.size - 1].text, all, currLine)
            }
        } else if (ScalaDocTokenType.DOC_COMMENT_DATA == element.elementType) {
            DocText.addHtmlText(element.text, all, currLine)
        }
        return false
    }
}