package com.example.javahotkeynotify.database;

import java.util.function.Predicate;

public class App implements Predicate<App> {

    /**
     * 进程名
     */
    private final String process;

    /**
     * 可执行文件路径
     */
    private final String executePath;

    /**
     * 窗口标题
     */
    private final String windowTitle;

    public App(String process, String executePath, String windowTitle) {
        this.process = process;
        this.executePath = executePath;
        this.windowTitle = windowTitle;
    }

    public String getProcess() {
        return process;
    }

    public String getExecutePath() {
        return executePath;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    @Override
    public boolean test(App app) {
        // 匹配进程名
        if (this.process != null && !this.process.equals(app.process)) {
            return false;
        }

        // 匹配可执行文件路径
        if (this.executePath != null && !this.executePath.equals(app.executePath)) {
            return false;
        }

        // 匹配窗口标题
        return this.windowTitle == null || this.windowTitle.equals(app.windowTitle);
    }
}
