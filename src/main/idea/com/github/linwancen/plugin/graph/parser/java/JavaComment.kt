package com.github.linwancen.plugin.graph.parser.java

import com.github.linwancen.plugin.common.psi.PsiUnSaveUtils
import com.github.linwancen.plugin.common.text.DocText
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.javadoc.PsiDocComment

open class JavaComment {
    companion object {
        private val INSTANCE = JavaComment()

        fun addDocParam(docComment: PsiDocComment?, map: MutableMap<String, String>) {
            INSTANCE.addDocParam(docComment, map)
        }
    }

    open fun addDocParam(docComment: PsiDocComment?, map: MutableMap<String, String>) {
        if (docComment == null) {
            return
        }
        addDescription(docComment.descriptionElements.asSequence(), map)
        addTag(docComment, map)
    }

    open fun addDescription(elements: Sequence<PsiElement>, map: MutableMap<String, String>) {
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

    open fun appendElementText(element: PsiElement, all: StringBuilder, currLine: StringBuilder): Boolean {
        if (element is PsiWhiteSpace && currLine.isNotEmpty()) {
            return true
        }
        val children = element.children
        if (children.isNotEmpty()) {
            if (children.size >= 3) {
                DocText.addHtmlText(PsiUnSaveUtils.getText(children[children.size - 2]), all, currLine)
            }
            return false
        }
        DocText.addHtmlText(PsiUnSaveUtils.getText(element), all, currLine)
        return false
    }

    open fun addTag(docComment: PsiDocComment, map: MutableMap<String, String>) {
        for (tag in docComment.tags) {
            val name = tag.name
            val valueElement = tag.valueElement ?: tag.dataElements.firstOrNull() ?: continue
            val value = DocText.addHtmlText(PsiUnSaveUtils.getText(valueElement)) ?: continue
            map["@$name"] = value
        }
    }
}