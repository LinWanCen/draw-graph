package com.github.linwancen.plugin.graph.ui;

import com.github.linwancen.plugin.common.ui.UiUtils;
import com.github.linwancen.plugin.graph.printer.InstallMermaid;
import com.github.linwancen.plugin.graph.printer.PrinterGraphviz;
import com.github.linwancen.plugin.graph.printer.PrinterMermaid;
import com.github.linwancen.plugin.graph.printer.PrinterPlantuml;
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState;
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState;
import com.github.linwancen.plugin.graph.ui.webview.Browser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.concurrency.EdtExecutorService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

public class GraphWindow {
    Project project;
    ToolWindow toolWindow;

    public GraphWindow(@NotNull Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        @NotNull DrawGraphAppState appState = DrawGraphAppState.of(null);
        @NotNull DrawGraphProjectState projectState = DrawGraphProjectState.of(project);

        openDir.addActionListener(e -> InstallMermaid.openDir(project));
        reload.addActionListener(e -> RelController.reload(project));
        reset.addActionListener(e -> initOut(project));

        autoLoad.setSelected(projectState.getAutoLoad());
        autoLoad.addActionListener(e -> {
            projectState.setAutoLoad(autoLoad.isSelected());
            RelController.reload(project);
        });

        skipGetSetIs.setSelected(projectState.getSkipGetSetIs());
        skipGetSetIs.addActionListener(e -> {
            projectState.setSkipGetSetIs(skipGetSetIs.isSelected());
            RelController.reload(project);
        });

        lr.setSelected(appState.getLr());
        lr.addActionListener(e -> {
            appState.setLr(lr.isSelected());
            RelController.reload(project);
        });

        doc.setSelected(appState.getDoc());
        doc.addActionListener(e -> {
            appState.setDoc(doc.isSelected());
            RelController.reload(project);
        });

        UiUtils.onChange(include, projectState.getInclude(), projectState::setInclude);
        UiUtils.onChange(exclude, projectState.getExclude(), projectState::setExclude);
        resetSetting.addActionListener(e -> {
            include.setText(DrawGraphProjectState.getDefault().getInclude());
            exclude.setText(DrawGraphProjectState.getDefault().getExclude());
            projectState.reset();
        });

        @Nullable FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        if (selectedEditor != null) {
            @Nullable VirtualFile file = selectedEditor.getFile();
            RelController.buildSrc(project, this, new VirtualFile[]{file});
        }
        initOut(project);
    }

    private void initOut(Project project) {
        mermaidBrowser = Browser.of(mermaidOut, project);
        graphvizBrowser = Browser.of(graphvizOut, project);
        plantumlBrowser = Browser.of(plantumlOut, project);
        initSrc();
    }

    private void initSrc() {
        initEvent(mermaidSrc, mermaidHtml, mermaidBrowser, PrinterMermaid::build);
        initEvent(graphvizSrc, graphvizHtml, graphvizBrowser, PrinterGraphviz::build);
        initEvent(plantumlSrc, plantumlHtml, plantumlBrowser, PrinterPlantuml::build);
    }

    private void initEvent(@NotNull JTextArea src, @NotNull JTextArea html, @Nullable Browser browser,
                           @NotNull TriConsumer<String, Project, Consumer<String>> tri) {
        @NotNull Consumer<FocusEvent> htmlAndOut = e -> tri.accept(src.getText(), project,
                        s -> EdtExecutorService.getInstance().submit(() -> {
                            html.setText(s);
                            if (browser != null) browser.load(s);
                        }));
        // reset browser load
        ApplicationManager.getApplication().executeOnPooledThread(() -> htmlAndOut.accept(null));

        UiUtils.onFocusLost(src, project, htmlAndOut);
        UiUtils.onFocusLost(html, project, e -> EdtExecutorService.getInstance().submit(() -> {
            if (browser != null) browser.load(html.getText());
        }));
    }

    JPanel mainPanel;

    // region title
    private JButton openDir;
    private JButton reload;
    private JButton reset;
    private JCheckBox autoLoad;
    // region title

    // region html
    @Nullable
    Browser mermaidBrowser;
    JPanel mermaidOut;
    JTextArea mermaidHtml;
    JTextArea mermaidSrc;

    @Nullable
    Browser plantumlBrowser;
    JPanel plantumlOut;
    JTextArea plantumlHtml;
    JTextArea plantumlSrc;

    @Nullable
    Browser graphvizBrowser;
    JPanel graphvizOut;
    JTextArea graphvizHtml;
    JTextArea graphvizSrc;
    // endregion html

    // region setting
    private JButton resetSetting;
    private JTextField include;
    private JTextField exclude;
    private JCheckBox skipGetSetIs;
    private JCheckBox lr;
    private JCheckBox doc;
    // endregion setting
}
