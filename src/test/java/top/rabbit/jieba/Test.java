package top.rabbit.jieba;

import top.rabbit.jieba.tokenizer.POSTokenizer;
import top.rabbit.jieba.tokenizer.Tokenizer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Test {

    private static final String s1 = "re.M\n" +
            "re.MULTILINE\n" +
            "当指定时，模式字符' ^'匹配字符串的开头以及每个行的开头（紧接每个换行符）； 模式字符'$'匹配字符串的末尾以及每一行的结尾（紧靠每个换行符之前）。默认情况下， '^'只匹配字符串的开始，'$'只匹配字符串的末尾和字符串末尾换行符（如果有的话）之前的位置。\n" +
            "\n" +
            "re.S\n" +
            "re.DOTALL\n" +
            "使'.'特殊字符匹配任何字符，包括换行 ；如果没有此标志， '.'将匹配任何内容除换行符。";

    private static final String s2 = "re.search(pattern, string, flags=0)\n" +
            "扫描字符串，寻找的第一个由该正则表达式模式产生匹配的位置，并返回相应的MatchObject实例。返回None如果没有字符串中的位置匹配模式 ；请注意这不同于在字符串的某个位置中找到一个长度为零的匹配。\n" +
            "\n" +
            "re.match(pattern, string, flags=0)\n" +
            "　　　如果在字符串的开头的零个或更多字符匹配正则表达式模式，将返回相应的MatchObject实例。返回None则该字符串中与模式不匹配；请注意这是不同于零长度匹配。\n" +
            "\n" +
            "　　　请注意，即使在多行模式下， re.match()将只匹配字符串的开头，而不是在每个行的开头。\n" +
            "\n" +
            "　　　如果你想要在字符串中的任意位置定位一个匹配，改用search () （请参见search () 与 match ()）。\n" +
            "\n" +
            "re.fullmatch(pattern, string, flags=0)\n" +
            "\n" +
            "如果整个字符串匹配正则表达式模式，则返回一个match对象。如果字符串与模式不匹配，则返回None；请注意：这与长度为0的match是有区别的。\n" +
            "\n" +
            "新版本3.4\n" +
            "\n" +
            "re.split(pattern, string, maxsplit=0, flags=0)\n" +
            "将字符串拆分的模式的匹配项。如果在模式中使用捕获括号，则然后也作为结果列表的一部分返回的文本模式中的所有组。如果maxsplit不为零，顶多maxsplit分裂发生，并且该字符串的其余部分将作为列表的最后一个元素返回。（不兼容性说明： 在原始的 Python 1.5 版本中， maxsplit被忽略。这已被固定在以后的版本。）";


    public static void testDocsExample() {
        TestDocsExample.test_1_cuts();
        TestDocsExample.test_2_load_dict();
        TestDocsExample.test_2_adjust_dict();
        TestDocsExample.test_3_keyword_extract_TFIDF();
    }

    public static void testCut() {
        Tokenizer dt = new Tokenizer();
        TestCut.test_cut(dt, IOUtils.TEST_FILE_1);
        TestCut.test_cutForSearch(dt, IOUtils.TEST_FILE_1);
        TestCut.test_cutForSearch_noHMM(dt, IOUtils.TEST_FILE_1);
        TestCut.test_cutAll(dt, IOUtils.TEST_FILE_1);
        TestCut.test_cut_noHMM(dt, IOUtils.TEST_FILE_1);
        //TestCut.test_cutWithIndex(dt, IOUtils.TEST_FILE_1);
        //TestCut.test_cutWithIndex_noHMM(dt, IOUtils.TEST_FILE_1);
        //TestCut.test_cutForSearchWithIndex(dt, IOUtils.TEST_FILE_1);
    }

    public static void testMultiCut() {
        Tokenizer dt = new Tokenizer();
        //TestMultiCut.multi_test_cut(dt, IOUtils.TEST_FILE_1, 200);
        //TestMultiCut.multi_test_cutForSearch(dt, IOUtils.TEST_FILE_1, 200);
        //TestMultiCut.multi_test_cut_noHMM(dt, IOUtils.TEST_FILE_1, 200);
        //TestMultiCut.multi_test_cutAll(dt, IOUtils.TEST_FILE_1, 200);
        //TestMultiCut.multi_test_cutForSearch_noHMM(dt, IOUtils.TEST_FILE_1, 200);
    }

    public static void  testPOSTokenizer() {
        POSTokenizer pt = new POSTokenizer();
        //TestPOSTokenizer.test_cut(pt);
        //TestPOSTokenizer.test_cut_noHMM(pt);
        TestPOSTokenizer.test_cut(pt, IOUtils.TEST_FILE_1);
        //TestPOSTokenizer.test_cut_noHMM(pt, IOUtils.TEST_FILE_1);
    }

    public static void testRank() {
        TestRank.test_TFIDF(IOUtils.TEST_FILE_1);
        //TestRank.test_TextRank(IOUtils.TEST_FILE_1);
    }

    public static void testDict() {
        TestDict.test_userDict();
        TestDict.test_syncUserDict();
        //TestDict.test_idfDict();
        //TestDict.test_idfStopWords();
    }


    private static void test1() {
        //String s = "hasd我lk，sadfll、、那不kei就里卡尔。‘a adsf、我\n\n你 alfjo9 0+";
        Tokenizer dt = new Tokenizer();
        List<String> segs = dt.cutAll(s2);
        System.out.println(segs);
    }

    private static void test2() throws Exception {
        LinkedList<Integer> q = new LinkedList<Integer>();
        for (int i = 0; i < 10; ++i) {
            q.push(i);
        }
        int e;
        for (int i = 0; i < 10; ++i) {
            e = q.pollLast();
            System.out.println(e);
            System.out.println(q);
            q.push(e);
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public static void main(String[] args) throws Exception {
        //test2();
        //testCut();
        //TestRank.test_TFIDF(IOUtils.TEST_FILE_1);
        //testPOSTokenizer();
        //testRank();
        //testMultiCut();
        //TestAsync.asnyc_cut();
        testDict();
    }

}