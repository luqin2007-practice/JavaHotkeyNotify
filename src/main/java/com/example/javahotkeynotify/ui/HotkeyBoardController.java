package com.example.javahotkeynotify.ui;

import com.example.javahotkeynotify.HelloApplication;
import com.example.javahotkeynotify.database.App;
import com.example.javahotkeynotify.database.HotKey;
import com.example.javahotkeynotify.event.KeyAddEvent;
import com.example.javahotkeynotify.util.WindowInfo;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

public class HotkeyBoardController extends TabPane {

    private static volatile Background KEY_BACKGROUND;

    private static Background getKeyBackground() {
        if (KEY_BACKGROUND == null) {
            synchronized (HotkeyBoardController.class) {
                if (KEY_BACKGROUND == null) {
                    try (InputStream resource = HelloApplication.class.getResourceAsStream("button.png")) {
                        assert resource != null;
                        Image image = new Image(resource);
                        BackgroundImage bi = new BackgroundImage(image,
                                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.DEFAULT,
                                new BackgroundSize(1.0, 1.0, true, true, false, false));
                        KEY_BACKGROUND = new Background(bi);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return KEY_BACKGROUND;
    }

    public TabPane root;
    public Tab tAdd;
    public Tab tRefresh;
    public Tab tListen;
    public Tab tApp;
    public Tab tProc;
    public Tab tWindow;

    private App showApp = null;
    private boolean isKeepShow = false;
    private Timer keepShowTimer = null;

    @FXML
    public void initialize() {
        // 初始化各类功能按钮
        tListen.setText(switchListener() ? "●" : "○");
        root.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == tAdd) {
                root.getSelectionModel().select(oldValue);
                openAddPanel();
            } else if (newValue == tRefresh) {
                root.getSelectionModel().select(oldValue);
                refreshKeys();
            } else if (newValue == tListen) {
                root.getSelectionModel().select(oldValue);
                tListen.setText(switchListener() ? "●" : "○");
            } else {
                showApp = createDefaultApp();
                refreshKeys();
            }
        });

        // 添加快捷键事件监听
        root.addEventHandler(KeyAddEvent.KEY_ADD_EVENT, event -> {
            HotKey hotKey = event.getHotKey();
            Tab tab = root.getSelectionModel().getSelectedItem();
            VBox content = (VBox) ((ScrollPane) tab.getContent()).getContent();
            addHotkeyLine(content, hotKey);
        });

        showApp = createDefaultApp();
        refreshKeys();
    }

    private void openAddPanel() {
        try {
            // 添加新快捷键
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("add-hotkeys.fxml"));
            Parent load = loader.load();

            AddHotKeysController controller = loader.getController();
            controller.setCurrentApps(HelloApplication.recorder.getRecentApps());

            Stage stage = new Stage();
            stage.setTitle("添加快捷键");
            stage.setResizable(false);
            stage.setScene(new Scene(load));
            HelloApplication.hideStage();
            stage.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.show();
        } finally {
            HelloApplication.openStage();
        }
    }

    private void refreshKeys() {
        // 清空面板内容
        Tab tab = root.getSelectionModel().getSelectedItem();
        VBox content = (VBox) ((ScrollPane) tab.getContent()).getContent();
        content.getChildren().clear();
        Label label = new Label("加载中...");
        content.getChildren().add(label);
        // 检索快捷键列表
        try {
            List<HotKey> hotKeys = HelloApplication.database.getHotKeys(showApp);
            // 更新快捷键列表
            content.getChildren().clear();
            for (HotKey hotKey : hotKeys) {
                addHotkeyLine(content, hotKey);
            }
        } catch (SQLException e) {
            content.getChildren().clear();
            label.setStyle("-fx-text-fill: red;");
            label.setText(String.format("%d %s\n%s", e.getErrorCode(), e.getSQLState(), e.getMessage()));
            content.getChildren().add(label);
        }
    }

    private boolean switchListener() {
        if (HelloApplication.recorder.isRunning()) {
            HelloApplication.recorder.stop();
        } else {
            HelloApplication.recorder.start();
        }
        return HelloApplication.recorder.isRunning();
    }

    private App createDefaultApp() {
        WindowInfo current = WindowInfo.getForegroundWindow();
        Tab tab = root.getSelectionModel().getSelectedItem();
        if (tab == tApp) {
            return new App(current.getExecutePath(), null, null);
        }

        if (tab == tProc) {
            return new App(null, current.getProcess(), null);
        }

        if (tab == tWindow) {
            return new App(null, null, current.getWindowTitle());
        }

        throw new RuntimeException("Unknown tab: " + tab.getText());
    }

    public void onMouseExit() {
        if (!isKeepShow) {
            HelloApplication.hideStage();
        }
    }

    public void onAppPaneClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            // 右键：选择显示的应用
            ContextMenu menu = new ContextMenu();
            Stream<String> fromOS = WindowUtils.getAllWindows(true).stream()
                    .map(DesktopWindow::getFilePath)
                    .map(p -> Path.of(p).getFileName().toString())
                    .sorted();
            Stream<String> fromDB = HelloApplication.database.getAllProgress().stream();
            Stream.concat(fromDB, fromOS)
                    .distinct()
                    .map(p -> {
                        MenuItem item = new MenuItem(p);
                        item.setOnAction(event -> {
                            showApp = new App(p, null, null);
                            refreshKeys();
                            keepShowShortly();
                        });
                        return item;
                    })
                    .forEach(item -> menu.getItems().add(item));
            isKeepShow = true;
            menu.show((Node) mouseEvent.getSource(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
        }
    }

    public void onProcPaneClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            // 右键：选择显示的进程
            ContextMenu menu = new ContextMenu();
            Stream<String> fromOS = WindowUtils.getAllWindows(true).stream()
                    .map(DesktopWindow::getFilePath)
                    .distinct()
                    .sorted();
            Stream<String> fromDB = HelloApplication.database.getAllPath().stream();
            Stream.concat(fromDB, fromOS)
                    .map(p -> {
                        String proc = Path.of(p).getFileName().toString();
                        Label label = new Label(proc);
                        Tooltip.install(label, new Tooltip(p));
                        CustomMenuItem item = new CustomMenuItem(label);
                        item.setOnAction(event -> {
                            showApp = new App(null, p, null);
                            refreshKeys();
                            keepShowShortly();
                        });
                        return item;
                    })
                    .forEach(item -> menu.getItems().add(item));
            isKeepShow = true;
            menu.show((Node) mouseEvent.getSource(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
        }
    }

    public void onTitlePaneClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            // 右键：选择显示的窗口
            ContextMenu menu = new ContextMenu();
            Stream<String> fromOS = WindowUtils.getAllWindows(true).stream()
                    .map(w -> {
                        String title = w.getTitle();
                        if (title.isEmpty()) {
                            title = "( 无标题 ) " + Path.of(w.getFilePath()).getFileName().toString();
                        }
                        return title;
                    })
                    .distinct()
                    .sorted();
            Stream<String> fromDB = HelloApplication.database.getAllProgress().stream();
            Stream.concat(fromDB, fromOS)
                    .map(title -> {
                        MenuItem item = new MenuItem(title);
                        item.setOnAction(event -> {
                            showApp = new App(null, null, title);
                            refreshKeys();
                            keepShowShortly();
                        });
                        return item;
                    })
                    .forEach(item -> menu.getItems().add(item));
            isKeepShow = true;
            menu.show((Node) mouseEvent.getSource(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
        }
    }

    private void keepShowShortly() {
        if (keepShowTimer != null) {
            keepShowTimer.cancel();
        }
        isKeepShow = true;
        keepShowTimer = new Timer();
        // 等待 1s 后
        keepShowTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isKeepShow = false;
            }
        }, 1000);
    }

    private void addHotkeyLine(VBox content, HotKey hotKey) {
        FlowPane pane = new FlowPane();
        pane.setHgap(10);
        pane.setPadding(new Insets(10));
        for (String key : hotKey.getKeys().getKeyNameList()) {
            Label keyLabel = createKeyLabel(key);
            pane.getChildren().add(keyLabel);
        }
        Label intro = new Label(hotKey.getIntro());
        intro.setPadding(new Insets(0, 0, 10, 20));
        content.getChildren().addAll(pane, intro);
    }

    private Label createKeyLabel(String key) {
        Label label = new Label(key);
        label.setBackground(getKeyBackground());
        if (key.length() < 5) {
            label.setPadding(new Insets(10));
        } else {
            label.setPadding(new Insets(10, 15, 10, 15));
        }
        return label;
    }
}
