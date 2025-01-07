package com.example.javahotkeynotify.ui;

import com.example.javahotkeynotify.HelloApplication;
import com.example.javahotkeynotify.database.App;
import com.example.javahotkeynotify.database.HotKey;
import com.example.javahotkeynotify.database.Keys;
import com.example.javahotkeynotify.event.KeyAddEvent;
import com.example.javahotkeynotify.util.HotKeyStringConverter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class AddHotKeysController {

    public ChoiceBox<String> processes;
    public ChoiceBox<HotKey> hotkeys;
    public TextArea intro;
    public CheckBox cbProc;
    public CheckBox cbPath;
    public CheckBox cbTitle;
    public Label lProc;
    public Label lPath;
    public Label lTitle;

    public ObservableList<HotKey> keys = FXCollections.observableArrayList();
    public ObservableList<String> currentApps = FXCollections.observableArrayList();

    public SimpleStringProperty proc = new SimpleStringProperty("未知");
    public SimpleStringProperty path = new SimpleStringProperty("未知");
    public SimpleStringProperty title = new SimpleStringProperty("未知");

    private final HotKeyStringConverter converter = new HotKeyStringConverter();

    @FXML
    public void initialize() {
        lProc.textProperty().bind(proc.map(s -> "进程名：" + s));
        lPath.textProperty().bind(path.map(s -> "可执行文件：" + s));
        lTitle.textProperty().bind(title.map(s -> "窗口标题：" + s));

        processes.itemsProperty().bind(new SimpleObjectProperty<>(currentApps));
        hotkeys.itemsProperty().bind(new SimpleObjectProperty<>(keys));
        hotkeys.setConverter(converter);
    }

    public void onProcessSelected() {
        String pro = processes.getSelectionModel().getSelectedItem();
        List<HotKey> hk = HelloApplication.recorder.getRecentKeys(pro);
        keys.clear();
        keys.addAll(hk);
        converter.setHotKeys(hk);
    }

    public void onHotkeySelected() {
        HotKey hotKey = hotkeys.getSelectionModel().getSelectedItem();
        if (hotKey != null) {
            proc.setValue(hotKey.getApp().getProcess());
            path.setValue(hotKey.getApp().getExecutePath());
            title.setValue(hotKey.getApp().getWindowTitle());
        }
    }

    public void addHotkey(ActionEvent event) {
        // 校验：快捷键已选择
        HotKey hotKey = hotkeys.getSelectionModel().getSelectedItem();
        Keys key = hotKey == null ? null : hotKey.getKeys();
        if (key == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "请选择快捷键", ButtonType.OK);
            alert.show();
            return;
        }

        // 校验：作用域已选择
        String mp = cbProc.isSelected() ? proc.getValue() : null;
        String me = cbPath.isSelected() ? path.getValue() : null;
        String mt = cbTitle.isSelected() ? title.getValue() : null;
        if (mp == null && me == null && mt == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "请选择范围", ButtonType.OK);
            alert.show();
            return;
        }
        App app = new App(mp, me, mt);

        // 校验：快捷键说明已填写
        String introText = intro.getText();
        if (introText == null || introText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "请输入快捷键说明", ButtonType.OK);
            alert.show();
            return;
        }

        HotKey hk = new HotKey(app, key, introText);

        // 存储快捷键到数据库
        try {
            hk = HelloApplication.database.addHotKey(hk);
            KeyAddEvent ev = new KeyAddEvent(hk);
            Event.fireEvent(ev.getTarget(), ev);
            closeAdd(event);
        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
            String sqlState = e.getSQLState();
            String message = e.getMessage();
            Alert alert = new Alert(Alert.AlertType.ERROR, String.format("%d %s\n%s", errorCode, sqlState, message), ButtonType.OK);
            alert.setTitle("数据库错误");
            alert.show();
        }
    }

    public void closeAdd(ActionEvent event) {
        Button button = (Button) event.getSource();
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }

    public void setCurrentApps(List<String> rApp) {
        if (rApp.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "找不到记录应用", ButtonType.OK);
            alert.show();
            return;
        }
        currentApps.clear();
        currentApps.addAll(rApp);
        if (rApp.size() > 1) {
            processes.getSelectionModel().select(1);
        }
    }
}
