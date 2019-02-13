package top.rabbit.jieba.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexUtils {

    private RegexUtils() {};
    private static final int reU = Pattern.UNICODE_CASE;

    public static final Pattern reUserDict = Pattern.compile("^(?:\ufeff+)?(.+?)( [0-9]+)?( [a-z]+)?$", reU);
    public static final Pattern reSkip_viterbi = Pattern.compile("([a-zA-Z0-9]+(?:\\.\\d+)?%?)");
    public static final Pattern reSkip_cut = Pattern.compile("(\\r\\n|\\s)", reU);
    public static final Pattern reSkip_cutAll = Pattern.compile("[^a-zA-Z0-9+#\\n]", reU);
    public static final Pattern reSkip_detail = Pattern.compile("([\\.0-9]+|[a-zA-Z0-9]+)");
    public static final Pattern reSkip_internal = Pattern.compile("(\\r\\n|\\s)");
    public static final Pattern reHan_viterbi = Pattern.compile("([\\u4E00-\\u9FD5]+)");
    public static final Pattern reHan_cut = Pattern.compile("([\\u4E00-\\u9FD5a-zA-Z0-9+#&\\._%]+)", reU);
    public static final Pattern reHan_cutAll = Pattern.compile("([\\u4E00-\\u9FD5]+)", reU);
    public static final Pattern reHan_detail = Pattern.compile("([\\u4E00-\\u9FD5]+)");
    public static final Pattern reHan_internal = Pattern.compile("([\\u4E00-\\u9FD5a-zA-Z0-9+#&\\._]+)");
    public static final Pattern reNum = Pattern.compile("[\\.0-9]+");
    public static final Pattern reEng = Pattern.compile("[a-zA-Z0-9]+");
    public static final Pattern reEngChar = Pattern.compile("^[a-zA-Z0-9]$");

    public static List<String> split(Pattern p, String s) {
        List<String> segs = new ArrayList<String>();
        Matcher mat = p.matcher(s);
        int offset = 0;
        while (mat.find()) {
            if (mat.start() > offset) { // 本应该用 >= ,但此处考虑到 s.substring(0,0) 没有利用价值
                                        // 这使得输出的空字符串数量比原 Jieba 要少
                segs.add(s.substring(offset, mat.start()));
            }
            /*-------------------------------------------------------------------*
             *  In [86]: re.split?
             *  Signature: re.split(pattern, string, maxsplit=0, flags=0)
             *  Docstring:
             *  Split the source string by the occurrences of the pattern,
             *  returning a list containing the resulting substrings.  If
             *  capturing parentheses are used in pattern, then the text of all
             *  groups in the pattern are also returned as part of the resulting
             *  list.  If maxsplit is nonzero, at most maxsplit splits occur,
             *  and the remainder of the string is returned as the final element
             *  of the list.
             *  File:      /usr/lib/python3.6/re.py
             *  Type:      function
             *-------------------------------------------------------------------*/
            if (mat.groupCount() > 0) { // 如果有分组，则将分组加入
                segs.add(mat.group());
            }
            offset = mat.end();
        }
        if (offset < s.length())
            segs.add(s.substring(offset));
        return segs;
    }
}
