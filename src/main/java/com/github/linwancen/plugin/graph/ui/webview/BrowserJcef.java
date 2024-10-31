package com.github.linwancen.plugin.graph.ui.webview;

import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefBrowser;
import org.cef.browser.CefMessageRouter;
import org.cef.browser.CefMessageRouter.CefMessageRouterConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class BrowserJcef extends Browser {

    private JBCefBrowser jbCefBrowser;

    @Override
    public void addImpl(@NotNull JPanel out, Project project) {
        jbCefBrowser = new JBCefBrowser();
        @NotNull JComponent browserComponent = jbCefBrowser.getComponent();

        @NotNull CefMessageRouterConfig config = new CefMessageRouterConfig("java", "javaCancel");
        CefMessageRouter router = CefMessageRouter.create(config);
        router.addHandler(new JcefNavigateHandler(project, jbCefBrowser), true);
        jbCefBrowser.getJBCefClient().getCefClient().addMessageRouter(router);

        out.add(browserComponent, BorderLayout.CENTER);
    }

    @Override
    public void loadImpl(@Nullable String html) {
        if (jbCefBrowser != null && html != null) {
            jbCefBrowser.loadHTML(html);
        }
    }
}
