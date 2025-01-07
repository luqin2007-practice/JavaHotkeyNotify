package com.example.javahotkeynotify.handler;

import com.sun.jna.platform.win32.*;

import java.util.List;
import java.util.function.BiConsumer;

public abstract class WindowsEventHandler implements WinUser.HOOKPROC {

    private static WinDef.HMODULE hmod;

    protected WinUser.HHOOK hhook;
    private final int idHook;
    private String error;

    public WindowsEventHandler() {
        if (this instanceof WinUser.LowLevelKeyboardProc) {
            idHook = WinUser.WH_KEYBOARD_LL;
        } else if (this instanceof WinUser.LowLevelMouseProc) {
            idHook = WinUser.WH_MOUSE_LL;
        } else {
            throw new IllegalArgumentException("Unknown hook type");
        }
        reset();
    }

    public void register() {
        if (hhook != null) {
            unregister();
            if (error != null) {
                return;
            }
        }

        if (hmod == null) {
            hmod = Kernel32.INSTANCE.GetModuleHandle(null);
        }
        hhook = User32.INSTANCE.SetWindowsHookEx(idHook, this, hmod, 0);
        if (hhook == null) {
            error = Kernel32Util.getLastErrorMessage();
        }
    }

    public void unregister() {
        if (hhook != null) {
            boolean succeed = User32.INSTANCE.UnhookWindowsHookEx(hhook);
            if (succeed) {
                hhook = null;
            } else {
                error = Kernel32Util.getLastErrorMessage();
                return;
            }
        }
        reset();
    }

    public void handleError(String title, Recorders recorders) {
        if (error != null) {
            recorders.handleError(title, error);
            error = null;
        }
    }

    public boolean isRegistered() {
        return hhook != null;
    }

    public void clearError() {
        error = null;
    }

    public void reset() {
        clearError();
    }
}
