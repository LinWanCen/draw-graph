package com.github.linwancen.plugin.common.text

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.TextNode
import com.github.linwancen.plugin.common.psi.PsiUnSaveUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object JsonValueParser {

    @JvmStatic
    fun convert(mapper: ObjectMapper, root: JsonNode): JsonNode {
        if (root.isObject) {
            val newObject = JsonNodeFactory.instance.objectNode()
            root.fields().forEach { (k, v) -> newObject.set<JsonNode>(k, convert(mapper, v)) }
            return newObject
        }
        if (root.isArray) {
            val newObject = JsonNodeFactory.instance.objectNode()
            root.forEachIndexed { i, v -> newObject.set<JsonNode>(i.toString(), convert(mapper, v)) }
            return newObject
        }
        if (root.isNumber && root.isIntegralNumber) {
            var timestamp = root.asLong()
            val length = timestamp.toString().length
            if (length != 10 && length != 13) {
                return root
            }
            if (length == 10) {
                timestamp *= 1000
            }
            val date = Date(timestamp)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            val dateStr = sdf.format(date)
            return TextNode(dateStr)
        }
        if (root.isTextual) {
            val s = root.asText()
            // json
            if (s.startsWith('{') || s.startsWith("[")) {
                try {
                    val subJson = mapper.readTree(s)
                    val convert = convert(mapper, subJson)
                    return convert
                } catch (ignored: Exception) {
                }
            }
            val s64 = PsiUnSaveUtils.LINE_END_PATTERN.matcher(s).replaceAll("")
            if (isBase64(s64)) {
                try {
                    val decode = Base64.getDecoder().decode(s64)
                    val string = String(decode, Charsets.UTF_8)
                    return multiLineToObject(string)
                } catch (ignored: Exception) {
                }
            }
            if (s64.length != s.length) {
                return multiLineToObject(s)
            }
        }
        return root
    }

    /**
     * 0-F maybe 16 decimal number
     */
    @JvmStatic
    private val A_PATTERN = Pattern.compile("[G-ZG-z]")

    @JvmStatic
    private val N_PATTERN = Pattern.compile("[0-9]")

    @JvmStatic
    private val NOT_BASE64_PATTERN = Pattern.compile("[^A-Za-z0-9+/]")

    @JvmStatic
    private fun isBase64(s: String): Boolean {
        if (s.length < 4) return false
        if (s.length % 4 != 0) return false
        val likeBase64 = s.endsWith('=') || A_PATTERN.matcher(s).find() && N_PATTERN.matcher(s).find()
        if (!likeBase64) return false
        val i = s.indexOf('=')
        val pre = if (i > 0) {
            if (i < s.length - 2) return false
            s.substring(0, i)
        } else {
            s
        }
        return !NOT_BASE64_PATTERN.matcher(pre).find()
    }

    @JvmStatic
    private fun multiLineToObject(s: String): JsonNode {
        val split = PsiUnSaveUtils.LINE_END_PATTERN.split(s)
        if (split.size == 1) {
            return TextNode(s)
        }
        val node = JsonNodeFactory.instance.objectNode()
        for ((index, str) in split.withIndex()) {
            node.put(index.toString(), str)
        }
        return node
    }
}