package xyz.realplussmp.bounty.util;

import java.text.DecimalFormat;

public class NumberUtil {

    private static final DecimalFormat df = new DecimalFormat("#.#");

    public static String format(double value) {
        if (value >= 1_000_000_000) {
            return df.format(value / 1_000_000_000D) + "B";
        }
        if (value >= 1_000_000) {
            return df.format(value / 1_000_000D) + "M";
        }
        if (value >= 1_000) {
            return df.format(value / 1_000D) + "K";
        }
        return df.format(value);
    }
}