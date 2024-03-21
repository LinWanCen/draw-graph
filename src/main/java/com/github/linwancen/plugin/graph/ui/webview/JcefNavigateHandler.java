package com.github.linwancen.plugin.graph.ui.webview;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.util.concurrency.EdtExecutorService;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public class JcefNavigateHandler extends CefMessageRouterHandlerAdapter {
    protected Project project;
    protected JBCefBrowser jbCefBrowser;

    public JcefNavigateHandler(Project project, JBCefBrowser jbCefBrowser) {
        this.project = project;
        this.jbCefBrowser = jbCefBrowser;
    }

    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, @NotNull String request, boolean persistent,
                           @NotNull CefQueryCallback callback) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> open(request, callback));
        return true;
    }

    private void open(@NotNull String request, @NotNull CefQueryCallback callback) {
        if (request.equals("openDevtools")) {
            jbCefBrowser.openDevtools();
            callback.success("openDevtools");
            return;
        }
        if (!request.startsWith("navigate:")) {
            callback.failure(40, "not support: " + request);
            return;
        }
        request = request.substring("navigate:".length());
        @NotNull String filePath = request.substring(0, request.indexOf("#"));
        @NotNull String elementName = request.substring(request.indexOf("#") + 1);
        @Nullable VirtualFile file = VirtualFileManager.getInstance().findFileByNioPath(Path.of(filePath));
        if (file == null) {
            callback.failure(41, "file not found: " + filePath);
            return;
        }
        DumbService.getInstance(project).runReadActionInSmartMode(() -> {
            @Nullable PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile == null) {
                callback.failure(42, "psiFile not found: " + filePath);
                return;
            }
            if (elementName.isBlank()) {
                navigate(callback, psiFile);
            }
            if (find(callback, psiFile, elementName)) {
                return;
            }
            navigate(callback, psiFile);
        });
    }

    private static boolean find(@NotNull CefQueryCallback callback, PsiElement element, @NotNull String elementName) {
        // findChildrenOfType() slow, so getChildrenOfType() Recursive return when found
        @NotNull List<NavigatablePsiElement> children = PsiTreeUtil.getChildrenOfTypeAsList(element, NavigatablePsiElement.class);
        for (@NotNull NavigatablePsiElement child : children) {
            if (elementName.equals(child.getName())) {
                navigate(callback, child);
                return true;
            }
            if (find(callback, child, elementName)) {
                return true;
            }
        }
        return false;
    }

    private static void navigate(@NotNull CefQueryCallback callback, @NotNull NavigatablePsiElement child) {
        EdtExecutorService.getInstance().submit(
                () -> ApplicationManager.getApplication().invokeLater(
                        () -> {
                            child.navigate(true);
                            callback.success("navigate: " + child.getName());
                        }
                )
        );
    }
}
