package com.github.linwancen.plugin.common.vfile

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor

object ChildFileUtils {

    @JvmStatic
    fun recurExtChildFile(files: Array<VirtualFile>): MutableMap<String, MutableList<VirtualFile>> {
        val map = mutableMapOf<String, MutableList<VirtualFile>>()
        for (file in files) {
            VfsUtil.visitChildrenRecursively(file, object : VirtualFileVisitor<Void?>() {
                override fun visitFile(file: VirtualFile): Boolean {
                    if (file.isDirectory) {
                        return true
                    }
                    map.computeIfAbsent(file.extension ?: return true) { mutableListOf() }.add(file)
                    return true
                }
            })
        }
        return map
    }

    @JvmStatic
    fun mostExt(map: MutableMap<String, MutableList<VirtualFile>>): MutableMap.MutableEntry<String, MutableList<VirtualFile>>? {
        var e: MutableMap.MutableEntry<String, MutableList<VirtualFile>>? = null
        for (entry in map) {
            if (e == null || entry.value.size > e.value.size) {
                e = entry
            }
        }
        return e
    }
}