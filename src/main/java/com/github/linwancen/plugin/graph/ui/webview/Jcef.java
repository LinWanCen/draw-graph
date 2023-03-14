package com.github.linwancen.plugin.graph.ui.webview;

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class Jcef extends WebUtils {

    static {
        WebUtils.map.put(Jcef.class.getName(), new Jcef());
    }

    @Override
    String addImpl(JPanel htmlPanel, JTextArea textArea) {
        try {
            JBCefBrowser jbCefBrowser = new JBCefBrowser();
            JComponent browserComponent = jbCefBrowser.getComponent();
            htmlPanel.add(browserComponent, BorderLayout.CENTER);

            jbCefBrowser.loadHTML(textArea.getText());

            textArea.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(@NotNull DocumentEvent e) {
                    jbCefBrowser.loadHTML(textArea.getText());
                }
            });
            return null;
        } catch (Throwable e) {
            return e.toString();
        }
    }
}
