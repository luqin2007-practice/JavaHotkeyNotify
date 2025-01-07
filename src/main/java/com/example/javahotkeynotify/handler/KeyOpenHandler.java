package com.example.javahotkeynotify.handler;

import com.example.javahotkeynotify.HelloApplication;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

public class KeyOpenHandler extends WindowsEventHandler implements WinUser.LowLevelKeyboardProc {

    private boolean isCtrl, isFn;

    @Override
    public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinUser.KBDLLHOOKSTRUCT lParam) {
        if (nCode >= 0) {
            if (wParam.intValue() == WinUser.WM_KEYDOWN) {
                if (lParam.vkCode == WinUser.VK_CONTROL || lParam.vkCode == WinUser.VK_LCONTROL || lParam.vkCode == WinUser.VK_RCONTROL) {
                    isCtrl = true;
                } else if (lParam.vkCode == (0x70 + 11)) {
                    isFn = true;
                }
                System.out.println(lParam.vkCode + " " + isCtrl + " " + isFn);
            } else if (wParam.intValue() == WinUser.WM_KEYUP) {
                if (lParam.vkCode == WinUser.VK_CONTROL || lParam.vkCode == WinUser.VK_LCONTROL || lParam.vkCode == WinUser.VK_RCONTROL) {
                    isCtrl = false;
                } else if (lParam.vkCode >= 0x70 && lParam.vkCode <= 0x70 + 11) {
                    isFn = false;
                }
                System.out.println(lParam.vkCode + " " + isCtrl + " " + isFn);
            }

            if (isCtrl && isFn) {
                HelloApplication.openStage();
                reset();
            }
        }

        WinDef.LPARAM lp = new WinDef.LPARAM(Pointer.nativeValue(lParam.getPointer()));
        return User32.INSTANCE.CallNextHookEx(hhook, nCode, wParam, lp);
    }

    @Override
    public void reset() {
        super.reset();
        isCtrl = false;
        isFn = false;
    }
}
