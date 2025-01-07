package com.example.javahotkeynotify.handler;

import com.example.javahotkeynotify.database.HotKey;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.List;

/**
 * 全局按键监控
 */
public class Recorders {

    // 按 Ctrl+Fn 打开
    private final KeyOpenHandler keyOpenHandler = new KeyOpenHandler();
    // 鼠标移动到顶部打开
//    private final MouseOpenHandler mouseOpenHandler = new MouseOpenHandler();
    // 记录活动应用
//    private final AppRecor0dHandler appRecordHandler = new AppRecordHandler();
    // 记录快捷键
    private final KeyRecordHandler keyRecordHandler;

    private boolean running = false;

    public Recorders(int maxKeyCount, int maxAppCount) {
        keyRecordHandler = new KeyRecordHandler(maxKeyCount, maxAppCount);
//        appRecordHandler.setCondition(() -> isRunning() && mouseOpenHandler.getEqTimes() == 1);
        keyOpenHandler.register();
        keyOpenHandler.handleError("添加启动快捷键监听失败", this);
//        mouseOpenHandler.register();
//        mouseOpenHandler.clearError();
    }

    /**
     * 开始监听
     */
    public void start() {
        if (running) {
            stop();
            // 检查是否成功停止上次的监听
            if (running) {
                return;
            }
        }
        // 添加监听
        keyRecordHandler.register();
        keyRecordHandler.handleError("添加按键记录监听失败", this);
        running = true;
    }

    /**
     * 停止监听
     */
    public void stop() {
        if (running) {
//            appRecordHandler.unregister();
//            appRecordHandler.handleError("移除应用记录监听失败", this);
            keyRecordHandler.unregister();
            keyRecordHandler.handleError("移除按键记录监听失败", this);
//            running = appRecordHandler.isRegistered() && keyRecordHandler.isRegistered();
        }
    }

    public void exit() {
        stop();
//        mouseOpenHandler.unregister();
//        mouseOpenHandler.clearError();
        keyOpenHandler.unregister();
        keyOpenHandler.clearError();
    }

    public boolean isRunning() {
        return running;
    }

    public List<String> getRecentApps() {
        return keyRecordHandler.getRecentApps();
    }

    public List<HotKey> getRecentKeys(String proc) {
        return keyRecordHandler.getRecentKeys(proc);
    }

//    public App getCurrentApp() {
//        return appRecordHandler.getCurrentApp();
//    }

    public void handleError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.show();
    }
}
