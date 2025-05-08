package com.rfmajor.scrabblesolver.common.gaddag.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LongBitEntry {
    private final long value;
    private final BigDecimal valueLog2;

    public LongBitEntry(long value) {
        this.value = value;
        this.valueLog2 = calculateLog2(value);
    }

    @Override
    public String toString() {
        return String.format("%d ~ 2^%s", value, valueLog2);
    }

    public static LongBitEntry of(long value) {
        return new LongBitEntry(value);
    }

    private static BigDecimal calculateLog2(long value) {
        double log2 = Math.log(value) / Math.log(2);
        return BigDecimal.valueOf(log2).setScale(2, RoundingMode.CEILING);
    }
}
