package com.realvnc.jvckenwood.util;

public class StringUtility {

    public static final String ByteToHexString(byte[] data) {
        final int len = data.length;
        if (len == 0) {
            return "";
        }
        if (len == 1) {
            return String.format("%02X", data[0]);
        }
        StringBuilder builder = new StringBuilder(len * 3 - 1);
        builder.append(String.format("%02X", data[0]));
        for (int i = 1; i < len; ++i) {
            builder.append(String.format(",%02X", data[i]));
        }
        return builder.toString();
    }
}
