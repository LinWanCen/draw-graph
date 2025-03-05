package com.github.linwancen.plugin.graph.parser.js

import com.intellij.lang.html.HTMLLanguage

class HtmlParser : JsParser() {
    override fun id(): String {
        return HTMLLanguage.INSTANCE.id
    }
}