package com.github.linwancen.plugin.graph.parser.relfile

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class RelFile {
    companion object{
        @JvmStatic
        fun relFileOf(project: Project, files: List<VirtualFile>): List<VirtualFile> {
            return RelFileMvc.relFileOf(project, files)
        }
    }
}