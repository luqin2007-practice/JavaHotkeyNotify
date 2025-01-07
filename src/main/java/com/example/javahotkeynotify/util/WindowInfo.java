package com.example.javahotkeynotify.util;

import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import java.nio.file.Paths;

/**
 * 窗口
 */
public class WindowInfo {

    private final String executePath, executeFile, title;

    public WindowInfo(WinDef.HWND hwnd) {
        this.executePath = WindowUtils.getProcessFilePath(hwnd);
        this.executeFile = Paths.get(executePath).getFileName().toString();
        this.title = WindowUtils.getWindowTitle(hwnd);
    }

    /**
     * 获取窗口标题。由于标题可能变化，不能缓存值
     * @return 窗口标题
     */
    public String getWindowTitle() {
        return title;
    }

    /**
     * 获取窗口对应的进程
     * @return 进程路径
     */
    public String getExecutePath() {
        return executePath;
    }

    /**
     * 获取窗口对应可执行文件名
     * @return 进程文件名
     */
    public String getProcess() {
        return executeFile;
    }

    public static WindowInfo getForegroundWindow() {
        return new WindowInfo(User32.INSTANCE.GetForegroundWindow());
    }
}
