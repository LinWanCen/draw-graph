package com.github.linwancen.plugin.graph.parser

import com.github.linwancen.plugin.common.psi.PsiUnSaveUtils
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import java.util.regex.Pattern

object CommentUtils {

    @JvmStatic
    fun <F : PsiElement> byteToSrc(func: F): F {
        val navElement = func.navigationElement ?: return func
        try {
            @Suppress("UNCHECKED_CAST")
            return navElement as F
        } catch (e: Throwable) {
            // ignore e
            return func
        }
    }

    /**
     * use in C/C++/OC
     */
    @Suppress("unused")
    fun childComment(element: PsiElement, map: MutableMap<String, String>) {
        val psiComment = PsiTreeUtil.getChildOfType(element, PsiComment::class.java) ?: return
        val doc = doc(PsiUnSaveUtils.getText(psiComment))
        map["@0"] = doc
        map["@1"] = doc
    }

    @JvmStatic
    private val DOC_PATTERN: Pattern = Pattern.compile(
        "(?m" +
                // ///// xx line start
                ")^ *//++ *+" +
                // /**** xx block start
                "|^ */\\*++ *+" +
                // ****/ xx block end, not only line start and must before ****
                "| *\\*++/.*" +
                // **** xx  block body
                "|^ *\\*++ *+" +
                // {@link A}
                "|\\{@\\w++|}" +
                // #### xx  python and shell start
                "|^ *#++ *+" +
                // -- xx SQL
                "|^ *--++ *+"
    )

    @JvmStatic
    private val HTML_PATTERN: Pattern = Pattern.compile("<[^>]++>")

    @JvmStatic
    fun doc(s: String): String {
        return DOC_PATTERN.matcher(s).replaceAll(" ").trim { it <= ' ' }
    }

    @JvmStatic
    fun split(s: String, map: MutableMap<String, String>) {
        val split = PsiUnSaveUtils.LINE_END_PATTERN.split(s)
        for ((index, line) in split.withIndex()) {
            map["@${index + 1}"] = line
        }
    }

    @JvmStatic
    fun html2Text(s: String): String {
        return HTML_PATTERN.matcher(s).replaceAll(" ").trim { it <= ' ' }
    }
}
