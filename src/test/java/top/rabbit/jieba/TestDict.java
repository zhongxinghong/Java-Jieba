package top.rabbit.jieba;

import top.rabbit.jieba.dataset.Dict;
import top.rabbit.jieba.rank.TFIDF;
import top.rabbit.jieba.struct.Keyword;
import top.rabbit.jieba.tokenizer.POSTokenizer;
import top.rabbit.jieba.tokenizer.Tokenizer;

import java.util.List;

public class TestDict {

    private static final String sentence = "李小福是创新办主任也是云计算方面的专家; 什么是八一双鹿\n" +
            "例如我输入一个带“韩玉赏鉴”的标题，在自定义词库中也增加了此词为N类\n" +
            "「台中」正確應該不會被切開。mac上可分出「石墨烯」；此時又可以分出來凱特琳了。";

    private static final String[] CUSTOM_USERDICT_FILES = new String[] {
            "/dict.custom.part.1.txt",
            "/dict.custom.part.2.txt",
            "/dict.custom.part.3.txt",
    };

    private static final String CUSTOM_IDF_DICT = "/idf.test.txt";


    private static final String[] CUSTOM_STOPWORDS_FILES = new String[] {
            "/stopwords.custom.part.1.txt",
            "/stopwords.custom.part.2.txt",
    };


    public static void test_userDict() {

        Tokenizer dt = new Tokenizer();

        System.out.println(dt.cut(sentence));

        for (String filename : CUSTOM_USERDICT_FILES) {
            dt.loadUserDict(filename);
            System.out.println(dt.cut(sentence));
        }

        System.out.println(dt.getDict());
        System.out.println(dt.getUserDicts());
    } /* Output:
    Load word frequency dict 'dict.std.gz' ... Done! cost 902 ms
    Load model 'prob_emit.wf.gz' ... Done! cost 161 ms
    [李小福, 是, 创新, 办, 主任, 也, 是, 云, 计算, 方面, 的, 专家, ;,  , 什么, 是, 八, 一双, 鹿,
    , 例如, 我, 输入, 一个, 带, “, 韩玉, 赏鉴, ”, 的, 标题, ，, 在, 自定义词, 库中, 也, 增加, 了, 此, 词为, N, 类,
    , 「, 台, 中, 」, 正確, 應該, 不會, 被, 切開, 。, mac, 上, 可, 分出, 「, 石墨, 烯, 」, ；, 此時, 又, 可以, 分出, 來凱, 特琳, 了, 。]
    Load custom word frequency dict 'dict.custom.part.1.txt' ... Done! cost 1 ms
    [李小福, 是, 创新办, 主任, 也, 是, 云计算, 方面, 的, 专家, ;,  , 什么, 是, 八, 一双, 鹿,
    , 例如, 我, 输入, 一个, 带, “, 韩玉, 赏鉴, ”, 的, 标题, ，, 在, 自定义词, 库中, 也, 增加, 了, 此, 词为, N, 类,
    , 「, 台, 中, 」, 正確, 應該, 不會, 被, 切開, 。, mac, 上, 可, 分出, 「, 石墨, 烯, 」, ；, 此時, 又, 可以, 分出, 來凱, 特琳, 了, 。]
    Load custom word frequency dict 'dict.custom.part.2.txt' ... Done! cost 0 ms
    [李小福, 是, 创新办, 主任, 也, 是, 云计算, 方面, 的, 专家, ;,  , 什么, 是, 八一双鹿,
    , 例如, 我, 输入, 一个, 带, “, 韩玉赏鉴, ”, 的, 标题, ，, 在, 自定义词, 库中, 也, 增加, 了, 此, 词为, N, 类,
    , 「, 台, 中, 」, 正確, 應該, 不會, 被, 切開, 。, mac, 上, 可, 分出, 「, 石墨, 烯, 」, ；, 此時, 又, 可以, 分出, 來凱, 特琳, 了, 。]
    Load custom word frequency dict 'dict.custom.part.3.txt' ... Done! cost 1 ms
    [李小福, 是, 创新办, 主任, 也, 是, 云计算, 方面, 的, 专家, ;,  , 什么, 是, 八一双鹿,
    , 例如, 我, 输入, 一个, 带, “, 韩玉赏鉴, ”, 的, 标题, ，, 在, 自定义词, 库中, 也, 增加, 了, 此, 词为, N, 类,
    , 「, 台中, 」, 正確, 應該, 不會, 被, 切開, 。, mac, 上, 可, 分出, 「, 石墨烯, 」, ；, 此時, 又, 可以, 分出, 來, 凱特琳, 了, 。]
    /dict.std.gz
    [/dict.custom.part.1.txt, /dict.custom.part.2.txt, /dict.custom.part.3.txt]
    */

    public static void test_syncUserDict() {
        Tokenizer dt = new Tokenizer();
        POSTokenizer pt = new POSTokenizer(dt);

        System.out.println(pt.cut(sentence));

        for (String filename : CUSTOM_USERDICT_FILES) {
            dt.loadUserDict(filename);
            System.out.println(pt.cut(sentence));
            System.out.println(pt.getUserDicts());
        }
    } /* Output:
    Load word frequency dict 'dict.std.gz' ... Done! cost 374 ms
    Load POS dict 'dict.std.gz' ... Done! cost 805 ms
    Load model 'prob_start.pos.gz' ... Done! cost 6 ms
    Load model 'prob_trans.pos.gz' ... Done! cost 49 ms
    Load model 'prob_emit.pos.gz' ... Done! cost 175 ms
    Load model 'char_state_tab.pos.gz' ... Done! cost 34 ms
    [(李小福, nr), (是, v), (创新, v), (办, v), (主任, b), (也, d), (是, v), (云, n), (计算, v), (方面, n), (的, uj), (专家, n), (;, x), ( , x), (什么, r), (是, v), (八, m), (一双, m), (鹿, nr), (
    , x), (例如, v), (我, r), (输入, v), (一个, m), (带, v), (“, x), (韩玉, nr), (赏鉴, v), (”, x), (的, uj), (标题, n), (，, x), (在, p), (自定义词, n), (库中, nrt), (也, d), (增加, v), (了, ul), (此, r), (词, n), (为, p), (了此词为, x), (N, eng), (类, x), (类, q), (
    , x), (「, x), (台, q), (中, f), (」, x), (正確, ad), (應該, v), (不, d), (會, v), (被, p), (切開, ad), (。, x), (mac, eng), (上可, x), (上, f), (可, v), (分出, v), (「, x), (石墨, n), (烯, x), (」, x), (；, x), (此時, c), (又, d), (可以, c), (分出, v), (來, v), (凱特琳, nrt), (了, ul), (。, x)]
    Load custom word frequency dict 'dict.custom.part.1.txt' ... Done! cost 0 ms
    Load custom POS dict 'dict.custom.part.1.txt' ... Done! cost 1 ms
    [(李小福, nr), (是, v), (创新办, i), (主任, b), (也, d), (是, v), (云计算, x), (方面, n), (的, uj), (专家, n), (;, x), ( , x), (什么, r), (是, v), (八, m), (一双, m), (鹿, nr), (
    , x), (例如, v), (我, r), (输入, v), (一个, m), (带, v), (“, x), (韩玉, nr), (赏鉴, v), (”, x), (的, uj), (标题, n), (，, x), (在, p), (自定义词, n), (库中, nrt), (也, d), (增加, v), (了, ul), (此, r), (词, n), (为, p), (了此词为, x), (N, eng), (类, x), (类, q), (
    , x), (「, x), (台, q), (中, f), (」, x), (正確, ad), (應該, v), (不, d), (會, v), (被, p), (切開, ad), (。, x), (mac, eng), (上可, x), (上, f), (可, v), (分出, v), (「, x), (石墨, n), (烯, x), (」, x), (；, x), (此時, c), (又, d), (可以, c), (分出, v), (來, v), (凱特琳, nrt), (了, ul), (。, x)]
    [/dict.custom.part.1.txt]
    Load custom word frequency dict 'dict.custom.part.2.txt' ... Done! cost 1 ms
    Load custom POS dict 'dict.custom.part.2.txt' ... Done! cost 0 ms
    [(李小福, nr), (是, v), (创新办, i), (主任, b), (也, d), (是, v), (云计算, x), (方面, n), (的, uj), (专家, n), (;, x), ( , x), (什么, r), (是, v), (八一双鹿, nz), (
    , x), (例如, v), (我, r), (输入, v), (一个, m), (带, v), (“, x), (韩玉赏鉴, nz), (”, x), (的, uj), (标题, n), (，, x), (在, p), (自定义词, n), (库中, nrt), (也, d), (增加, v), (了, ul), (此, r), (词, n), (为, p), (了此词为, x), (N, eng), (类, x), (类, q), (
    , x), (「, x), (台, q), (中, f), (」, x), (正確, ad), (應該, v), (不, d), (會, v), (被, p), (切開, ad), (。, x), (mac, eng), (上可, x), (上, f), (可, v), (分出, v), (「, x), (石墨, n), (烯, x), (」, x), (；, x), (此時, c), (又, d), (可以, c), (分出, v), (來, v), (凱特琳, nrt), (了, ul), (。, x)]
    [/dict.custom.part.1.txt, /dict.custom.part.2.txt]
    Load custom word frequency dict 'dict.custom.part.3.txt' ... Done! cost 1 ms
    Load custom POS dict 'dict.custom.part.3.txt' ... Done! cost 1 ms
    [(李小福, nr), (是, v), (创新办, i), (主任, b), (也, d), (是, v), (云计算, x), (方面, n), (的, uj), (专家, n), (;, x), ( , x), (什么, r), (是, v), (八一双鹿, nz), (
    , x), (例如, v), (我, r), (输入, v), (一个, m), (带, v), (“, x), (韩玉赏鉴, nz), (”, x), (的, uj), (标题, n), (，, x), (在, p), (自定义词, n), (库中, nrt), (也, d), (增加, v), (了, ul), (此, r), (词, n), (为, p), (了此词为, x), (N, eng), (类, x), (类, q), (
    , x), (「, x), (台中, s), (」, x), (正確, ad), (應該, v), (不, d), (會, v), (被, p), (切開, ad), (。, x), (mac, eng), (上可, x), (上, f), (可, v), (分出, v), (「, x), (石墨烯, x), (」, x), (；, x), (此時, c), (又, d), (可以, c), (分出, v), (來, zg), (凱特琳, nz), (了, ul), (。, x)]
    [/dict.custom.part.1.txt, /dict.custom.part.2.txt, /dict.custom.part.3.txt]
    */

    public static void test_idfDict() {
        Tokenizer dt = new Tokenizer();
        List<String> segs = dt.cut(sentence);
        TFIDF tfidf;
        List<Keyword> kws;

        tfidf = new TFIDF();
        kws = tfidf.extract(segs, 10);
        System.out.println(kws);

        tfidf = new TFIDF(CUSTOM_IDF_DICT);
        kws = tfidf.extract(segs, 10);
        System.out.println(kws);
    } /* Output:
    Load word frequency dict 'dict.std.gz' ... Done! cost 702 ms
    Load model 'prob_emit.wf.gz' ... Done! cost 202 ms
    Load stop words from 'stopwords.default.txt' ... Done! cost 1 ms
    Load IDF dict 'idf.std.gz' ... Done! cost 479 ms
    [分出, 應該, mac, 韩玉, 此時, 特琳, 词为, 不會, 李小福, 來凱]
    Load stop words from 'stopwords.default.txt' ... Done! cost 1 ms
    Load IDF dict 'idf.test.txt' ... Done! cost 1 ms
    [來凱, 李小福, 不會, 词为, 什么, 方面, 一双, 计算, 增加, 专家]
    */

    public static void test_idfStopWords() {
        Tokenizer dt = new Tokenizer();
        List<String> segs = dt.cut(sentence);
        TFIDF tfidf = new TFIDF();
        List<Keyword> kws;

        kws = tfidf.extract(segs, 10);
        System.out.println(kws);

        for (String filename : CUSTOM_STOPWORDS_FILES) {
            tfidf.loadStopWords(filename);
            kws = tfidf.extract(segs, 10);
            System.out.println(kws);
        }
    } /* Output:
    Load word frequency dict 'dict.std.gz' ... Done! cost 398 ms
    Load stop words from 'stopwords.default.txt' ... Done! cost 1 ms
    Load IDF dict 'idf.std.gz' ... Done! cost 823 ms
    [分出, 應該, mac, 韩玉, 此時, 特琳, 词为, 不會, 李小福, 來凱]
    Load stop words from 'stopwords.custom.part.1.txt' ... Done! cost 1 ms
    [應該, mac, 韩玉, 此時, 特琳, 词为, 不會, 李小福, 來凱, 自定义词]
    Load stop words from 'stopwords.custom.part.2.txt' ... Done! cost 1 ms
    [應該, 此時, 特琳, 词为, 不會, 李小福, 來凱, 自定义词, 切開, 库中]
    */

    public static void test_NONE_DICT() {
        Tokenizer dt = new Tokenizer(Dict.NONE_DICT);
        dt.loadUserDict(Dict.STD_WORD_DICT_GZ);
        List<String>segs = dt.cut(IOUtils.readFrom(IOUtils.TEST_FILE_1));
        TFIDF tfidf = new TFIDF();
        System.out.println(tfidf.extract(segs));
    }
}
