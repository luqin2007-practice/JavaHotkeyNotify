package com.example.javahotkeynotify.handler;

import com.example.javahotkeynotify.database.App;
import com.example.javahotkeynotify.database.Keys;
import com.example.javahotkeynotify.database.HotKey;
import com.example.javahotkeynotify.util.WindowInfo;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.util.LinkedList;
import java.util.List;

public class KeyRecordHandler extends WindowsEventHandler implements WinUser.LowLevelKeyboardProc {

    private static final int VK_LWIN = 0x5B;
    private static final int VK_RWIN = 0x5C;

    private Keys tempKey;
    private boolean isLastPressed;

    private final int maxKeyCount, maxAppCount;
    private final LinkedList<String> recentApps;
    private final LinkedList<LinkedList<HotKey>> recentKeys;

    public KeyRecordHandler(int maxKeyCount, int maxAppCount) {
        this.maxAppCount = maxAppCount;
        this.maxKeyCount = maxKeyCount;
        recentApps = new LinkedList<>();
        recentKeys = new LinkedList<>();
    }

    @Override
    public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinUser.KBDLLHOOKSTRUCT lParam) {
        if (nCode >= 0) {
            switch (wParam.intValue()) {
                case WinUser.WM_SYSKEYDOWN:
                    // 按下 Alt
                    tempKey = tempKey.mapSpecialKeys(Keys.ALT_MASK, true);
                    isLastPressed = true;
                    break;
                case WinUser.WM_SYSKEYUP:
                    // 释放 Alt
                    if (isLastPressed && tempKey.isAnySpecialKeysPressed()) {
                        WindowInfo info = WindowInfo.getForegroundWindow();
                        App app = new App(info.getProcess(), info.getExecutePath(), info.getWindowTitle());
                        recordKey(new HotKey(app, tempKey, ""));
                    }
                    tempKey = tempKey.mapSpecialKeys(Keys.ALT_MASK, false);
                    isLastPressed = false;
                    break;
                case WinUser.WM_KEYUP, WinUser.WM_KEYDOWN:
                    // 其他按键
                    boolean isPressed = wParam.intValue() == WinUser.WM_KEYDOWN;
                    // 放开按键 & 之前是按下状态 & 之前有特殊按键
                    if (!isPressed && isLastPressed && tempKey.isAnySpecialKeysPressed()) {
                        WindowInfo info = WindowInfo.getForegroundWindow();
                        App app = new App(info.getProcess(), info.getExecutePath(), info.getWindowTitle());
                        recordKey(new HotKey(app, tempKey, ""));
                    }
                    // 处理各类按键
                    switch (lParam.vkCode) {
                        case WinUser.VK_CONTROL, WinUser.VK_LCONTROL, WinUser.VK_RCONTROL:
                            // ctrl
                            tempKey = tempKey.mapSpecialKeys(Keys.CTRL_MASK, isPressed);
                            break;
                        case WinUser.VK_SHIFT, WinUser.VK_LSHIFT, WinUser.VK_RSHIFT:
                            // shift
                            tempKey = tempKey.mapSpecialKeys(Keys.SHIFT_MASK, isPressed);
                            break;
                        case WinUser.VK_MENU, WinUser.VK_LMENU, WinUser.VK_RMENU:
                            // alt
                            tempKey = tempKey.mapSpecialKeys(Keys.ALT_MASK, isPressed);
                            break;
                        case VK_LWIN, VK_RWIN:
                            // win
                            tempKey = tempKey.mapSpecialKeys(Keys.WIN_MASK, isPressed);
                            break;
                        default:
                            // other keys
                            if (isPressed) {
                                tempKey = tempKey.mapKeyPressed(lParam.vkCode);
                            } else {
                                tempKey = tempKey.mapKeyReleased(lParam.vkCode);
                            }
                            break;
                    }
                    isLastPressed = isPressed;
                    break;
            }
        }

        WinDef.LPARAM lp = new WinDef.LPARAM(Pointer.nativeValue(lParam.getPointer()));
        return User32.INSTANCE.CallNextHookEx(hhook, nCode, wParam, lp);
    }

    private void recordKey(HotKey newValue) {
        // 添加到最近记录
        WindowInfo info = WindowInfo.getForegroundWindow();
        App app = new App(info.getProcess(), info.getExecutePath(), info.getWindowTitle());
        synchronized (recentKeys) {
            String proc = app.getProcess();
            LinkedList<HotKey> keys;
            int pos = recentApps.indexOf(proc);
            if (pos == -1) {
                // 新应用
                if (recentApps.size() == maxAppCount) {
                    // 删除最旧的应用
                    recentApps.removeFirst();
                    recentKeys.removeFirst();
                }
                keys = new LinkedList<>();
                recentApps.addFirst(proc);
                recentKeys.addFirst(keys);
            } else {
                String p = recentApps.remove(pos);
                recentApps.addFirst(p);
                keys = recentKeys.remove(pos);
                recentKeys.addFirst(keys);
            }

            if (keys.size() == maxKeyCount) {
                keys.removeLast();
            }
            keys.addFirst(newValue);
        }
    }

    public List<HotKey> getRecentKeys(String proc) {
        if (proc == null) return List.of();
        synchronized (recentKeys) {
            int pos = recentApps.indexOf(proc);
            if (pos == -1) {
                return List.of();
            }

            return List.copyOf(recentKeys.get(pos));
        }
    }

    public List<String> getRecentApps() {
        synchronized (recentKeys) {
            return List.copyOf(recentApps);
        }
    }

    @Override
    public void reset() {
        super.reset();
        tempKey = new Keys();
        isLastPressed = false;
        if (recentApps != null) {
            recentApps.clear();
            recentKeys.clear();
        }
    }
}
