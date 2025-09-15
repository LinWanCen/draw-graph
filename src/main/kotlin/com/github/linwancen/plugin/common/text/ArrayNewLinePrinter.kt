package com.github.linwancen.plugin.common.text

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter

/**
 * jackson array to multi line
 */
class ArrayNewLinePrinter : DefaultPrettyPrinter() {
    init {
        _arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
        _objectIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
    }

    override fun createInstance(): DefaultPrettyPrinter {
        return ArrayNewLinePrinter()
    }
}