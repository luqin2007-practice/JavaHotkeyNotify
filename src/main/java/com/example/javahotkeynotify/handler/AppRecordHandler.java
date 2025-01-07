package com.example.javahotkeynotify.handler;

import com.example.javahotkeynotify.database.App;
import com.example.javahotkeynotify.util.WindowInfo;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;

import java.util.function.BooleanSupplier;

public class AppRecordHandler extends WinEventHandler {

    private static final int EVENT_SYSTEM_FOREGROUND = 0x0003;

    private App currentApp;
    private BooleanSupplier condition = () -> true;

    public AppRecordHandler() {
        super(EVENT_SYSTEM_FOREGROUND);
    }

    @Override
    public void callback(WinNT.HANDLE hWinEventHook, WinDef.DWORD event, WinDef.HWND hwnd, WinDef.LONG idObject, WinDef.LONG idChild, WinDef.DWORD dwEventThread, WinDef.DWORD dwmsEventTime) {
        // 在等待第一秒时记录窗口信息
        if (condition.getAsBoolean()) {
            WindowInfo currentInfo = new WindowInfo(hwnd);
            currentApp = new App(currentInfo.getProcess(), currentInfo.getExecutePath(), currentInfo.getWindowTitle());
        }
    }

    public App getCurrentApp() {
        return currentApp;
    }

    public void setCondition(BooleanSupplier condition) {
        this.condition = condition;
    }
}
