package com.example.javahotkeynotify.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 按键情况，包含特殊按键和其他按键的 VK 值
 */
public class Keys implements Predicate<Keys> {

    public static final int SHIFT_MASK = 0x01;
    public static final int CTRL_MASK = 0x02;
    public static final int WIN_MASK = 0x04;
    public static final int ALT_MASK = 0x08;

    private final int specialKeys;
    private final int keyCount;
    private final int[] keys;

    private String keysName;

    public Keys(int specialKeys, int count, int[] keys) {
        this.specialKeys = specialKeys;
        this.keyCount = count;
        this.keys = keys;
    }

    public Keys() {
        this(0, 0, new int[]{-1, -1, -1, -1, -1});
    }

    public Keys mapSpecialKeys(int specialKeys, boolean isPressed) {
        if (isPressed) {
            return new Keys(this.specialKeys | specialKeys, keyCount, keys);
        } else {
            return new Keys(this.specialKeys & ~specialKeys, keyCount, keys);
        }
    }

    public Keys mapKeyReleased(int key) {
        int i = Arrays.binarySearch(keys, 0, keyCount, key);
        // 按键未记录
        if (i < 0) return this;
        // 删除一个按键
        int[] newKeys = new int[5];
        System.arraycopy(keys, 0, newKeys, 0, i);
        System.arraycopy(keys, i + 1, newKeys, i, keyCount - i - 1);
        return new Keys(specialKeys, keyCount - 1, newKeys);
    }

    public Keys mapKeyPressed(int key) {
        // 按键为 -1 时无效
        if (key == -1) return this;

        // 检查是否已经按下
        if (Arrays.binarySearch(keys, 0, keyCount, key) >= 0) {
            return this;
        }

        // 最多支持 5 个按键，删除第一个按键记录
        int[] newKeys = new int[5];
        int newCount;
        if (keyCount == 5) {
            System.arraycopy(keys, 1, newKeys, 0, keyCount - 1);
            newCount = keyCount - 1;
        } else {
            System.arraycopy(keys, 0, newKeys, 0, keyCount);
            newCount = keyCount;
        }

        newKeys[newCount] = key;
        Arrays.sort(newKeys, 0, newCount + 1);
        return new Keys(specialKeys, newCount + 1, newKeys);
    }

    public int getSpecialKeys() {
        return specialKeys;
    }

    public int getKeyCount() {
        return keyCount;
    }

    public int[] getKeys() {
        Arrays.fill(keys, keyCount, 5, -1);
        return keys;
    }

    public boolean isAnySpecialKeysPressed() {
        return specialKeys > 0;
    }

    public boolean isAnySpecialKeysPressed(int keyMask) {
        return (specialKeys & keyMask) == keyMask;
    }

    public List<String> getKeyNameList() {
        List<String> keys = new ArrayList<>();
        if (isAnySpecialKeysPressed(CTRL_MASK)) {
            keys.add("Ctrl");
        }
        if (isAnySpecialKeysPressed(ALT_MASK)) {
            keys.add("Alt");
        }
        if (isAnySpecialKeysPressed(SHIFT_MASK)) {
            keys.add("Shift");
        }
        if (isAnySpecialKeysPressed(WIN_MASK)) {
            keys.add("Win");
        }
        for (int i = 0; i < keyCount; i++) {
            appendKeyString(this.keys[i], keys);
        }
        return keys;
    }

    public String getKeyNames() {
        if (keysName == null) {
            keysName = String.join(" + ", getKeyNameList());
        }
        return keysName;
    }

    private void appendKeyString(int key, List<String> list) {
        String k = keyToString(key);
        if (k != null) {
            list.add(k);
        }
    }

    @Override
    public boolean test(Keys keys) {
        return keys.isAnySpecialKeysPressed(specialKeys) && Arrays.equals(keys.keys, this.keys);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Keys keys1 = (Keys) o;
        return specialKeys == keys1.specialKeys && Objects.deepEquals(keys, keys1.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(specialKeys, Arrays.hashCode(keys));
    }

    private static String keyToString(int key) {
        if (key < 0) {
            return null;
        }

        // 0-9
        if (key >= 0x30 && key <= 0x39) {
            return String.valueOf(key - 0x30);
        }

        // A-Z
        if (key >= 0x41 && key <= 0x5A) {
            return String.valueOf((char) ('A' + (key - 0X41)));
        }

        // VK_NUMPAD0-VK_NUMPAD9
        if (key >= 0x60 && key <= 0x69) {
            return "Num " + (key - 0x60);
        }

        // VK_F1-VK_F24
        if (key >= 0x70 && key <= 0x87) {
            return "F" + (key - 0x70 + 1);
        }

        switch (key) {
            case 0x6A: // VK_MULTIPLY
                return "Num *";
            case 0x6B: // VK_ADD
                return "Num +";
            case 0x6C: // VK_SEPARATOR
                return "Num Enter";
            case 0x6D: // VK_SUBTRACT
                return "Num -";
            case 0x6E: // VK_DECIMAL
                return "Num .";
            case 0x6F: // VK_DIVIDE
                return "Num /";
            case 0xBA: // VK_OEM_1 ;:
                return ";";
            case 0xBB: // VK_OEM_PLUS
                return "+";
            case 0xBC: // VK_OEM_COMMA
                return ",";
            case 0xBD: // VK_OEM_MINUS
                return "-";
            case 0xBE: // VK_OEM_PERIOD
                return ".";
            case 0xBF: // VK_OEM_2 /?
                return "/";
            case 0xC0: // VK_OEM_3 `~
                return "`";
            case 0xDB: // VK_OEM_4 [{
                return "[";
            case 0xDC: // VK_OEM_5 \|
                return "\\";
            case 0xDD: // VK_OEM_6 ]}
                return "]";
            case 0xDE: // VK_OEM_7 '"
                return "'";
            case 8:
                return "Backspace";
            case 9:
                return "Tab";
            case 13:
                return "Enter";
            case 19:
                return "Pause";
            case 20:
                return "CapsLk";
            case 27:
                return "Esc";
            case 32:
                return "Space";
            case 33:
                return "PgUp";
            case 34:
                return "PgDown";
            case 35:
                return "End";
            case 36:
                return "Home";
            case 37:
                return "←";
            case 38:
                return "↑";
            case 39:
                return "→";
            case 40:
                return "↓";
            case 44:
                return "PrtSc";
            case 45:
                return "Ins";
            case 46:
                return "Del";
            case 144:
                return "Num";
            case 145:
                return "ScLk";
            default:
                return "UNKNOWN_VK_" + key;
        }
    }
}
