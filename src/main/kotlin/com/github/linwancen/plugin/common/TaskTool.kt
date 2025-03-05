package com.github.linwancen.plugin.common

import com.intellij.openapi.progress.ProgressIndicator
import java.time.Duration

/**
 * Calculate progress, time remaining, return null when canceled
 */
class TaskTool(var indicator: ProgressIndicator, var length: Int) {
    private var refTime: Long = System.currentTimeMillis()
    private var refUnitTime: Long = 0
    private var refIndex: Int = 0

    init {
        indicator.isIndeterminate = false
    }

    /**
     * taskTool.beforeNext(index, text2) ?: break
     */
    fun beforeNext(index: Int, text2: String): String? {
        if (indicator.isCanceled) {
            return null
        }
        indicator.fraction = 1.0 * index / length
        indicator.text2 = text2
        if (index == 0) {
            return ""
        }
        val remain = length - index
        val currTime = System.currentTimeMillis()
        if (index > refIndex * 2) {
            val refUseTime = currTime - refTime
            refTime = currTime
            refUnitTime = refUseTime / (index - refIndex)
            refIndex = index
        }
        val remainTime = refUnitTime * remain
        val timeStr = timeStr(remainTime)
        indicator.text = "$index / $length need $timeStr"
        return ""
    }

    companion object {
        @JvmStatic
        fun timeStr(millis: Long): String {
            val time = Duration.ofMillis(millis)
            val hour = if (time.toHours() > 0) "${time.toHours()}h " else ""
            val minutes = if (time.toMinutesPart() > 0) "${time.toMinutesPart()}m " else ""
            val seconds = if (time.toSecondsPart() > 0) "${time.toSecondsPart()}s" else ""
            return "$hour$minutes$seconds"
        }
    }
}