package com.example.javahotkeynotify.util;

import com.example.javahotkeynotify.database.HotKey;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.List;

public class HotKeyStringConverter extends StringConverter<HotKey> {

    private final HashMap<String, HotKey> keyMap = new HashMap<>();

    @Override
    public String toString(HotKey object) {
        if (object == null) {
            return "<无记录>";
        }
        return object.getKeys().getKeyNames();
    }

    @Override
    public HotKey fromString(String string) {
        return keyMap.get(string);
    }

    public void setHotKeys(List<HotKey> hotKeys) {
        keyMap.clear();
        for (HotKey hotKey : hotKeys) {
            keyMap.put(hotKey.getKeys().getKeyNames(), hotKey);
        }
    }
}
