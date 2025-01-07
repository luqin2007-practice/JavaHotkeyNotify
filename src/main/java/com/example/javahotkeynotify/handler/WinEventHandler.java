package com.example.javahotkeynotify.handler;

import com.sun.jna.platform.win32.*;

public abstract class WinEventHandler implements WinUser.WinEventProc {

    private static WinDef.HMODULE hmod;

    private WinNT.HANDLE handle;
    private final int event;
    private String error;

    public WinEventHandler(int event) {
        this.event = event;
    }

    public boolean register() {
        error = null;
        if (handle != null) {
            if (!unregister()) {
                return false;
            }
        }

        if (hmod == null) {
            hmod = Kernel32.INSTANCE.GetModuleHandle(null);
        }
        handle = User32.INSTANCE.SetWinEventHook(event, event, null, this, 0, 0, 0);
        if (handle == null) {
            error = Kernel32Util.getLastErrorMessage();
            return false;
        }
        return true;
    }

    public boolean unregister() {
        if (handle != null) {
            boolean succeed = User32.INSTANCE.UnhookWinEvent(handle);
            if (succeed) {
                handle = null;
            } else {
                error = Kernel32Util.getLastErrorMessage();
                return false;
            }
        }
        return true;
    }

    public void handleError(String title, Recorders recorders) {
        if (error != null) {
            recorders.handleError(title, error);
        }
    }

    public boolean isRegistered() {
        return handle != null;
    }
}
