package top.rabbit.jieba;

import top.rabbit.jieba.rank.KeywordExtractor;
import top.rabbit.jieba.rank.TFIDF;
import top.rabbit.jieba.rank.TextRank;
import top.rabbit.jieba.struct.Keyword;
import top.rabbit.jieba.struct.TaggedWord;
import top.rabbit.jieba.tokenizer.POSTokenizer;
import top.rabbit.jieba.tokenizer.Tokenizer;

import java.util.List;

public class TestRank {

    public static void printKeyword(Keyword kw) {
        if (kw.flag == null) {
            System.out.println(String.format("rank: %02d, score: %f, %s",
                    kw.rank, kw.score, kw.word));
        } else {
            System.out.println(String.format("rank: %02d, score: %f, (%s, %s)",
                    kw.rank, kw.score, kw.word, kw.flag));
        }
    }

    public static void printKeywords(List<Keyword> kws) {
        for (Keyword kw : kws) {
            printKeyword(kw);
        }
    }

    public static void test_TFIDF(String filename) {
        Tokenizer dt = new Tokenizer();
        TFIDF tfidf = new TFIDF(KeywordExtractor.EXTENDED_STOP_WORDS_TXT);
        List<String> segs = dt.cut(IOUtils.readFrom(filename));
        List<Keyword> kws = tfidf.extract(segs, 50);
        System.out.println(kws);
        printKeywords(kws);
    } /* Output:
    rank: 01, score: 0.216383, 鸿渐
    rank: 02, score: 0.123853, 辛楣
    rank: 03, score: 0.068592, 小姐
    rank: 04, score: 0.062132, 孙小姐
    rank: 05, score: 0.054676, 柔嘉
    rank: 06, score: 0.049626, 方鸿渐
    rank: 07, score: 0.033501, 自己
    rank: 08, score: 0.028715, 李梅亭
    rank: 09, score: 0.028197, 唐小姐
    rank: 10, score: 0.028064, 没有
    rank: 11, score: 0.023341, 知道
    rank: 12, score: 0.021301, 什么
    rank: 13, score: 0.021241, 高松年
    rank: 14, score: 0.020962, 先生
    rank: 15, score: 0.020325, 太太
    rank: 16, score: 0.019338, 汪太太
    rank: 17, score: 0.019230, 女人
    rank: 18, score: 0.019107, 可是
    rank: 19, score: 0.017729, 他们
    rank: 20, score: 0.017701, 赵辛楣
    rank: 21, score: 0.017446, 学生
    rank: 22, score: 0.016344, 今天
    rank: 23, score: 0.014504, 李先生
    rank: 24, score: 0.014416, 一个
    rank: 25, score: 0.013854, 你们
    rank: 26, score: 0.013175, 结婚
    rank: 27, score: 0.012912, 时候
    rank: 28, score: 0.012883, 明天
    rank: 29, score: 0.012722, 东西
    rank: 30, score: 0.011774, 咱们
    rank: 31, score: 0.011725, 这样
    rank: 32, score: 0.011526, 仿佛
    rank: 33, score: 0.011448, 现在
    rank: 34, score: 0.011381, 大家
    rank: 35, score: 0.011285, 不是
    rank: 36, score: 0.011074, 也许
    rank: 37, score: 0.010669, 不会
    rank: 38, score: 0.010384, 不要
    rank: 39, score: 0.010328, 觉得
    rank: 40, score: 0.010322, 可以
    rank: 41, score: 0.010266, 我们
    rank: 42, score: 0.010222, 人家
    rank: 43, score: 0.009985, 忽然
    rank: 44, score: 0.009929, 家里
    rank: 45, score: 0.009393, 为什么
    rank: 46, score: 0.009347, 的话
    rank: 47, score: 0.009177, 学校
    rank: 48, score: 0.009087, 老太太
    rank: 49, score: 0.009069, 这时候
    rank: 50, score: 0.009051, 所以
    */

    public static void test_TextRank(String filename) {
        POSTokenizer pt = new POSTokenizer();
        TextRank textrank = new TextRank();
        /*String sentence = "此外，公司拟对全资子公司吉林欧亚置业有限公司增资4.3亿元，增资后，" +
                "吉林欧亚置业注册资本由7000万元增加到5亿元。吉林欧亚置业主要经营范围为房地产" +
                "开发及百货零售等业务。目前在建吉林欧亚城市商业综合体项目。2013年，实现营业" +
                "收入0万元，实现净利润-139.13万元。";*/
        List<TaggedWord> segs = pt.cut(IOUtils.readFrom(filename));
        List<Keyword> kws = textrank.extract(segs, 50);
        System.out.println(kws);
        printKeywords(kws);
    } /* Output:
    rank: 01, score: 1.000000, (没有, v)
    rank: 02, score: 0.564273, (知道, v)
    rank: 03, score: 0.450865, (女人, n)
    rank: 04, score: 0.435597, (学生, n)
    rank: 05, score: 0.376396, (时候, n)
    rank: 06, score: 0.346339, (先生, n)
    rank: 07, score: 0.321423, (太太, n)
    rank: 08, score: 0.306327, (中国, ns)
    rank: 09, score: 0.305356, (大家, n)
    rank: 10, score: 0.301393, (不会, v)
    rank: 11, score: 0.295127, (东西, ns)
    rank: 12, score: 0.260514, (觉得, v)
    rank: 13, score: 0.228799, (学校, n)
    rank: 14, score: 0.221306, (人家, n)
    rank: 15, score: 0.221238, (出来, v)
    rank: 16, score: 0.207631, (起来, v)
    rank: 17, score: 0.204145, (结婚, v)
    rank: 18, score: 0.194029, (不能, v)
    rank: 19, score: 0.182624, (看见, v)
    rank: 20, score: 0.181950, (上海, ns)
    rank: 21, score: 0.181699, (大学, n)
    rank: 22, score: 0.178714, (外国, ns)
    rank: 23, score: 0.168501, (眼睛, n)
    rank: 24, score: 0.158097, (父亲, n)
    rank: 25, score: 0.157427, (儿子, n)
    rank: 26, score: 0.157272, (好像, v)
    rank: 27, score: 0.154295, (不肯, v)
    rank: 28, score: 0.144794, (还有, v)
    rank: 29, score: 0.142715, (汽车, n)
    rank: 30, score: 0.142107, (说话, v)
    rank: 31, score: 0.138975, (丈夫, n)
    rank: 32, score: 0.138296, (朋友, n)
    rank: 33, score: 0.136615, (孩子, n)
    rank: 34, score: 0.136091, (地方, n)
    rank: 35, score: 0.134077, (回来, v)
    rank: 36, score: 0.133259, (吃饭, v)
    rank: 37, score: 0.126143, (衣服, n)
    rank: 38, score: 0.124656, (愿意, v)
    rank: 39, score: 0.122321, (校长, n)
    rank: 40, score: 0.117343, (男人, n)
    rank: 41, score: 0.114831, (表示, v)
    rank: 42, score: 0.113083, (告诉, v)
    rank: 43, score: 0.105929, (听见, v)
    rank: 44, score: 0.105703, (银行, n)
    rank: 45, score: 0.105275, (研究, vn)
    rank: 46, score: 0.103357, (声音, n)
    rank: 47, score: 0.103142, (好比, v)
    rank: 48, score: 0.102417, (回家, n)
    rank: 49, score: 0.102038, (身体, n)
    rank: 50, score: 0.100934, (希望, v)
    */
}
