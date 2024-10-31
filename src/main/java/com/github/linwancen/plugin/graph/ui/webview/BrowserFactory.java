package com.github.linwancen.plugin.graph.ui.webview;

import com.github.linwancen.plugin.graph.ui.DrawGraphBundle;
import com.github.linwancen.plugin.graph.ui.webview.extension.BrowserExtensionPoint;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextArea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BrowserFactory {
    @Nullable
    public static Browser of(@NotNull JPanel out, Project project) {
        List<BrowserExtensionPoint> browsers = BrowserExtensionPoint.BROWSER_EPN.getExtensionList();
        out.removeAll();
        out.setLayout(new BorderLayout());
        @NotNull StringBuilder errMsg = new StringBuilder();
        for (BrowserExtensionPoint browser : browsers) {
            try {
                return browser.add(out, project);
            } catch (Throwable e) {
                errMsg.append(e).append("\n");
            }
        }
        errMsg.insert(0, DrawGraphBundle.message("web.load.err.msg"));
        @NotNull JBTextArea errTip = new JBTextArea();
        errTip.setText(errMsg.toString());
        out.add(errTip);
        return null;
    }
}
