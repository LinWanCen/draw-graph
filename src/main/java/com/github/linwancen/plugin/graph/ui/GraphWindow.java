package com.github.linwancen.plugin.graph.ui;

import com.github.linwancen.plugin.common.ui.UiUtils;
import com.github.linwancen.plugin.graph.printer.InstallMermaid;
import com.github.linwancen.plugin.graph.printer.PrinterData;
import com.github.linwancen.plugin.graph.printer.PrinterGraphviz;
import com.github.linwancen.plugin.graph.printer.PrinterMermaid;
import com.github.linwancen.plugin.graph.printer.PrinterPlantuml;
import com.github.linwancen.plugin.graph.settings.DrawGraphAppState;
import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState;
import com.github.linwancen.plugin.graph.ui.webview.Browser;
import com.github.linwancen.plugin.graph.ui.webview.BrowserFactory;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GraphWindow {
    Project project;
    ToolWindow toolWindow;
    @NotNull
    DrawGraphAppState appState;
    @NotNull
    DrawGraphProjectState projectState;

    public GraphWindow(@NotNull Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        appState = DrawGraphAppState.of(null);
        projectState = DrawGraphProjectState.of(project);

        openDir.addActionListener(e -> InstallMermaid.openDir(project));
        reload.addActionListener(e -> RelController.reload(project));
        reset.addActionListener(e -> initOut(project));

        autoLoad.setSelected(true);
        projectState.setAutoLoad(true);
        autoLoad.addActionListener(e -> {
            projectState.setAutoLoad(autoLoad.isSelected());
            if (autoLoad.isSelected()) {
                RelController.forFile(project, FileEditorManager.getInstance(project).getSelectedFiles(), false);
            }
        });

        skipLib.setSelected(projectState.getSkipLib());
        skipLib.addActionListener(e -> {
            projectState.setSkipLib(skipLib.isSelected());
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

        impl.setSelected(appState.getImpl());
        impl.addActionListener(e -> {
            appState.setImpl(impl.isSelected());
            RelController.reload(project);
        });

        mvc.setSelected(appState.getMvc());
        mvc.addActionListener(e -> {
            appState.setMvc(mvc.isSelected());
            RelController.reload(project);
        });

        online.setSelected(appState.getOnline());
        online.addActionListener(e -> {
            appState.setOnline(online.isSelected());
            RelController.reload(project);
        });

        UiUtils.onChange(limit, String.valueOf(appState.getLimit()), s -> {
            try {
                int v = Integer.parseInt(s);
                appState.setLimit(v);
            } catch (Exception ignored) {}
        });

        UiUtils.onChange(include, projectState.getInclude(), projectState::setInclude);
        UiUtils.onChange(exclude, projectState.getExclude(), projectState::setExclude);
        UiUtils.onChange(otherInclude, projectState.getOtherInclude(), projectState::setOtherInclude);
        UiUtils.onChange(otherExclude, projectState.getOtherExclude(), projectState::setOtherExclude);
        UiUtils.onChange(effectInclude, projectState.getEffectInclude(), projectState::setEffectInclude);
        UiUtils.onChange(effectExclude, projectState.getEffectExclude(), projectState::setEffectExclude);
        UiUtils.onChange(annoDoc, projectState.getAnnoDoc(), projectState::setAnnoDoc);
        UiUtils.onChange(effectAnno, appState.getEffectAnno(), appState::setEffectAnno);
        UiUtils.onChange(mermaidOnline, appState.getMermaidOnline(), appState::setMermaidOnline);
        UiUtils.onChange(tempPath, appState.getTempPath(), appState::setTempPath);
        resetSetting.addActionListener(e -> {
            include.setText(DrawGraphProjectState.getDefault().getInclude());
            exclude.setText(DrawGraphProjectState.getDefault().getExclude());
            otherInclude.setText(DrawGraphProjectState.getDefault().getOtherInclude());
            otherExclude.setText(DrawGraphProjectState.getDefault().getOtherExclude());
            projectState.reset();
        });

        @Nullable FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        if (selectedEditor != null) {
            @Nullable VirtualFile file = selectedEditor.getFile();
            RelController.forFile(project, new VirtualFile[]{file}, false);
        }
        initOut(project);
    }

    private void initOut(Project project) {
        plantumlBrowser = BrowserFactory.of(plantumlOut, project);
        mermaidBrowser = BrowserFactory.of(mermaidOut, project);
        graphvizBrowser = BrowserFactory.of(graphvizOut, project);
        initSrc();
    }

    private void initSrc() {
        initEvent(plantumlSrc, plantumlHtml, plantumlBrowser, PrinterPlantuml::build);
        initEvent(mermaidSrc, mermaidHtml, mermaidBrowser, PrinterMermaid::build);
        initEvent(graphvizSrc, graphvizHtml, graphvizBrowser, PrinterGraphviz::build);
    }

    private void initEvent(@NotNull JTextArea src, @NotNull JTextArea html, @Nullable Browser browser,
                           @NotNull BiConsumer<PrinterData, Consumer<String>> fun) {
        @NotNull Consumer<FocusEvent> htmlAndOut = e -> fun.accept(new PrinterData(src.getText(), null, project),
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

    public void closeAutoLoad() {
        EdtExecutorService.getInstance().submit(() -> {
            autoLoad.setSelected(false);
            projectState.setAutoLoad(false);
        });
    }

    JPanel mainPanel;

    // region title
    private JButton openDir;
    private JButton reload;
    private JButton reset;
    private JCheckBox autoLoad;
    private JCheckBox skipLib;
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

    JTextArea effect;

    // region setting
    private JButton resetSetting;
    private JCheckBox skipGetSetIs;
    private JCheckBox lr;
    private JCheckBox doc;
    private JTextField limit;
    private JCheckBox impl;
    private JCheckBox mvc;
    private JTextField include;
    private JTextField exclude;
    private JTextField otherInclude;
    private JTextField otherExclude;
    private JTextField effectInclude;
    private JTextField effectExclude;
    private JTextArea annoDoc;
    private JTextArea effectAnno;
    private JCheckBox online;
    private JTextField mermaidOnline;
    private JTextField tempPath;
    // endregion setting
}
