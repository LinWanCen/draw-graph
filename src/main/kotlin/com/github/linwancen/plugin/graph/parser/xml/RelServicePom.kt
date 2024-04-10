package com.github.linwancen.plugin.graph.parser.xml

import com.github.linwancen.plugin.graph.parser.RelData
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag

/**
 * for pom.xml
 */
object RelServicePom {

    fun parsePom(
        psiFile: XmlFile,
        callSetMap: MutableMap<String, MutableSet<String>>,
        relData: RelData,
    ) {
        for (it in psiFile.children) {
            if (it !is XmlDocument) {
                continue
            }
            val root = it.rootTag ?: continue
            val rootInfo = info(root) ?: continue
            val sign = rootInfo["sign"] ?: continue
            val callSet = mutableSetOf<String>()
            callSetMap[sign] = callSet

            module(root, relData, rootInfo)

            dependencies(root, callSet)
        }
    }

    private fun module(
        root: XmlTag,
        relData: RelData,
        rootInfo: MutableMap<String, String>,
    ) {
        val modules = root.findFirstSubTag("modules")
        if (modules == null) {
            relData.regParentChild(rootInfo)
        } else {
            val items = modules.findSubTags("module")
            if (items.isEmpty()) {
                relData.regParentChild(rootInfo)
            } else {
                for (item in items) {
                    val info = mutableMapOf<String, String>()
                    info["sign"] = item.value.text
                    relData.regParentChild(rootInfo, info)
                }
            }
        }
    }

    private fun dependencies(root: XmlTag, callList: MutableSet<String>) {
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
        info["link"] = xmlTag.containingFile.virtualFile.path

        val description = xmlTag.findFirstSubTag("description")?.value?.text?.trim() ?: ""
        if (description.isNotEmpty()) {
            info["@0"] = description
            info["@1"] = description
            return info
        }
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