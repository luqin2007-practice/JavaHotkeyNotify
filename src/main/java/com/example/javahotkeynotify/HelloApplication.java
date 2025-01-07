package com.example.javahotkeynotify;

import com.example.javahotkeynotify.database.DB;
import com.example.javahotkeynotify.handler.Recorders;
import com.example.javahotkeynotify.ui.AppTrayIcon;
import com.example.javahotkeynotify.util.ScreenSize;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.SQLException;

public class HelloApplication extends Application {

    public static Stage stage;
    public static DB database;
    public static Recorders recorder;
    public static AppTrayIcon trayIcon;

    public static void main(String[] args) throws SQLException {
        Platform.setImplicitExit(false);
        trayIcon = new AppTrayIcon().add();
        database = new DB("database.db");
        recorder = new Recorders(10, 5);
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hotkey-board.fxml"));
        primaryStage.setTitle("按键列表");
        // 设置双层 Stage 构建无图标、无边框界面
        ScreenSize size = ScreenSize.getLastSize();
        primaryStage.setAlwaysOnTop(true);
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setOpacity(0);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> exitEnvironment());
        stage = new Stage(StageStyle.TRANSPARENT);
        stage.initOwner(primaryStage);
        // 内部视图
        stage.setX(size.x0);
        stage.setY(0);
        stage.setWidth(size.width);
        stage.setHeight(size.height);
        ScreenSize area = ScreenSize.calcScreen();
        Scene scene = new Scene(fxmlLoader.load(), area.width, area.height);
        stage.setScene(scene);

        primaryStage.show();
        stage.show();
    }

    public static void exitEnvironment() {
        if (recorder != null) {
            recorder.exit();
            recorder = null;
        }
        if (database != null) {
            try {
                database.close();
            } catch (Exception ignored) {
            } finally {
                database = null;
            }
        }
        if (trayIcon != null) {
            trayIcon.remove();
            trayIcon = null;
        }
        stage = null;
        Platform.exit();
    }

    public static void switchStageOpen() {
        if (stage != null && stage.isShowing()) {
            hideStage();
        } else {
            openStage();
        }
    }

    public static void openStage() {
        if (stage != null) {
            if (Platform.isFxApplicationThread()) {
                _showStage();
            } else {
                Platform.runLater(HelloApplication::_showStage);
            }
        }
    }

    public static void hideStage() {
        if (stage != null) {
            if (Platform.isFxApplicationThread()) {
                _hideStage();
            } else {
                Platform.runLater(HelloApplication::_hideStage);
            }
        }
    }

    private static void _showStage() {
        if (!stage.isShowing()) {
            ScreenSize size = ScreenSize.calcScreen();
            stage.setWidth(size.width);
            stage.setHeight(size.height);
            stage.setX(size.x0);
            stage.setY(0);
            stage.show();
            stage.toFront();
        }
    }

    private static void _hideStage() {
        if (stage.isShowing()) {
            stage.hide();
        }
    }
}