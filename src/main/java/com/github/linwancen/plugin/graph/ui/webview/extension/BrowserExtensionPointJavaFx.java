package com.github.linwancen.plugin.graph.ui.webview.extension;

import org.jetbrains.annotations.NotNull;

public class BrowserExtensionPointJavaFx extends BrowserExtensionPoint {

    @NotNull
    protected String className() {
        return "com.github.linwancen.plugin.graph.ui.webview.BrowserJavaFx";
    }
}