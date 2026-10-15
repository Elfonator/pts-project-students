package sk.testlab.support;

import java.util.regex.Pattern;

/** Len formátovanie a validácia identity. Žiadne opätovné počítanie ceny aplikácie. */
public final class TextChecks {
    private static final Pattern UUID = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    private TextChecks() { }

    public static String normalizeSpaces(String text) {
        // Intl.NumberFormat môže pred € použiť NBSP U+00A0 alebo NNBSP U+202F.
        // Zachovávame desatinnú čiarku, číslice aj menu; ignorujeme len druh/počet medzier.
        return text == null ? "" : text.replaceAll("[\\s\\p{Zs}]+", " ").trim();
    }

    public static boolean isCanonicalUuid(String text) {
        return text != null && UUID.matcher(text).matches();
    }
}