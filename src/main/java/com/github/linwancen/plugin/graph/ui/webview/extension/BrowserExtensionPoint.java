package com.github.linwancen.plugin.graph.ui.webview.extension;

import com.github.linwancen.plugin.graph.ui.webview.Browser;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.lang.reflect.Method;

public abstract class BrowserExtensionPoint {
    private static final Logger LOG = LoggerFactory.getLogger(BrowserExtensionPoint.class);

    public static final ExtensionPointName<BrowserExtensionPoint> BROWSER_EPN =
            ExtensionPointName.create("com.github.linwancen.drawgraph.browser");

    @NotNull
    protected abstract String className();

    @Nullable
    public Browser add(@NotNull JPanel out, Project project) throws Exception {
        Class<?> clazz = Class.forName(className());
        Object instance = clazz.getConstructor().newInstance();
        if (instance instanceof Browser) {
            ((Browser) instance).clazz = clazz;
            Method method = clazz.getDeclaredMethod("addImpl", JPanel.class, Project.class);
            method.invoke(instance, out, project);
            LOG.info("Browser add: {}", instance);
            return (Browser) instance;
        }
        return null;
    }
}
