package com.example.foodapp.ui.chat;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Hiển thị markdown đơn giản mà AI hay dùng: **in đậm**, `code`, gạch đầu dòng "- " / "* ". */
final class ChatText {
    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");

    private ChatText() {}

    static CharSequence format(String raw) {
        String text = raw.replace("`", "")
                .replaceAll("(?m)^\\s*[-*•]\\s+", "• ")
                .replaceAll("(?m)^#{1,6}\\s*", "")
                .trim();

        SpannableStringBuilder out = new SpannableStringBuilder();
        Matcher m = BOLD.matcher(text);
        int last = 0;
        while (m.find()) {
            out.append(text, last, m.start());
            int start = out.length();
            out.append(m.group(1));
            out.setSpan(new StyleSpan(Typeface.BOLD), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            last = m.end();
        }
        out.append(text.substring(last));
        return out;
    }
}
