package com.chinczyk.util;

/**
 * Klasa pomocnicza z metodami statycznymi.
 */
public class Utils {
    public static String pad(String s, int len) {
        if (s == null) s = "";
        while (s.length() < len) s += " ";
        return s;
    }
}
