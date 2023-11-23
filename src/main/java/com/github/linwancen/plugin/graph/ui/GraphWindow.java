package com.github.linwancen.plugin.graph.ui;

import com.github.linwancen.plugin.common.ui.UiUtils;
import com.github.linwancen.plugin.graph.printer.InstallMermaid;
import com.github.linwancen.plugin.graph.printer.PrinterGraphviz;
import com.github.linwancen.plugin.graph.printer.PrinterMermaid;
import com.github.linwancen.plugin.graph.printer.PrinterPlantuml;
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState;
import com.github.linwancen.plugin.graph.ui.webview.Browser;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

public class GraphWindow {
    Project project;
    ToolWindow toolWindow;

    public GraphWindow(@NotNull Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        DrawGraphProjectState projectState = DrawGraphProjectState.of(project);

        openDir.addActionListener(e -> InstallMermaid.openDir(project));
        reload.addActionListener(e -> RelController.reload(project));
        reset.addActionListener(e -> initOut());

        autoLoad.setSelected(projectState.getAutoLoad());
        autoLoad.addActionListener(e -> projectState.setAutoLoad(autoLoad.isSelected()));

        UiUtils.onChange(include, projectState.getInclude(), projectState::setInclude);
        UiUtils.onChange(exclude, projectState.getExclude(), projectState::setExclude);
        resetSetting.addActionListener(e -> {
            include.setText(DrawGraphProjectState.getDefault().getInclude());
            exclude.setText(DrawGraphProjectState.getDefault().getExclude());
            projectState.reset();
        });

        FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        if (selectedEditor != null) {
            VirtualFile file = selectedEditor.getFile();
            RelController.buildSrc(project, this, new VirtualFile[]{file});
        }
        initOut();
    }

    private void initOut() {
        mermaidBrowser = Browser.of(mermaidOut);
        graphvizBrowser = Browser.of(graphvizOut);
        plantumlBrowser = Browser.of(plantumlOut);
        initSrc();
    }

    private void initSrc() {
        initEvent(mermaidSrc, mermaidHtml, mermaidBrowser, PrinterMermaid::build);
        initEvent(graphvizSrc, graphvizHtml, graphvizBrowser, PrinterGraphviz::build);
        initEvent(plantumlSrc, plantumlHtml, plantumlBrowser, PrinterPlantuml::build);
    }

    private void initEvent(JTextArea src, JTextArea html, Browser browser,
                           TriConsumer<String, Project, Consumer<String>> tri) {
        Consumer<FocusEvent> htmlAndOut = e -> tri.accept(src.getText(), project,
                s -> {
                    html.setText(s);
                    if (browser != null) browser.load(s);
                });
        htmlAndOut.accept(null);

        UiUtils.onFocusLost(src, project, htmlAndOut);
        UiUtils.onFocusLost(html, project, e -> {if (browser != null) browser.load(html.getText());});
    }

    void load() {
        if (mermaidBrowser != null) mermaidBrowser.load(mermaidHtml.getText());
        if (graphvizBrowser != null) graphvizBrowser.load(graphvizHtml.getText());
        if (plantumlBrowser != null) plantumlBrowser.load(plantumlHtml.getText());
    }

    JPanel mainPanel;

    // region title
    private JButton openDir;
    private JButton reload;
    private JButton reset;
    private JCheckBox autoLoad;
    // region title

    // region html
    private Browser mermaidBrowser;
    JPanel mermaidOut;
    JTextArea mermaidHtml;
    JTextArea mermaidSrc;

    private Browser plantumlBrowser;
    JPanel plantumlOut;
    JTextArea plantumlHtml;
    JTextArea plantumlSrc;

    private Browser graphvizBrowser;
    JPanel graphvizOut;
    JTextArea graphvizHtml;
    JTextArea graphvizSrc;
    // endregion html

    // region setting
    private JButton resetSetting;
    private JTextField include;
    private JTextField exclude;
    // endregion setting
}
