package com.example.javahotkeynotify.ui;

import com.example.javahotkeynotify.HelloApplication;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.awt.*;
import java.net.URL;

public class AppTrayIcon extends TrayIcon {

    private static Image loadIcon() {
        URL icon = HelloApplication.class.getResource("icon.png");
        return Toolkit.getDefaultToolkit().getImage(icon);
    }

    private boolean isAdded = false;

    public AppTrayIcon() {
        super(loadIcon());
        setToolTip("快捷键记录器");
        setImageAutoSize(true);
        initMenu();
        addActionListener(e -> HelloApplication.switchStageOpen());
    }

    private void initMenu() {
        PopupMenu menu = new PopupMenu();
        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        menu.add(exit);
        setPopupMenu(menu);
    }

    public AppTrayIcon add() {
        if (isAdded) return this;

        try {
            SystemTray.getSystemTray().add(this);
            isAdded = true;
        } catch (AWTException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.setTitle("状态栏初始化错误");
            alert.showAndWait();
            System.exit(1);
        }

        return this;
    }

    public void remove() {
        if (isAdded) {
            SystemTray.getSystemTray().remove(this);
            isAdded = false;
        }
    }
}
