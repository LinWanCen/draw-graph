package com.github.linwancen.plugin.common.ui

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.util.function.BiConsumer
import java.util.function.Consumer
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent

object UiUtils {
    @JvmStatic
    fun lineCount(jTextComponent: JTextComponent): Int {
        val str: String = jTextComponent.text.trim()
        return str.length - str.replace("\n", "").length
    }

    @JvmStatic
    fun onChange(jTextComponent: JTextComponent, initValue: String, func: Consumer<String>) {
        onChangeEvent(jTextComponent, initValue) { _, s -> func.accept(s) }
    }

    @JvmStatic
    fun onChangeEvent(jTextComponent: JTextComponent, initValue: String, onChange: BiConsumer<DocumentEvent, String>) {
        jTextComponent.removeAll()
        jTextComponent.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                onChange.accept(e, jTextComponent.text)
            }

            override fun removeUpdate(e: DocumentEvent) {
                onChange.accept(e, jTextComponent.text)
            }

            override fun changedUpdate(e: DocumentEvent) {
                onChange.accept(e, jTextComponent.text)
            }
        })
        jTextComponent.text = initValue
    }

    @JvmStatic
    fun onFocusLost(jTextComponent: JTextComponent, project: Project, onFocusLost: Consumer<FocusEvent>) {
        jTextComponent.removeAll()
        jTextComponent.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent) {
                object : Task.Backgroundable(project, "draw onFocusLost") {
                    override fun run(indicator: ProgressIndicator) {
                        onFocusLost.accept(e)
                    }
                }.queue()
            }
        })
    }
}