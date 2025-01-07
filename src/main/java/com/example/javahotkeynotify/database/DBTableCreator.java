package com.example.javahotkeynotify.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTableCreator {

    public static final String PRIMARY_KEY = "primary key";
    public static final String AUTO_INCREMENT = "autoincrement";
    public static final String NOT_NULL = "not null";

    public static final String INTEGER = "integer";
    public static final String TINY_INT = "tinyint";
    public static final String TEXT = "string";

    public StringBuilder sb = new StringBuilder();

    private boolean firstColumn = true;
    private String sql = null;

    public DBTableCreator(String tableName) {
        sb.append(String.format("CREATE TABLE IF NOT EXISTS %s (", tableName));
    }

    public DBTableCreator column(String name, String type, String... constraints) {
        if (!firstColumn) {
            sb.append(",");
        }
        sb.append(String.format("%s %s", name, type));
        for (String constraint : constraints) {
            sb.append(String.format(" %s", constraint));
        }
        firstColumn = false;
        return this;
    }

    public DBTableCreator foreignKey(String key, String foreignTable, String foreignKey) {
        sb.append(String.format(",foreign key(%s) references %s(%s) on delete cascade", key, foreignTable, foreignKey));
        return this;
    }

    public void create(Connection connection) throws SQLException {
        if (sql == null) {
            sql = sb.append(");").toString();
        }
        System.out.println(sql);
        Statement stat = connection.createStatement();
        stat.execute(sql);
        stat.close();
    }
}
