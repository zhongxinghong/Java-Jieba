# Java-Jieba
Jieba 0.39 的 Java 复刻版，支持原版 Jieba 的所有核心功能


## 写在前面
这是我为了入门 Java 而尝试构建的第一个工程，目的是为了熟悉 Java 的一些基本语法和常用类库，并学习一些简单的 Java 类设计。之所以选择 Jieba ，是因为它曾经帮助我完成过一些项目，非常有用，我很喜欢！


## 鸣谢

- Python 原版 Jieba [@fxsjy/jieba][fxsjy/jieba]
- 老 Java 版 Jieba [@huaban/jieba-analysis][huaban/jieba-analysis]


## 特点
Java-Jieba 支持原版 Jieba 的所有核心功能：

- 中文分词的所有模式： 全模式、精确模式、搜索引擎模式、索引模式
- 关键词提取的所有算法： TF-IDF、TextRank
- 用户自定义词典、IDF 表、停词表


## 一致性说明
本项目是对 Jieba 0.39 的源码翻译，核心功能完全一致，吻合度几乎为 **100%** ，具体见[测试结果](#一致性测试)。


## 安装说明
本项目利用 [maven][maven] 进行构建，至少需要 **JDK 1.8** 。


## 项目结构
```console
debian-9:~/IdeaProjects/java-jieba/src/main/java/top/rabbit/jieba# tree
.
├── dataset
│   ├── Dict.java
│   ├── POSDict.java
│   └── WordFrequencyDict.java
├── rank
│   ├── KeywordExtractor.java
│   ├── TFIDF.java
│   ├── TextRank.java
│   └── UndirectWeightedGraph.java
├── struct
│   ├── Keyword.java
│   ├── LocatedWord.java
│   ├── Pair.java
│   └── TaggedWord.java
├── tokenizer
│   ├── POSTokenizer.java
│   └── Tokenizer.java
├── util
│   ├── CharUtils.java
│   └── RegexUtils.java
└── viterbi
    ├── POSViterbi.java
    └── WordFrequencyViterbi.java

6 directories, 17 files
```

## 主要功能

下面基于原 Jieba [文档][fxsjy/jieba] (一个[备份][doc-backup])，对本项目的核心类及其方法进行介绍。


### 中文分词

`Tokenizer` 是实现中文分词的核心分词器类，详见 [Tokenizer][Tokenizer] 类。

`Tokenizer` 中与分词相关的方法如下。无 `HMM` 参数的同名方法使用默认值 `HMM = true` 。以 `WithIndex` 结尾的方法对应于原 Jieba 中 `tokenize` 函数，实现索引模式。`LocatedWord` 对应于 `tuple(word, start, end)`，详见 [LocatedWord][LocatedWord] 类。
```java
public List<String> cut(String sentence, boolean HMM);
public List<String> cut(String sentence);
public List<String> cut_noHMM(String sentence);
public List<String> cutAll(String sentence);
public List<String> cutForSearch(String sentence, boolean HMM);
public List<String> cutForSearch(String sentence);
public List<String> cutForSearch_noHMM(String sentence);
public List<LocatedWord> cutWithIndex(String sentence, boolean HMM);
public List<LocatedWord> cutWithIndex(String sentence);
public List<LocatedWord> cutWithIndex_noHMM(String sentence);
public List<LocatedWord> cutForSearchWithIndex(String sentence, boolean HMM);
public List<LocatedWord> cutForSearchWithIndex(String sentence);
public List<LocatedWord> cutForSearchWithIndex_noHMM(String sentence);
```

相关测试见 [TestCut][TestCut], [TestMultiCut][TestMultiCut] 。

#### 差别：

- 原 Jieba 在 “全模式” 下并没有使用到 HMM 模型，因此 `cutAll` 方法不具有 HMM 参数选项。
- 删除了原 Jieba 在 cut 模式下的一个多余的小分支，详见 `cut` 函数定义。
- 原 Jieba 的正则表达式实现的效果比较简单，因此该项目对所有核心算法中的 `re.match` 函数统一用传统的字符串处理函数进行替代，详见 [CharUtils][CharUtils] 类。经测试，效果完全一致。
- Java 的 Regex 模板对 `re.split` 方法的实现与 Python 的 re 模块并不一致，因此我写了一个几乎等效的 `split` 方法，详见 [RegexUtils][RegexUtils] 类。经测试，效果完全一致。


下面给出原 Jieba 文档中案例的实现，详见 [TestDocsExample][TestDocsExample]：
```java
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
[他, 来到, 了, 网易, 杭研, 大厦]
[小明, 硕士, 毕业, 于, 中国科学院, 中国, 科学, 学院, 科学院, 计算所, 计算, ，, 后, 在, 日本京都大学, 日本, 京都, 大学, 深造]
*/
```


### 词频调整

`Tokenizer` 中与词频调整相关的方法如下。原 Jieba 中的 `suggest_freq` 函数被细分为 `suggestFreqForJoin`, `suggestFreqForSplit`, `forcedJoin`, `forcedSplit` 四个方法。两个 `suggestFreq*` 函数可以给出推荐词频，两个 `forced*` 方法相当于 `addWord` 与 `suggestFreq` 方法的结合，使用推荐词频实现词语的强制结合和分割。对于 `addWord` 方法，如果传入小于 0 的词频，那么将自动设为允许的最小值 `freq = 0`，这相当 `delWord`。不带 `freq` 参数的 `addWord` 与 `forcedJoin` 等价。更多解释可参看原 Jieba [文档][fxsjy/jieba] 及其源码 [suggest_freq.\_\_doc\_\_][suggest_freq] 中的相关介绍。
```java
public synchronized void addWord(String word, int freq);
public synchronized void addWord(String word);
public synchronized void delWord(String word);
public int suggestFreqForJoin(String word);
public int suggestFreqForSplit(String... segs);
public synchronized int forcedSplit(String... segs);
public synchronized int forcedJoin(String word);
```

下面给出原 Jieba 文档中案例的实现，详见 [TestDocsExample][TestDocsExample]：
```java
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
[如果, 放到, post, 中将, 出错, 。]
494
494
[如果, 放到, post, 中, 将, 出错, 。]
[「, 台, 中, 」, 正确, 应该, 不会, 被, 切开]
69
69
[「, 台中, 」, 正确, 应该, 不会, 被, 切开]
*/
```


### 自定义词典

`Tokenizer` 初始化时需要一个 Jieba 自带的词典，如果不声明，则使用默认词典 `STD_WORD_DICT_GZ` 。
```
public Tokenizer(String filename)
public Tokenizer()
```

可供选择的词典定义在 `Dict` 抽象基类中，详见 [Dict][Dict] 类。其中 `NONE_DICT` 表示不使用任何 Jieba 自带的词典。额外词典均来自原 Jieba ，相关特性见原 Jieba [文档][fxsjy/jieba]。本项目还提供了所有词典的 gzip 压缩版，如果需要打包成 jar，可以选择性打包这些词典，以减小文件体积。
```java
public static final String NONE_DICT = "";
public static final String STD_WORD_DICT_TXT = "/dict.std.txt";
public static final String STD_WORD_DICT_GZ = "/dict.std.gz";
public static final String BIG_WORD_DICT_TXT = "/dict.big.txt";
public static final String BIG_WORD_DICT_GZ = "/dict.big.gz";
public static final String SMALL_WORD_DICT_TXT = "/dict.small.txt";
public static final String SMALL_WORD_DICT_GZ = "/dict.small.gz";
```

这些词典可以通过如下方法使用：
```java
Tokenizer dt = new Tokenizer(Dict.BIG_WORD_DICT_GZ);
```

`Tokenizer` 在创建后，可以通过 `loadUserDict` 方法加入若干用户自定义的词典。请确保词典的格式和文件编码正确，详细规则见原 Jieba [文档][fxsjy/jieba]。可以通过设置初始化词典为 `NONE_DICT` ，随后再利用 `loadUserDict` 导入自定义词典，来实现分词器词典的完全自定义化。自定义词典同样支持 gzip 压缩版，但请确保文件后缀名为 `.gz` 。
```java
public synchronized void loadUserDict(String filename)
```

下面给出原 Jieba 文档中案例的实现，详见 [TestDocsExample][TestDocsExample]：
```java
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
[李小福, 是, 创新, 办, 主任, 也, 是, 云, 计算, 方面, 的, 专家, ;,  , 什么, 是, 八, 一双, 鹿,
, 例如, 我, 输入, 一个, 带, “, 韩玉, 赏鉴, ”, 的, 标题, ，, 在, 自定义词, 库中, 也, 增加, 了, 此, 词为, N, 类,
, 「, 台, 中, 」, 正確, 應該, 不會, 被, 切開, 。, mac, 上, 可, 分出, 「, 石墨, 烯, 」, ；, 此時, 又, 可以, 分出, 來凱, 特琳, 了, 。]
[李小福, 是, 创新办, 主任, 也, 是, 云计算, 方面, 的, 专家, ;,  , 什么, 是, 八一双鹿,
, 例如, 我, 输入, 一个, 带, “, 韩玉赏鉴, ”, 的, 标题, ，, 在, 自定义, 词库, 中, 也, 增加, 了, 此, 词为, N, 类,
, 「, 台中, 」, 正確, 應該, 不會, 被, 切開, 。, mac, 上, 可, 分出, 「, 石墨烯, 」, ；, 此時, 又, 可以, 分出, 來, 凱特琳, 了, 。]
*/
```

另一个测试例子见 [TestDict][TestDict] 下的 `test_userDict` 函数。


### 词性标注

`POSTokenizer` 是词性标注的核心分词器类，详见 [POSTokenizer][POSTokenizer] 类。

`POSTokenizer` 内嵌 `Tokenizer` 类，具有 has-a 关系。在初始化时，可以将一个 `Tokenizer` 实例传入。默认使用 `new Tokenizer()` 作为内嵌 `Tokenizer` 实例。
```java
public POSTokenizer(Tokenizer dt);
public POSTokenizer();
```

`POSTokenizer` 的功能与 `Tokenizer` 相似，同样可以具有分词、词频调整功能。

`POSTokenizer` 与分词相关的方法如下。`TaggedWord` 对应于 `tuple(word, flag)` ，详见 [TaggedWord][TaggedWord] 类。
```java
public List<TaggedWord> cut(String sentence, boolean HMM);
public List<TaggedWord> cut(String sentence);
public List<TaggedWord> cut_noHMM(String sentence);
```

相关测试见 [TestPOSTokenizer][TestPOSTokenizer] 。

`POSTokenizer` 与词频调整相关的方法如下。需要注意的是： `addWord` 方法并不会检查传入的词性是否合理。
```java
public synchronized void addWord(String word, String pos);
public synchronized void delWord(String word);
```

需要注意的是，由于 `POSTokenizer` 与 `Tokenizer` 具有 has-a 关系，它将继承 `Tokenizer` 的所有词典，可以预先给 `Tokenizer` 实例定义好词典，再将其用于 `POSTokenizer` 的初始化，即可实现自定义词典。当内部 `Tokenizer` 指向的实例调用
`loadUserDict` 增加自定义词典时，相应 `POSTokenizer` 实例的词典也将同步变化。
```java
Tokenizer dt = new Tokenizer();
dt.loadUserDict("/dict.custom.txt");
POSTokenizer pt = new POSTokenizer(dt);
```

详细案例见 [TestDict][TestDict] 下的 `test_syncUserDict` 函数。

正因为 `POSTokenizer` 的词典继承于内嵌 `Tokenizer` 实例的词典，不应该允许单独修改 `POSTokenizer` 的词典，甚至通过修改 `POSTokenizer` 词典间接修改内嵌 `Tokenizer` 实例的词典，否则我认为这是一种以下犯上的设计，在一定程度上有违设计哲学。所以 `POSTokenizer` 类本身不具有单独直接自定义词典的的方法。


### 关键词提取

`TFIDF` 类和 `TextRank` 类分别可以实现基于 `TF-IDF` 算法和 `TextRank` 算法的关键词提取，他们共同继承与 `KeywordExtractor` 抽象基类，并各自实现了下述的 `extract` 抽象方法。详见 [TFIDF][TFIDF], [TextRank][TextRank], [KeywordExtractor][KeywordExtractor] 类。
```java
abstract public List<Keyword> extract(List<T> words, int topK);
```

`KeywordExtractor` 类还定义了一个统一的 `extract` 函数的多态，它使用默认值 `topK = 20` 。
```java
public List<Keyword> extract(List<T> words);
```

它们共同的返回值类型为 `Keyword` ，相当于 `tuple(word, score, rank)` 或 `tuple(word, flag, score, rank)` ，详见 [Keyword][Keyword] 类。它相当于原 Jieba 中的 `pair` 类，详见 [pair][pair] 。

与原 Jieba 不同的是，该项目中关键词提取类与分词器类是分离。因此，不再需要提供一个内嵌的分词器类来实现排序前的分词步骤，而是改成将词分好后再传入 `extract` 方法，以实现关键词提取。

相对应的， `TFIDF` 可以对 `Tokenier` 的分词结果进行排序，`TextRank` 可以对 `POSTokenizer` 的分词结果进行排序。对于 `TFIDF` ，需要传入 `List<String>` 类型的分词结果，返回 `tuple(word, score, rank)` 型的 `Keyword` ，对于 `TextRank` ，需要传入 `List<TaggedWord>` 类型的分词结果，返回 `tuple(word, flag, score, rank)` 型的 `Keyword` 。

对于 `TextRank` ，还有一个额外的 `extract` 多态，其中的 `posList` 参数为参与排序的词语词性。详细解释见原 Jieba [文档][fxsjy/jieba] 及其源码 [textrank.\_\_doc\_\_][textrank] 中的相关介绍。
```java
public List<Keyword> extract(List<TaggedWord> words, int topK, List<String> posList);
```

`TFIDF` 与 `TextRank` 均可在初始化时自定义停词表，`TFIDF` 还允许自定义 IDF 表。
```java
public TFIDF(String IDF_DICT, String STOP_WORDS_FILE);
public TFIDF(String IDF_DICT);
public TFIDF();
public TextRank(String STOP_WORDS_FILE);
public TextRank();
```

默认会导入 Jieba 自带的一个停词表 `DEFAULT_STOP_WORDS_TXT` ，它仅仅包含一些简单的英文停词，对于中文来说，可能还需要一些额外的停词。在本项目中我添加了一个曾经使用过的中文停词表 `EXTENDED_STOP_WORDS_TXT` ，它们均以常数的形式定义在 `KeywordExtractor` 中，推荐你在创建关键词提取实例时使用！
```java
TFIDF tfidf = new TFIDF(KeywordExtractor.EXTENDED_STOP_WORDS_TXT);
```

当然，也可以在创建实例后通过定义在 `KeywordExtractor` 中的 `loadStopWords` 方法导入自定义停词表，导入表的数量不限。具体案例见 [TestDict][TestDict] 中的 `test_idfStopWords` 函数。

IDF 表默认使用定义在 `TFIDF` 类中的 `STD_IDF_DICT_GZ` 。目前仅有这一种选项，原 Jieba 额外提供的 `idf.txt.big` 表过于年代古老，其文件体积甚至小于当前 Jieba 0.39 使用的 `idf.txt` ，因此这里就没有采用。也可以使用自定义的 IDF 表，具体案例见 [TestDict][TestDict] 中的 `test_idfDict` 函数。

停词表和 IDF 表同样支持 gzip 压缩版，但请确保文件后缀名为 `.gz` 。

下面给出原 Jieba [测试文件]()中一个案例的实现，详见 [TestDocsExample][TestDocsExample]：
```java
public static void test_3_keyword_extract_TFIDF() { /* 默认使用自定义的停词表 */

    Tokenizer dt = new Tokenizer();
    KeywordExtractor tfidf = new TFIDF(TFIDF.DEFAULT_DICT, TFIDF.EXTENDED_STOP_WORDS_TXT);
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
*/
```

更多具体案例见 [TestRank][TestRank] 。


### 延迟加载和缓存机制

`Tokenizer`, `POSTokenizer`, `TFIDF` 均采用惰性加载机制，在创建实例时，它们内部的词典和模型并不会马上导入，直到使用到时才会导入。你可以通过统一的 `initialize` 方法进行人为初始化。
```java
public synchronized void initialize();
```

该项目并未沿用原 Jieba 的词典缓存机制，如果有需要，你可以自行编写相关的模块，实现对特定的词典对象的序列化、压缩，缓存文件校验等操作。


### 命令行分词

该项目并未实现原 Jieba 的命令行分词（实际上相关模块写了一半，但最后不想写了 ... 详见 [Main][Main]）。



## 一些测试

### 测试环境

- **CPU:** Intel(R) Core(TM) i5-3210M CPU @ 2.50GHz
- **MEM:** DDR3L, 1600 MHz, 8 GB + 4 GB
- **OS:** Linux debian-9 4.9.0-4-amd64 #1 SMP Debian 4.9.65-3+deb9u1 (2017-12-23) x86_64 GNU/Linux
- **JDK:** OpenJDK 1.8.0_181
- **Python:** Python 3.6.6

### 文件说明
```console
debian-9:~/IdeaProjects/java-jieba# tree -L 1
.
├── LICENSE
├── README.md
├── backup
├── input
├── java-jieba.iml
├── output
├── pom.xml
├── py
├── src
└── target

6 directories, 4 files
```

测试文件为 `input/` 内的 `围城.txt`。`output/` 内有各种输出的 JSON 数据。`py/` 内以 `test_*` 为前缀的 python 脚本中定义了相关的测试函数，并存储了相应的测试数据。Java-Jieba 的测试函数及其测试数据存在 `src/test/` 中以 `Test*` 为前缀的类里。

### 一致性测试

测试方法为： 二者分别采用相同的函数对 `围城.txt` 进行分词，以 python 函数 `lambda x: (x != "") and (not x.isspace())` 筛除空字符和空格，再分别取 `set()` ，然后以二者分词结果的 `交集长度/并集长度` 作为 `score` ，可以验证 Java-Jieba 与原 Jieba 的分词结果几乎完全一致。详见 [py/test\_dt.py][test_dt.py]

| Mode         | HMM    | Score                    |
| :----------- | :---:  | :----------------------: |
| cut          | true   | 18489 / 18489 = 1.000000 |
| cut          | false  | 14620 / 14623 = 0.999795 |
| cutAll       | /      | 18604 / 18604 = 1.000000 |
| cutForSearch | true   | 20202 / 20202 = 1.000000 |
| cutForSearch | false  | 16320 / 16323 = 0.999816 |


### 分词效率

下面是对 Python 原版 Jieba 和 Java-Jieba 分词效率的一些对比测试的结果。

#### 单进程单线程

采用反复分词的方式进行测试，Java-Jieba 的 `Tokenier` 单进程单线程分词的效率是 Python 原版 Jieba 的 3-8 倍。不过，还要考虑到 JVM 对重复执行代码的一些优化，例如 JIT 技术。从 Java 的测试数据来开，Java 第 1 次分词的花销通常是第 200 次分词的 3-6 倍，单次分词的效率不相上下。详见 [test\_dt.py][test_dt.py] 和 [TestMultiCut][TestMultiCut] 。

| Language | Mode         | HMM   |  N  | Time/s |
| :------- | :----------- | :---: | :-: | :----: |
| Python   | cut          | true  | 100 | 149    |
| Java     | cut          | true  | 200 | 38     |
| Python   | cut          | false | 100 | 94     |
| Java     | cut          | false | 200 | 24     |
| Python   | cutAll       | /     | 100 | 50     |
| Java     | cutAll       | /     | 200 | 18     |
| Python   | cutForSearch | true  | 100 | 154    |
| Java     | cutForSearch | true  | 200 | 70     |
| Python   | cutForSearch | false | 100 | 100    |
| Java     | cutForSearch | false | 200 | 60     |

并未系统性地测试过 `POSTokenizer` 的效率，不过从单次测试的时长估计，Java-Jieba 的分词速度似乎比原版 Jieba 要稍慢。


#### 并发与并行

对原版 Jieba 采用多进程并行分词，创建 2 个独立的 `jieba.Tokenier` 实例，进程数 2，最终效率提升为原来的 2 倍。详见 [test\_dt.py][test_dt.py] 。

顺便提一句，原 Jieba 采用的多进程分词在我的机器上并没有实现分词效率的提升。

对 Java-Jieba 采用多线程异步分词，实际测试中发现，线程池大小设为 1-4 时，效率反而比正常的单线程分词要慢 ... 从测试数据来看，可能是因为线程调度存在开销，分词过程中偶尔会出现较长时间的阻塞所导致的。

我本来想顺便设计几个用于支持多线程分词的类，因为这个测试结果便作罢了。相关测试类见 [TestAsync][TestAsync], [AsyncTokenizer][AsyncTokenizer] 。



## 写在最后

有任何问题和建议请直接 issue ～


## 证书

- Jieba 0.39 [MIT LICENSE](https://github.com/fxsjy/jieba/blob/master/LICENSE)
- Java-Jieba [MIT LICENSE](https://github.com/zhongxinghong/Java-Jieba/blob/master/LICENSE)



[maven]: http://maven.apache.org/
[fxsjy/jieba]: https://github.com/fxsjy/jieba
[huaban/jieba-analysis]: https://github.com/huaban/jieba-analysis
[suggest_freq]: https://github.com/fxsjy/jieba/blob/master/jieba/__init__.py
[pair]: https://github.com/fxsjy/jieba/blob/master/jieba/posseg/__init__.py
[textrank]: https://github.com/fxsjy/jieba/blob/master/jieba/analyse/textrank.py

[Tokenizer]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/main/java/top/rabbit/jieba/tokenizer/Tokenizer.java
[POSTokenizer]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/main/java/top/rabbit/jieba/tokenizer/POSTokenizer.java
[TFIDF]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/main/java/top/rabbit/jieba/rank/TFIDF.java
[TextRank]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/main/java/top/rabbit/jieba/rank/TextRank.java
[KeywordExtractor]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/main/java/top/rabbit/jieba/rank/KeywordExtractor.java
[Dict]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/main/java/top/rabbit/jieba/dataset/Dict.java
[LocatedWord]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/main/java/top/rabbit/jieba/struct/LocatedWord.java
[TaggedWord]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/main/java/top/rabbit/jieba/struct/TaggedWord.java
[Keyword]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/main/java/top/rabbit/jieba/struct/Keyword.java
[CharUtils]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/main/java/top/rabbit/jieba/util/CharUtils.java
[RegexUtils]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/main/java/top/rabbit/jieba/util/RegexUtils.java
[TestDocsExample]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/test/java/top/rabbit/jieba/TestDocsExample.java
[TestDict]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/test/java/top/rabbit/jieba/TestDict.java
[TestCut]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/test/java/top/rabbit/jieba/TestCut.java
[TestMultiCut]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/test/java/top/rabbit/jieba/TestMultiCut.java
[TestPOSTokenizer]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/test/java/top/rabbit/jieba/TestPOSTokenizer.java
[TestRank]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/test/java/top/rabbit/jieba/TestRank.java
[Main]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/test/java/top/rabbit/jieba/Main.java
[TestAsync]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/test/java/top/rabbit/jieba/TestAsync.java
[AsyncTokenizer]: https://github.com/zhongxinghong/Java-Jieba/blob/master/src/test/java/top/rabbit/jieba/AsyncTokenizer.java

[doc-backup]: https://github.com/zhongxinghong/Java-Jieba/tree/master/backup
[test_dt.py]: https://github.com/zhongxinghong/Java-Jieba/blob/master/py/test_dt.py
