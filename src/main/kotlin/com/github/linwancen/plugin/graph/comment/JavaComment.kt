package com.github.linwancen.plugin.graph.comment

import com.github.linwancen.plugin.common.text.DocText
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.javadoc.PsiDocToken
import com.intellij.psi.javadoc.PsiInlineDocTag

object JavaComment {
    fun addDocParam(docComment: PsiDocComment?, map: MutableMap<String, String>) {
        if (docComment == null) {
            return
        }
        addDescription(docComment, map)
        addTag(docComment, map)
    }

    private fun addDescription(docComment: PsiDocComment, map: MutableMap<String, String>) {
        val elements: Array<PsiElement> = docComment.descriptionElements
        val all = StringBuilder()
        val currLine = StringBuilder()
        var lineCount = 1
        for (element in elements) {
            if (appendElementText(element, all, currLine)) {
                map["@$lineCount"] = currLine.toString()
                lineCount++
                currLine.clear()
            }
        }
        if (currLine.isNotEmpty()) {
            map["@$lineCount"] = currLine.toString()
        }
        if (all.isNotEmpty()) {
            map["@0"] = all.toString()
        }
    }

    private fun appendElementText(element: PsiElement, all: StringBuilder, currLine: StringBuilder): Boolean {
        if (element is PsiDocToken) {
            DocText.addHtmlText(element.text, all, currLine)
        }
        if (element is PsiInlineDocTag) {
            val children = element.children
            if (children.size >= 3) {
                DocText.addHtmlText(children[children.size - 2].text, all, currLine)
            }
        }
        return element is PsiWhiteSpace && currLine.isNotEmpty()
    }

    private fun addTag(docComment: PsiDocComment, map: MutableMap<String, String>) {
        for (tag in docComment.tags) {
            // @see @param should use getDataElements()
            val name = tag.name
            val valueElement = tag.valueElement ?: continue
            val value = DocText.addHtmlText(valueElement.text) ?: continue
            map["@$name"] = value
        }
    }
}