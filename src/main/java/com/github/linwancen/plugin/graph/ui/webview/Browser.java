package com.github.linwancen.plugin.graph.ui.webview;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.lang.reflect.Method;

public abstract class Browser {

    public Class<?> clazz;

    public abstract void addImpl(@NotNull JPanel out, Project project);

    public abstract void loadImpl(String html);

    public String load(String html) {
        try {
            if (html != null) {
                Method method = clazz.getDeclaredMethod("loadImpl", String.class);
                method.invoke(this, html);
            }
            return null;
        } catch (Throwable e) {
            return e.toString();
        }
    }
}
