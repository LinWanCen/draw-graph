package com.github.linwancen.plugin.common.text

object Formats {
    @JvmStatic
    val DOLLAR_KEY = Regex("\\$\\{([^}]++)}")

    /**
     * @param default null is keep key
     */
    @JvmStatic
    fun text(format: String, param: Map<String, String>, default: String? = "", regex: Regex = DOLLAR_KEY): String {
        val split = regex.split(format)
        val findAll = regex.findAll(format)
        val result = StringBuilder()
        for ((index, match) in findAll.withIndex()) {
            val key = match.groups[1]?.value
            result.append(split[index])
            result.append(param[key] ?: default ?: match.value)
        }
        result.append(split[split.size - 1])
        return result.toString()
    }
}