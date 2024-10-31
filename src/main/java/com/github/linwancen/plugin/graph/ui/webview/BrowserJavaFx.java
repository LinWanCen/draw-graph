package com.github.linwancen.plugin.graph.ui.webview;

import com.intellij.openapi.project.Project;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class BrowserJavaFx extends Browser {

    private WebEngine engine;

    @Override
    public void addImpl(@NotNull JPanel out, Project project) {
        @NotNull JFXPanel jfxPanel = new JFXPanel();
        Platform.runLater(() -> {
            @NotNull Group group = new Group();
            @NotNull WebView webView = new WebView();
            group.getChildren().add(webView);
            @NotNull Scene scene = new Scene(group);
            jfxPanel.setScene(scene);
            out.add(jfxPanel, BorderLayout.CENTER);

            engine = webView.getEngine();
        });
    }

    @Override
    public void loadImpl(String html) {
        if (engine != null && html != null) {
            Platform.runLater(() -> engine.loadContent(html));
        }
    }
}
