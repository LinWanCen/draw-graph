package com.github.linwancen.plugin.graph.ui.webview;

import com.github.linwancen.plugin.graph.ui.DrawGraphBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity.RequiredForSmartMode;
import com.intellij.ui.components.JBTextArea;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public abstract class WebUtils implements RequiredForSmartMode {
    @Override
    public void runActivity(@NotNull Project project) {}

    static Map<String, WebUtils> map = new HashMap<>();

    public static void add(JPanel htmlPanel, JTextArea textArea) {
        htmlPanel.setLayout(new BorderLayout());
        StringBuilder errMsg = new StringBuilder();
        for (WebUtils webUtils : map.values()) {
            String s = webUtils.addImpl(htmlPanel, textArea);
            if (s == null) {
                return;
            }
            errMsg.append(s).append("\n");
        }
        if (errMsg.length() == 0) {
            errMsg.append(DrawGraphBundle.message("web.load.err.msg"));
        }
        JBTextArea errTip = new JBTextArea();
        errTip.setText(errMsg.toString());
        htmlPanel.add(errTip);
    }

    abstract String addImpl(JPanel htmlPanel, JTextArea textArea);
}
