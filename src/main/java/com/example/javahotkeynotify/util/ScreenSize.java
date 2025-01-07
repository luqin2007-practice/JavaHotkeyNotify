package com.example.javahotkeynotify.util;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class ScreenSize {

    private static ScreenSize lastSize = null;

    public double x0, x1, width, height;

    public ScreenSize(double x0, double x1, double width, double height) {
        this.x0 = x0;
        this.x1 = x1;
        this.width = width;
        this.height = height;
    }

    public static ScreenSize calcScreen() {
        Screen primary = Screen.getPrimary();
        Rectangle2D bounds = primary.getVisualBounds();
        double height = bounds.getHeight() / 4;
        double width = bounds.getWidth() / 3;
        lastSize = new ScreenSize(width, width * 2, width, height);
        return lastSize;
    }

    public static ScreenSize getLastSize() {
        return lastSize == null ? calcScreen() : lastSize;
    }
}
