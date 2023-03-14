package com.github.linwancen.plugin.graph.ui;

import com.github.linwancen.plugin.graph.settings.DrawGraphProjectState;
import com.github.linwancen.plugin.graph.ui.webview.WebUtils;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GraphWindow {
    Project project;
    ToolWindow toolWindow;

    public GraphWindow(@NotNull Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        DrawGraphProjectState projectState = DrawGraphProjectState.of(project);

        reload.addActionListener(e -> RelController.reload(project));
        autoLoad.setSelected(projectState.getAutoLoad());
        autoLoad.addActionListener(e -> projectState.setAutoLoad(autoLoad.isSelected()));

        FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        if (selectedEditor != null) {
            VirtualFile file = selectedEditor.getFile();
            String src = RelController.src(project, new VirtualFile[]{file});
            mermaidSrc.setText(src);
        }
        WebUtils.add(mermaidHtml, mermaidSrc);
    }

    JPanel mainPanel;
    private JTabbedPane graphTab;
    private JTabbedPane mermaidTab;
    private JButton reload;
    public JTextArea mermaidSrc;
    private JButton mermaidTempReset;
    private JTextArea textArea1;
    private JTextArea labelTextArea1;
    private JTextArea labelTextArea;
    private JButton resetButton;
    private JButton resetButton1;
    private JPanel mermaidHtml;
    private JCheckBox autoLoad;
    private JLabel mermaidSrcLabel;
}
