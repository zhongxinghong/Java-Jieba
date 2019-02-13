package top.rabbit.jieba;

import top.rabbit.jieba.rank.KeywordExtractor;
import top.rabbit.jieba.rank.TFIDF;
import top.rabbit.jieba.tokenizer.Tokenizer;
import top.rabbit.jieba.struct.Keyword;

import java.util.List;

public class TestDocsExample {

    public static void test_1_cuts() {
        Tokenizer dt = new Tokenizer();
        List<String> segs;

        segs = dt.cutAll("我来到北京清华大学"); // 全模式
        System.out.println(segs);

        segs = dt.cut("我来到北京清华大学"); // 精确模式
        System.out.println(segs);

        segs = dt.cut("他来到了网易杭研大厦"); // 默认是精确模式
        System.out.println(segs);

        segs = dt.cutForSearch("小明硕士毕业于中国科学院计算所，后在日本京都大学深造"); // 搜索引擎模式
        System.out.println(segs);

    } /* Output:
    [我, 来到, 北京, 清华, 清华大学, 华大, 大学]
    [我, 来到, 北京, 清华大学]
    Load model 'prob_emit.gz' ... Done! cost 275 ms
    [他, 来到, 了, 网易, 杭研, 大厦]
    [小明, 硕士, 毕业, 于, 中国科学院, 中国, 科学, 学院, 科学院, 计算所, 计算, ，, 后, 在, 日本京都大学, 日本, 京都, 大学, 深造]
    *///:~

    public static void test_2_load_dict() {
        Tokenizer dt = new Tokenizer();
        String USER_CUSTOM_DICT = "/dict.custom.txt";
            // A dict using UTF-8 encoding with BOM header and redundant blank lines
        List<String> segs;
        String sentence = "李小福是创新办主任也是云计算方面的专家; 什么是八一双鹿\n" +
                "例如我输入一个带“韩玉赏鉴”的标题，在自定义词库中也增加了此词为N类\n" +
                "「台中」正確應該不會被切開。mac上可分出「石墨烯」；此時又可以分出來凱特琳了。";

        segs = dt.cut(sentence);
        System.out.println(segs);

        dt.loadUserDict(USER_CUSTOM_DICT);
        ////////////////////////////////
        // Content of dict.custom.txt //
        ////////////////////////////////
        /*dt.addWord("云计算", 5);
        dt.addWord("李小福", 3);
        dt.addWord("创新办", 3);
        dt.addWord("easy_install", 3);
        dt.addWord("好用", 300);
        dt.addWord("韩玉赏鉴", 3);
        dt.addWord("八一双鹿", 3);
        dt.addWord("台中");
        dt.addWord("凱特琳");
        dt.addWord("Edu Trust认证", 2000);*/
        dt.addWord("石墨烯");
        dt.delWord("自定义词");

        segs = dt.cut(sentence);
        System.out.println(segs);

    } /* Output:
    Load word dict 'dict.std.txt' ... Done! cost 626 ms
    Load model 'prob_emit.gz' ... Done! cost 278 ms
    [李小福, 是, 创新, 办, 主任, 也, 是, 云, 计算, 方面, 的, 专家, ;,  , 什么, 是, 八, 一双, 鹿,
    , 例如, 我, 输入, 一个, 带, “, 韩玉, 赏鉴, ”, 的, 标题, ，, 在, 自定义词, 库中, 也, 增加, 了, 此, 词为, N, 类,
    , 「, 台, 中, 」, 正確, 應該, 不會, 被, 切開, 。, mac, 上, 可, 分出, 「, 石墨, 烯, 」, ；, 此時, 又, 可以, 分出, 來凱, 特琳, 了, 。]
    Load word dict 'dict.custom.txt' ... Done! cost 2 ms
    [李小福, 是, 创新办, 主任, 也, 是, 云计算, 方面, 的, 专家, ;,  , 什么, 是, 八一双鹿,
    , 例如, 我, 输入, 一个, 带, “, 韩玉赏鉴, ”, 的, 标题, ，, 在, 自定义, 词库, 中, 也, 增加, 了, 此, 词为, N, 类,
    , 「, 台中, 」, 正確, 應該, 不會, 被, 切開, 。, mac, 上, 可, 分出, 「, 石墨烯, 」, ；, 此時, 又, 可以, 分出, 來, 凱特琳, 了, 。]
    *///:~

    public static void test_2_adjust_dict() {
        Tokenizer dt = new Tokenizer();
        List<String> segs;
        int freq;

        segs = dt.cut_noHMM("如果放到post中将出错。");
        System.out.println(segs);

        freq = dt.suggestFreqForSplit("中", "将");
        System.out.print(freq + " ");
        freq = dt.forcedSplit("中", "将");
        System.out.println(freq);

        segs = dt.cut_noHMM("如果放到post中将出错。");
        System.out.println(segs);


        segs = dt.cut_noHMM("「台中」正确应该不会被切开");
        System.out.println(segs);

        freq = dt.suggestFreqForJoin("台中");
        System.out.print(freq + " ");
        freq = dt.forcedJoin("台中");
        System.out.println(freq);

        segs = dt.cut_noHMM("「台中」正确应该不会被切开");
        System.out.println(segs);

    } /* Output:
    Load word dict 'dict.std.txt' ... Done! cost 641 ms
    [如果, 放到, post, 中将, 出错, 。]
    494
    494
    [如果, 放到, post, 中, 将, 出错, 。]
    [「, 台, 中, 」, 正确, 应该, 不会, 被, 切开]
    69
    69
    [「, 台中, 」, 正确, 应该, 不会, 被, 切开]
    *///:~

    public static void test_3_keyword_extract_TFIDF() { /* 默认使用自定义的停词表 */
        Tokenizer dt = new Tokenizer();
        KeywordExtractor tfidf = new TFIDF();
        String sentence = "此外，公司拟对全资子公司吉林欧亚置业有限公司增资4.3亿元，增资后，" +
                "吉林欧亚置业注册资本由7000万元增加到5亿元。吉林欧亚置业主要经营范围为房地产" +
                "开发及百货零售等业务。目前在建吉林欧亚城市商业综合体项目。2013年，实现营业" +
                "收入0万元，实现净利润-139.13万元。";

        List<String> segs = dt.cut(sentence);
        List<Keyword> kws = tfidf.extract(segs);

        System.out.println(kws);
        for (Keyword kw : kws) {
            System.out.println(String.format("rank: %02d, score: %f, %s",
                    kw.rank, kw.score, kw.word));
        }

    } /* Output:
    Load word dict 'dict.std.txt' ... Done! cost 606 ms
    Load model 'prob_emit.gz' ... Done! cost 282 ms
    Load IDF dict 'idf.std.txt' ... Done! cost 563 ms
    [欧亚, 吉林, 置业, 万元, 增资, 7000, 139.13, 2013, 4.3, 综合体, 经营范围, 亿元, 在建, 全资, 注册资本, 百货, 零售, 子公司, 营业, 净利润]
    rank: 01, score: 0.816921, 欧亚
    rank: 02, score: 0.737495, 吉林
    rank: 03, score: 0.546894, 置业
    rank: 04, score: 0.379662, 万元
    rank: 05, score: 0.375803, 增资
    rank: 06, score: 0.284637, 7000
    rank: 07, score: 0.284637, 139.13
    rank: 08, score: 0.284637, 2013
    rank: 09, score: 0.284637, 4.3
    rank: 10, score: 0.217994, 综合体
    rank: 11, score: 0.216981, 经营范围
    rank: 12, score: 0.214233, 亿元
    rank: 13, score: 0.196302, 在建
    rank: 14, score: 0.192254, 全资
    rank: 15, score: 0.191630, 注册资本
    rank: 16, score: 0.187267, 百货
    rank: 17, score: 0.165066, 零售
    rank: 18, score: 0.163337, 子公司
    rank: 19, score: 0.155773, 营业
    rank: 20, score: 0.142569, 净利润
    *///:~
}
