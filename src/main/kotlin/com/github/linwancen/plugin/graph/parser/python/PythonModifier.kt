package com.github.linwancen.plugin.graph.parser.python

import com.jetbrains.python.psi.PyFunction

object PythonModifier {
    fun symbol(func: PyFunction): String {
        val sb = StringBuilder()
        if (PyFunction.Modifier.STATICMETHOD ==  func.modifier) {
            sb.append("S")
        }
        return sb.toString()
    }
}
