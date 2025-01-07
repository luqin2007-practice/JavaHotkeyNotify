package com.example.javahotkeynotify.database;

public class HotKey {

    private final int id;
    private final App app;
    private final Keys keys;
    private final String intro;

    public HotKey(int id, App app, Keys keys, String intro) {
        this.id = id;
        this.app = app;
        this.keys = keys;
        this.intro = intro;
    }

    public HotKey(App app, Keys keys, String intro) {
        this(-1, app, keys, intro);
    }

    public App getApp() {
        return app;
    }

    public Keys getKeys() {
        return keys;
    }

    public String getIntro() {
        return intro;
    }

    public HotKey mapId(int newId) {
        return new HotKey(newId, app, keys, intro);
    }
}
