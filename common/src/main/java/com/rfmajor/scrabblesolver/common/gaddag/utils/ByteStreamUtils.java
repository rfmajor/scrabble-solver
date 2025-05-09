package com.rfmajor.scrabblesolver.common.gaddag.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ByteStreamUtils {
    public static final String ROOT_ARC = "rootArc";
    public static final String ALPHABET = "alphabet";
    public static final String DELIMITER = "delimiter";
    public static final String LETTER_SETS = "letterSets";
    public static final String ARCS_AND_STATES = "arcsAndStates";

    public static byte[] intArrayToBytes(int[] array) {
        byte[] byteArray = new byte[array.length * 4];
        for (int i = 0; i < array.length; i++) {
            byteArray[i * 4] = (byte) (array[i]);
            byteArray[i * 4 + 1] = (byte) (array[i] >> 8);
            byteArray[i * 4 + 2] = (byte) (array[i] >> 16);
            byteArray[i * 4 + 3] = (byte) (array[i] >> 24);
        }
        return byteArray;
    }

    public static byte[] charArrayToBytes(char[] array) {
        byte[] byteArray = new byte[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            byteArray[i * 2] = (byte) (array[i]);
            byteArray[i * 2 + 1] = (byte) (array[i] >> 8);
        }
        return byteArray;
    }

    public static byte[] intToBytes(long value) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (value >> (8 * i));
        }
        return bytes;
    }

    public static byte[] longToBytes(long value) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (value >> (8 * i));
        }
        return bytes;
    }

    public static byte[] charToBytes(char value) {
        byte[] bytes = new byte[2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (value >> (8 * i));
        }
        return bytes;
    }

    public static int[] bytesToIntArray(byte[] bytes) {
        int[] intArray = new int[bytes.length / 4];
        for (int i = 0; i < intArray.length; i++) {
            int j = 4 * i;
            intArray[i] |= Byte.toUnsignedInt(bytes[j]);
            intArray[i] |= (Byte.toUnsignedInt(bytes[j + 1]) << 8);
            intArray[i] |= (Byte.toUnsignedInt(bytes[j + 2]) << 16);
            intArray[i] |= (Byte.toUnsignedInt(bytes[j + 3]) << 24);
        }
        return intArray;
    }

    public static char[] bytesToCharArray(byte[] bytes) {
        char[] charArray = new char[bytes.length / 2];
        for (int i = 0; i < charArray.length; i++) {
            int j = 2 * i;
            charArray[i] |= (char) Byte.toUnsignedInt(bytes[j]);
            charArray[i] |= (char) (Byte.toUnsignedInt(bytes[j + 1]) << 8);
        }
        return charArray;
    }

    public static char bytesToChar(byte[] bytes) {
        if (bytes.length != 2) {
            throw new IllegalArgumentException("Char type needs to be 2 bytes long");
        }
        char result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result |= (char) (Byte.toUnsignedInt(bytes[i]) << (8 * i));
        }
        return result;
    }

    public static long bytesToLong(byte[] bytes) {
        if (bytes.length != 8) {
            throw new IllegalArgumentException("Long type needs to be 8 bytes long");
        }
        long result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result |= Byte.toUnsignedLong(bytes[i]) << (8 * i);
        }
        return result;
    }

    public static int bytesToInt(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Int type needs to be 4 bytes long");
        }
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result |= Byte.toUnsignedInt(bytes[i]) << (8 * i);
        }
        return result;
    }
}
