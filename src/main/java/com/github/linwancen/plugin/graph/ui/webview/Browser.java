package com.github.linwancen.plugin.graph.ui.webview;

import com.github.linwancen.plugin.graph.ui.DrawGraphBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.ui.components.JBTextArea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Browser implements StartupActivity.RequiredForSmartMode {
    @Override
    public void runActivity(@NotNull Project project) {}

    static Map<String, Class<? extends Browser>> map = new HashMap<>();

    static {
        // ensure load
        map.put(BrowserJcef.class.getName(), BrowserJcef.class);
    }

    @Nullable
    public static Browser of(@NotNull JPanel out, Project project) {
        out.removeAll();
        out.setLayout(new BorderLayout());
        StringBuilder errMsg = new StringBuilder();
        for (Class<? extends Browser> c : map.values()) {
            try {
                @NotNull Browser browser = c.getConstructor().newInstance();
                @Nullable String s = browser.add(out, project);
                if (s == null) {
                    return browser;
                }
                errMsg.append(s).append("\n");
            } catch (Exception e) {
                errMsg.append(e).append("\n");
            }
        }
        errMsg.insert(0, DrawGraphBundle.message("web.load.err.msg"));
        JBTextArea errTip = new JBTextArea();
        errTip.setText(errMsg.toString());
        out.add(errTip);
        return null;
    }

    @Nullable
    abstract String add(JPanel out, Project project);

    public abstract String load(String html);
}
