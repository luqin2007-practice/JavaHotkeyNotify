package com.example.javahotkeynotify.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.example.javahotkeynotify.database.DBTableCreator.*;

public class DB implements AutoCloseable {

    private static final String SQLITE_FILE_CONNECT = "jdbc:sqlite:%s";
    private static final String SQLITE_MEMORY_CONNECT = "jdbc:sqlite::memory:";

    private final Connection connection;

    public DB(String dbFile) throws SQLException {
        String connectUri = dbFile == null
                ? SQLITE_MEMORY_CONNECT
                : String.format(SQLITE_FILE_CONNECT, dbFile);
        connection = DriverManager.getConnection(connectUri);
        initializeTables();
    }

    private void initializeTables() throws SQLException {
        // 应用快捷键表
        new DBTableCreator("hotkeys")
                .column("id", INTEGER, PRIMARY_KEY, AUTO_INCREMENT, NOT_NULL)
                // 进程名 进程路径 窗口名
                .column("process", TEXT)
                .column("path", TEXT)
                .column("window", TEXT)
                // 特殊按键 字母按键
                .column("special_keys", TINY_INT)
                .column("key_count", TINY_INT)
                .column("key1", INTEGER)
                .column("key2", INTEGER)
                .column("key3", INTEGER)
                .column("key4", INTEGER)
                .column("key5", INTEGER)
                // 快捷键介绍
                .column("intro", TEXT)
                .create(connection);
    }

    public List<HotKey> getHotKeys(App qApp) throws SQLException {
        List<HotKey> hotKeys = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select * from hotkeys where");
        boolean added = false;
        if (qApp.getProcess() != null) {
            sql.append(" process='").append(qApp.getProcess()).append("'");
            added = true;
        }
        if (qApp.getExecutePath() != null) {
            if (added) sql.append(" and");
            sql.append(" path='").append(qApp.getExecutePath()).append("'");
            added = true;
        }
        if (qApp.getWindowTitle() != null) {
            if (added) sql.append(" and");
            sql.append(" window='").append(qApp.getWindowTitle()).append("'");
        }
        try (Statement stat = connection.createStatement();
             ResultSet rs = stat.executeQuery(sql.append(";").toString())) {
            while (rs.next()) {
                int id = rs.getInt(1);
                // 进程名
                String process = rs.getString(2);
                // 进程路径
                String path = rs.getString(3);
                // 窗口名
                String window = rs.getString(4);
                // 特殊按键 字母按键
                int specialKeys = rs.getInt(5);
                int keyCount = rs.getInt(6);
                int[] keyArray = new int[5];
                keyArray[0] = rs.getInt(7);
                keyArray[1] = rs.getInt(8);
                keyArray[2] = rs.getInt(9);
                keyArray[3] = rs.getInt(10);
                keyArray[4] = rs.getInt(11);
                // 快捷键介绍
                String intro = rs.getString(12);
                App app = new App(process, path, window);
                Keys keys = new Keys(specialKeys, keyCount, keyArray);
                HotKey hotKey = new HotKey(id, app, keys, intro);
                hotKeys.add(hotKey);
            }
        }
        return hotKeys;
    }

    public List<String> getAllProgress() {
        List<String> list = new ArrayList<>();
        try (Statement stat = connection.createStatement();
             ResultSet rs = stat.executeQuery("select distinct process from hotkeys where process is not null order by process")) {
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<String> getAllPath() {
        List<String> list = new ArrayList<>();
        try (Statement stat = connection.createStatement();
             ResultSet rs = stat.executeQuery("select distinct path from hotkeys where path is not null order by path")) {
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<String> getAllWindow() {
        List<String> list = new ArrayList<>();
        try (Statement stat = connection.createStatement();
             ResultSet rs = stat.executeQuery("select distinct window from hotkeys where window is not null order by window")) {
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public HotKey addHotKey(HotKey hotKey) throws SQLException {
        try (PreparedStatement stat = connection.prepareStatement(
                "insert into hotkeys (process, path, window, special_keys, key_count, key1, key2, key3, key4, key5, intro) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            App app = hotKey.getApp();
            Keys keys = hotKey.getKeys();
            int[] keyArray = keys.getKeys();
            stat.setString(1, app.getProcess());
            stat.setString(2, app.getExecutePath());
            stat.setString(3, app.getWindowTitle());
            stat.setInt(4, keys.getSpecialKeys());
            stat.setInt(5, keys.getKeyCount());
            stat.setInt(6, keyArray[0]);
            stat.setInt(7, keyArray[1]);
            stat.setInt(8, keyArray[2]);
            stat.setInt(9, keyArray[3]);
            stat.setInt(10, keyArray[4]);
            stat.setString(11, hotKey.getIntro());
            stat.execute();
            int id = stat.getGeneratedKeys().getInt(1);
            return hotKey.mapId(id);
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
