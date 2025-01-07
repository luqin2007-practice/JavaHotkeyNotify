package com.example.javahotkeynotify.handler;

import com.example.javahotkeynotify.HelloApplication;
import com.example.javahotkeynotify.util.ScreenSize;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

public class MouseOpenHandler extends WindowsEventHandler implements WinUser.LowLevelMouseProc {
    private static final int WM_MOUSEMOVE = 0x0200;

    private double lastX;
    private int eqTimes;
    private long lastRecTime;

    @Override
    public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinUser.MSLLHOOKSTRUCT lParam) {
        if (nCode >= 0) {
            // 每 1s 监控一次
            long curTime = System.currentTimeMillis();
            if (curTime - lastRecTime >= 1000) {
                System.out.println(wParam.intValue() + " " + lParam.pt.x + " " + lParam.pt.y + " " + WM_MOUSEMOVE);
                // 只监控鼠标移动事件
                if (wParam.intValue() == WM_MOUSEMOVE) {
                    ScreenSize ss = ScreenSize.getLastSize();
                    double mx = lParam.pt.x;
                    double my = lParam.pt.y;
                    // 鼠标选停在指定区域
                    if (Math.abs(mx - lastX) < 0.001 && Math.abs(my) < 0.001 && mx >= ss.x0 && mx <= ss.x1) {
                        eqTimes++;
                    } else {
                        eqTimes = 0;
                        lastX = mx;
                    }
                } else {
                    eqTimes = 0;
                }
                // 等待 3s
                if (eqTimes >= 3) {
                    eqTimes = 0;
                    HelloApplication.openStage();
                }
            }
            lastRecTime = curTime;
        }

        WinDef.LPARAM lp = new WinDef.LPARAM(Pointer.nativeValue(lParam.getPointer()));
        return User32.INSTANCE.CallNextHookEx(hhook, nCode, wParam, lp);
    }

    public int getEqTimes() {
        return eqTimes;
    }

    @Override
    public void reset() {
        super.reset();
        lastX = -1;
        eqTimes = 0;
        lastRecTime = 0;
    }
}
