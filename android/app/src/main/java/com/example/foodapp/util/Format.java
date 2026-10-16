package com.example.foodapp.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class Format {
    private static final DecimalFormat MONEY;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        MONEY = new DecimalFormat("#,###", symbols);
    }

    private Format() {}

    /** 79000 -> "79.000đ" */
    public static String money(long amount) {
        return MONEY.format(amount) + "đ";
    }

    public static String rating(double rating) {
        return String.format(Locale.US, "%.1f", rating);
    }

    /** "2026-10-05T06:54:23.273Z" -> "13:54 05/10/2026" (giờ máy) */
    public static String dateTime(String iso) {
        if (iso == null) return "";
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = parser.parse(iso);
            return new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return iso;
        }
    }
}
