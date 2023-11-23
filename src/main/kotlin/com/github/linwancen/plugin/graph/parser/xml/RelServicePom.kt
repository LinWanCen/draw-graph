package com.github.linwancen.plugin.graph.parser.xml

import com.github.linwancen.plugin.graph.printer.Printer
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag

/**
 * for pom.xml
 */
object RelServicePom {

    fun parsePom(
        psiFile: XmlFile,
        callListMap: MutableMap<String, List<String>>,
        printer: Array<out Printer>,
    ) {
        for (it in psiFile.children) {
            if (it !is XmlDocument) {
                continue
            }
            val root = it.rootTag ?: continue
            val rootInfo = info(root) ?: continue
            val sign = rootInfo["sign"] ?: continue
            val callList = mutableListOf<String>()
            callListMap[sign] = callList

            module(root, printer, rootInfo)

            dependencies(root, callList)
        }
    }

    private fun module(
        root: XmlTag,
        printer: Array<out Printer>,
        rootInfo: MutableMap<String, String>,
    ) {
        val modules = root.findFirstSubTag("modules")
        if (modules == null) {
            printer.forEach { it.item(rootInfo) }
        } else {
            val items = modules.findSubTags("module")
            if (items.isEmpty()) {
                printer.forEach { it.item(rootInfo) }
            } else {
                printer.forEach { it.beforeGroup(rootInfo) }
                for (item in items) {
                    val info = mutableMapOf<String, String>()
                    info["sign"] = item.value.text
                    printer.forEach { it.item(info) }
                }
                printer.forEach { it.afterGroup(rootInfo) }
            }
        }
    }

    private fun dependencies(root: XmlTag, callList: MutableList<String>) {
        root.findFirstSubTag("dependencies")?.let { tag ->
            val items = tag.findSubTags("dependency")
            if (items.isNotEmpty()) {
                for (item in items) {
                    val itemInfo = info(item) ?: continue
                    callList.add(itemInfo["sign"] ?: continue)
                }
            }
        }
    }

    @JvmStatic
    var regex = Regex("\\$\\{project.artifactId} ? \\|? ?")

    private fun info(xmlTag: XmlTag): MutableMap<String, String>? {
        val info = mutableMapOf<String, String>()
        val artifactId = xmlTag.findFirstSubTag("artifactId")?.value?.text ?: return null
        info["sign"] = artifactId
        info["name"] = artifactId

        var name = xmlTag.findFirstSubTag("name")?.value?.text ?: ""
        if (name.isNotEmpty()) {
            name = regex.replace(name, "").trim()
        }
        if (name.isNotEmpty()) {
            info["@0"] = name
            info["@1"] = name
        }
        return info
    }
}