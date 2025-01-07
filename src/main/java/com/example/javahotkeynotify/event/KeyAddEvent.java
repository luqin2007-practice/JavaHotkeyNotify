package com.example.javahotkeynotify.event;

import com.example.javahotkeynotify.database.HotKey;
import javafx.event.Event;
import javafx.event.EventType;

public class KeyAddEvent extends Event {

    public static final EventType<KeyAddEvent> KEY_ADD_EVENT = new EventType<>(Event.ANY, "KEY_ADD_EVENT");

    private final HotKey hotKey;

    public KeyAddEvent(HotKey hotKey) {
        super(KEY_ADD_EVENT);
        this.hotKey = hotKey;
    }

    public HotKey getHotKey() {
        return hotKey;
    }
}
