package com.github.linwancen.plugin.graph.parser.java

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import java.util.regex.Pattern

object GetSetIs {
    private val getSetIsPattern: Pattern = Pattern.compile("^(?:get|set|is)(.++)$")

    @JvmStatic
    fun isGetSetIs(method: PsiMethod, psiClass: PsiClass): Boolean {
        val matcher = getSetIsPattern.matcher(method.name)
        if (!matcher.find()) {
            return false
        }
        val fieldName = matcher.group(1) ?: return false
        return psiClass.findFieldByName(fieldName.decapitalize(), true) != null
    }
}