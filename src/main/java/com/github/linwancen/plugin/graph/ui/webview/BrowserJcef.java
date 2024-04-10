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

    static {
        // load move to Browser
    }

    private JBCefBrowser jbCefBrowser;

    @Nullable
    @Override
    String add(@NotNull JPanel out, Project project) {
        try {
            jbCefBrowser = new JBCefBrowser();
            @NotNull JComponent browserComponent = jbCefBrowser.getComponent();

            @NotNull CefMessageRouterConfig config = new CefMessageRouterConfig("java", "javaCancel");
            CefMessageRouter router = CefMessageRouter.create(config);
            router.addHandler(new JcefNavigateHandler(project, jbCefBrowser), true);
            jbCefBrowser.getJBCefClient().getCefClient().addMessageRouter(router);

            out.add(browserComponent, BorderLayout.CENTER);
            return null;
        } catch (Throwable e) {
            return e.toString();
        }
    }

    @Nullable
    @Override
    public String load(@Nullable String html) {
        try {
            if (jbCefBrowser != null && html != null) {
                jbCefBrowser.loadHTML(html);
            }
            return null;
        } catch (Throwable e) {
            return e.toString();
        }
    }
}
