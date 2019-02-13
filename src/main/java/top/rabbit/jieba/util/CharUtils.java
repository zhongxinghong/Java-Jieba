package top.rabbit.jieba.util;

public final class CharUtils {

    private CharUtils() {};

    private static final char[] SPECIAL_SYMBOLS = "+#&\\._%".toCharArray();

    public static final boolean isChinese(char ch) {
        if ('\u4E00' <= ch && ch <= '\u9FD5')
            return true;
        return false;
    }

    public static final boolean isChinese(String s) {
        if (s.length() == 0)
            return false;
        for (char ch : s.toCharArray()) {
            if (!isChinese(ch))
                return false;
        }
        return true;
    }

    public static final boolean isEnglish(char ch) {
        if ( ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || Character.isDigit(ch))
            return true;
        return false;
    }

    public static final boolean isEnglish(String s) {
        if (s.length() == 0)
            return false;
        for (char ch : s.toCharArray()) {
            if (!isEnglish(ch))
                return false;
        }
        return true;
    }

    public static final boolean isDigit(char ch) {
        return Character.isDigit(ch);
    }

    public static final boolean isDigit(String s) {
        if (s.length() == 0)
            return false;
        for (char ch : s.toCharArray()) {
            if (!Character.isDigit(ch))
                return false;
        }
        return true;
    }

    public static final boolean isNumber(char ch) {
        return ( Character.isDigit(ch) || ch == '.' );
    }

    public static final boolean isNumber(String s) {
        if (s.length() == 0)
            return false;
        for (char ch : s.toCharArray()) {
            if (!isNumber(ch)) {
                return false;
            }
        }
        return true;
    }

    public static final boolean isSpace(char ch) {
        return Character.isSpaceChar(ch);
    }

    public static final boolean isSpace(String s) {
        if (s.length() == 0)
            return true;
        for (char ch : s.toCharArray()) {
            if (!Character.isSpaceChar(ch))
                return false;
        }
        return true;
    }

    public static final boolean isSpecialSymbol(char ch) {
        for (char c : SPECIAL_SYMBOLS) {
            if (ch == c)
                return true;
        }
        return false;
    }

    public static final boolean isSpecialSymbol(String s) {
        if (s.length() == 0)
            return false;
        for (char ch : s.toCharArray()) {
            if (!isSpecialSymbol(ch))
                return false;
        }
        return true;
    }

    public static final boolean isNotSpace(char ch) {
        if ( isChinese(ch) || isEnglish(ch) || isSpecialSymbol(ch) )
            return true;
        return false;
    }

    public static final boolean isNotSpace(String s) {
        if (s.length() == 0)
            return false;
        for (char ch : s.toCharArray()) {
            if (!isNotSpace(ch))
                return false;
        }
        return true;
    }
}
