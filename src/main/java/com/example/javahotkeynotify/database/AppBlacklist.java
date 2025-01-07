package com.example.javahotkeynotify.database;

public class AppBlacklist {

    private final int id;
    private final App app;

    public AppBlacklist(int id, App app) {
        this.id = id;
        this.app = app;
    }

    public App getApp() {
        return app;
    }

    public AppBlacklist mapId(int newId) {
        return new AppBlacklist(newId, app);
    }
}
