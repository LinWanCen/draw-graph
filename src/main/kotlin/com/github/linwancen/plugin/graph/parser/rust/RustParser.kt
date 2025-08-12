package com.github.linwancen.plugin.graph.parser.rust

import com.github.linwancen.plugin.common.psi.PsiUnSaveUtils
import com.github.linwancen.plugin.graph.parser.Call
import com.github.linwancen.plugin.graph.parser.ParserLang
import com.github.linwancen.plugin.graph.parser.RelData
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState
import com.intellij.psi.PsiComment
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.RsLanguage
import org.rust.lang.core.psi.*

class RustParser : ParserLang<RsFunction>() {

    override fun id(): String {
        return RsLanguage.id
    }

    override fun funClass(): Class<RsFunction> {
        return RsFunction::class.java
    }

    override fun skipFun(state: DrawGraphProjectState, func: RsFunction): Boolean {
        return false
    }

    override fun toSign(func: RsFunction): String {
        val mod = PsiTreeUtil.getParentOfType(func, RsModItem::class.java)?.name
        val implItem = PsiTreeUtil.getParentOfType(func, RsImplItem::class.java)
        val struct = implItem?.firstChild?.let { PsiUnSaveUtils.getText(it) }
        var sign = "${func.name}"
        if (struct != null) {
            sign = "$struct.$sign"
        }
        if (mod != null) {
            sign = "$mod.$sign"
        }
        return sign
    }

    override fun funMap(funMap: MutableMap<String, String>, func: RsFunction) {
        val v = RustModifier.symbol(func)
        funMap["name"] = "$v ${func.name}"
    }

    override fun classMap(func: RsFunction, relData: RelData): MutableMap<String, String>? {
        val mod = PsiTreeUtil.getParentOfType(func, RsModItem::class.java)?.name
        val implItem = PsiTreeUtil.getParentOfType(func, RsImplItem::class.java)
        val struct = implItem?.firstChild?.let { PsiUnSaveUtils.getText(it) }
        val name = struct ?: mod ?: return null
        val classMap = mutableMapOf<String, String>()
        classMap["name"] = name
        classMap["sign"] = when {
            struct != null && mod != null -> "$mod.$struct"
            else -> name
        }
        return classMap
    }

    override fun callList(func: RsFunction, call: Boolean): List<RsFunction> {
        if (!call) {
            return Call.find(func, false, funClass())
        }
        val refs = PsiTreeUtil
            .findChildrenOfAnyType(func, RsPath::class.java, RsMethodCall::class.java)
            .filter { PsiTreeUtil.getNonStrictParentOfType(it, PsiComment::class.java) == null }
        return Call.findRefs(refs).filterIsInstance<RsFunction>()
    }
}