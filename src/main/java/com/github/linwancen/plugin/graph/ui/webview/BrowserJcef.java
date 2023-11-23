package com.github.linwancen.plugin.graph.ui.webview;

import com.intellij.ui.jcef.JBCefBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class BrowserJcef extends Browser {
    private static final Logger LOG = LoggerFactory.getLogger(BrowserJcef.class);

    static {
        // load move to Browser
    }

    private JBCefBrowser jbCefBrowser;

    @Override
    String add(JPanel out) {
        try {
            jbCefBrowser = new JBCefBrowser();
            JComponent browserComponent = jbCefBrowser.getComponent();
            out.add(browserComponent, BorderLayout.CENTER);
            return null;
        } catch (Throwable e) {
            return e.toString();
        }
    }

    @Override
    public String load(String html) {
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
