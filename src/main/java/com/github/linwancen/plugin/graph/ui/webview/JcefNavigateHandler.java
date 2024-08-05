package com.github.linwancen.plugin.graph.ui.webview;

import com.github.linwancen.plugin.graph.parser.Parser;
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

import java.util.List;

public class JcefNavigateHandler extends CefMessageRouterHandlerAdapter {
    protected Project project;
    protected JBCefBrowser jbCefBrowser;

    public JcefNavigateHandler(Project project, JBCefBrowser jbCefBrowser) {
        this.project = project;
        this.jbCefBrowser = jbCefBrowser;
    }

    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, @NotNull String request,
                           boolean persistent, @NotNull CefQueryCallback callback) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                open(request, callback);
            } catch (Throwable e) {
                callback.failure(50, e.toString());
            }
        });
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
        DumbService.getInstance(project).runReadActionInSmartMode(() -> {
            String link = request.substring("navigate:".length());
            @NotNull String filePath = link;
            @NotNull String childName = "";
            int i = link.indexOf("#");
            if (i > 0) {
                filePath = link.substring(0, i);
                childName = link.substring(i + 1);
            }
            // className to Element
            if (!filePath.contains("/")) {
                PsiElement psiElement = Parser.nameToElement(project, filePath);
                if (psiElement instanceof NavigatablePsiElement) {
                    navigateSelect(callback, (NavigatablePsiElement) psiElement, childName);
                    return;
                }
            }
            // findFileByNioPath() not support 2020.1
            @Nullable VirtualFile file = VirtualFileManager.getInstance().findFileByUrl("file:///" + filePath);
            if (file == null) {
                callback.failure(41, "file not found: " + filePath);
                return;
            }
            @Nullable PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile == null) {
                callback.failure(42, "psiFile not found: " + filePath);
                return;
            }
            navigateSelect(callback, psiFile, childName);
        });
    }

    private static void navigateSelect(@NotNull CefQueryCallback callback,
                                       @NotNull NavigatablePsiElement element, @NotNull String childName) {
        if (childName.isBlank()) {
            navigate(callback, element);
        }
        if (navigateChild(callback, element, childName)) {
            return;
        }
        navigate(callback, element);
    }

    private static boolean navigateChild(@NotNull CefQueryCallback callback,
                                         @NotNull NavigatablePsiElement element, @NotNull String childName) {
        // findChildrenOfType() slow, so getChildrenOfType() Recursive return when found
        @NotNull List<NavigatablePsiElement> children = PsiTreeUtil.getChildrenOfTypeAsList(element,
                NavigatablePsiElement.class);
        for (@NotNull NavigatablePsiElement child : children) {
            if (childName.equals(child.getName())) {
                navigate(callback, child);
                return true;
            }
            if (navigateChild(callback, child, childName)) {
                return true;
            }
        }
        return false;
    }

    private static void navigate(@NotNull CefQueryCallback callback, @NotNull NavigatablePsiElement element) {
        EdtExecutorService.getInstance().submit(
                () -> ApplicationManager.getApplication().invokeLater(
                        () -> {
                            element.navigate(true);
                            callback.success("navigate: " + element.getName());
                        }
                )
        );
    }
}
