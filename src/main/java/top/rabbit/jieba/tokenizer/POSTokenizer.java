package top.rabbit.jieba.tokenizer;

import top.rabbit.jieba.dataset.POSDict;
import top.rabbit.jieba.struct.Pair;
import top.rabbit.jieba.struct.TaggedWord;
import top.rabbit.jieba.util.CharUtils;
import top.rabbit.jieba.util.RegexUtils;
import top.rabbit.jieba.viterbi.POSViterbi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;

public final class POSTokenizer {

    private Tokenizer dt;
    private POSDict dict = null;
    private final String DICT;
    private final List<String> userDicts;

    public POSTokenizer(Tokenizer dt) {
        this.dt = dt;
        DICT = dt.getDict();
        userDicts = dt.getUserDicts();
    }

    public POSTokenizer() {
        this(new Tokenizer());
    }

    synchronized boolean isInitialized() {
        return (dict != null && dt.isInitialized());
    }

    public synchronized void initialize() {
        if (isInitialized()) return;
        dt.initialize();
        dict = new POSDict(DICT);
        for (String filename : userDicts) {
            loadUserDict(filename);
        }
        POSViterbi.initialize();
    }

    public String getDict() {
        return DICT;
    }

    public List<String> getUserDicts() {
        return new ArrayList<String>(userDicts);
    }

    private synchronized void syncUserDict() {
        int i = dt.getUserDictsSize();
        if (i != userDicts.size()) { // 将超出长度的字典加入
            for (String filename : dt.getUserDicts().subList(userDicts.size(), i)) {
                loadUserDict(filename);
            }
        }
    }

    public synchronized void addWord(String word, String pos) {
        initialize();
        dict.addWord(word, pos);
    }

    public synchronized void delWord(String word) {
        initialize();
        dict.delWord(word);
    }

    private synchronized void loadUserDict(String filename) {
        initialize();
        try {
            String line;
            Matcher mat;
            String word;
            String pos;

            long st = System.currentTimeMillis();
            Path p = Paths.get(filename);
            System.out.print(String.format("Load custom POS dict '%s' ... ", p.getFileName()));
            BufferedReader fin;
            if (filename.endsWith(".gz")) {
                fin = new BufferedReader(new InputStreamReader(
                        new GZIPInputStream(this.getClass().getResourceAsStream(filename))));
            } else {
                fin = new BufferedReader(new InputStreamReader(
                        this.getClass().getResourceAsStream(filename)));
            }
            while (fin.ready()) {
                line = fin.readLine().trim();
                mat = RegexUtils.reUserDict.matcher(line); // 带BOM头过滤
                if (mat.matches()) { // 空行为false
                    word = mat.group(1).trim();
                    pos = mat.group(3);
                    if (pos != null) {
                        dict.addWord(word, pos.trim());
                    }
                }
            }
            fin.close();
            long et = System.currentTimeMillis();
            System.out.println(String.format("Done! cost %s ms", et - st));

            userDicts.add(filename);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private List<TaggedWord> _cut(String sentence) {
        Pair<Double, List<Pair<String, String>>> res = POSViterbi.viterbi(sentence);
        double prob = res.a;
        List<Pair<String, String>> posList = res.b;
        int begin = 0, nexti = 0;
        List<TaggedWord> segs = new ArrayList<TaggedWord>();
        char ch;
        String pos;

        for (int i = 0; i < sentence.length(); ++i) {
            ch = sentence.charAt(i);
            pos = posList.get(i).a;
            if (pos.equals("B")) {
                begin = i;
            } else if (pos.equals("E")) {
                segs.add(new TaggedWord(sentence.substring(begin, i+1), posList.get(i).b));
                nexti = i + 1;
            } else if (pos.equals("S")) {
                segs.add(new TaggedWord(String.valueOf(ch), posList.get(i).b));
                nexti = i + 1;
            }
        }
        if (nexti < sentence.length()) {
            segs.add(new TaggedWord(sentence.substring(nexti, sentence.length()), posList.get(nexti).b));
        }
        return segs;
    }
    /*
    def __cut(self, sentence):
        prob, pos_list = viterbi(
            sentence, char_state_tab_P, start_P, trans_P, emit_P)
        begin, nexti = 0, 0

        for i, char in enumerate(sentence):
            pos = pos_list[i][0]
            if pos == 'B':
                begin = i
            elif pos == 'E':
                yield pair(sentence[begin:i + 1], pos_list[i][1])
                nexti = i + 1
            elif pos == 'S':
                yield pair(char, pos_list[i][1])
                nexti = i + 1
        if nexti < len(sentence):
            yield pair(sentence[nexti:], pos_list[nexti][1])
     */

    private List<TaggedWord> cutDetail(String sentence) {
        List<TaggedWord> segs = new ArrayList<TaggedWord>();
        for (String blk : RegexUtils.split(RegexUtils.reHan_detail, sentence)) {
            if (CharUtils.isChinese(blk)) {
                segs.addAll(_cut(blk));
            } else {
                for (String x : RegexUtils.split(RegexUtils.reSkip_detail, sentence)) {
                    if (!CharUtils.isSpace(x)) {
                        if (CharUtils.isNumber(x)) { // 等效
                            // if (RegexUtils.reNum.matcher(x).matches()) {
                            segs.add(new TaggedWord(x, TaggedWord.FLAG_NUMERAL));
                        } else if (CharUtils.isEnglish(x)) {
                        // } else if (RegexUtils.reEng.matcher(x).matches()) {
                            segs.add(new TaggedWord(x, TaggedWord.FLAG_ENGLISH));
                        } else {
                            segs.add(new TaggedWord(x, TaggedWord.FLAG_UNKNOWN));
                        }
                    }
                }
            }
        }
        return segs;
    }
    /*
    def __cut_detail(self, sentence):
        blocks = re_han_detail.split(sentence)
        for blk in blocks:
            if re_han_detail.match(blk):
                for word in self.__cut(blk):
                    yield word
            else:
                tmp = re_skip_detail.split(blk)
                for x in tmp:
                    if x:
                        if re_num.match(x):
                            yield pair(x, 'm')
                        elif re_eng.match(x):
                            yield pair(x, 'eng')
                        else:
                            yield pair(x, 'x')

     */
    private List<TaggedWord> cutDAG_noHMM(String sentence) {
        List<TaggedWord> segs = new ArrayList<TaggedWord>();
        Map<String, String> wordTagTab = dict.getWordTagTab();
        Map<Integer, List<Integer>> DAG = dt.getDAG(sentence);
        Map<Integer, Pair<Double, Integer>> route = dt.calc(sentence, DAG);
        int x = 0, y;
        String lWord;
        StringBuilder sb = new StringBuilder();
        while (x < sentence.length()) {
            y = route.get(x).b + 1;
            lWord = sentence.substring(x, y);
            if ( (lWord.length() == 1) && (CharUtils.isEnglish(lWord)) ) {
            // if (RegexUtils.reEngChar.matcher(lWord).matches()) {
                sb.append(lWord);
                x = y;
            } else {
                if (sb.length() > 0) {
                    segs.add(new TaggedWord(sb.toString(), TaggedWord.FLAG_ENGLISH));
                    sb.setLength(0);
                }
                segs.add(new TaggedWord(lWord, wordTagTab.getOrDefault(lWord, TaggedWord.FLAG_UNKNOWN)));
                x = y;
            }
        }
        if (sb.length() > 0) {
            segs.add(new TaggedWord(sb.toString(), TaggedWord.FLAG_ENGLISH));
            sb.setLength(0);
        }
        return segs;
    }
    /*
    def __cut_DAG_NO_HMM(self, sentence):
        DAG = self.tokenizer.get_DAG(sentence)
        route = {}
        self.tokenizer.calc(sentence, DAG, route)
        x = 0
        N = len(sentence)
        buf = ''
        while x < N:
            y = route[x][1] + 1
            l_word = sentence[x:y]
            if re_eng1.match(l_word):
                buf += l_word
                x = y
            else:
                if buf:
                    yield pair(buf, 'eng')
                    buf = ''
                yield pair(l_word, self.word_tag_tab.get(l_word, 'x'))
                x = y
        if buf:
            yield pair(buf, 'eng')
            buf = ''
     */

    private List<TaggedWord> cutDAG(String sentence) {
        List<TaggedWord> segs = new ArrayList<TaggedWord>();
        Map<String, String> wordTagTab = dict.getWordTagTab();
        Map<Integer, List<Integer>> DAG = dt.getDAG(sentence);
        Map<Integer, Pair<Double, Integer>> route = dt.calc(sentence, DAG);
        int x = 0, y;
        String lWord;
        String s, elem;
        StringBuilder sb = new StringBuilder();
        while (x < sentence.length()) {
            y = route.get(x).b + 1;
            lWord = sentence.substring(x, y);
            if (y - x == 1) {
                sb.append(lWord);
            } else {
                if (sb.length() > 0) {
                    s = sb.toString();
                    if (sb.length() == 1) {
                        segs.add(new TaggedWord(s, wordTagTab.getOrDefault(s, TaggedWord.FLAG_UNKNOWN)));
                    } else if (dt.getFreq(s) <= 0) { // 0 or -1
                        segs.addAll(cutDetail(s));
                    } else {
                        for (char ch : s.toCharArray()) {
                            elem = String.valueOf(ch);
                            segs.add(new TaggedWord(elem, wordTagTab.getOrDefault(elem, TaggedWord.FLAG_UNKNOWN)));
                        }
                    }
                    sb.setLength(0);
                }
                segs.add(new TaggedWord(lWord, wordTagTab.getOrDefault(lWord, TaggedWord.FLAG_UNKNOWN)));
            }
            x = y;
        }
        if (sb.length() > 0) {
            s = sb.toString();
            if (sb.length() == 1) {
                segs.add(new TaggedWord(s, wordTagTab.getOrDefault(s, TaggedWord.FLAG_UNKNOWN)));
            } else if (dt.getFreq(s) <= 0) { // 0 or -1
                segs.addAll(cutDetail(s));
            } else {
                for (char ch : s.toCharArray()) {
                    elem = String.valueOf(ch);
                    segs.add(new TaggedWord(elem, wordTagTab.getOrDefault(elem, TaggedWord.FLAG_UNKNOWN)));
                }
            }
            sb.setLength(0);
        }
        return segs;
    }
    /*
    def __cut_DAG(self, sentence):
        DAG = self.tokenizer.get_DAG(sentence)
        route = {}

        self.tokenizer.calc(sentence, DAG, route)

        x = 0
        buf = ''
        N = len(sentence)
        while x < N:
            y = route[x][1] + 1
            l_word = sentence[x:y]
            if y - x == 1:
                buf += l_word
            else:
                if buf:
                    if len(buf) == 1:
                        yield pair(buf, self.word_tag_tab.get(buf, 'x'))
                    elif not self.tokenizer.FREQ.get(buf):
                        recognized = self.__cut_detail(buf)
                        for t in recognized:
                            yield t
                    else:
                        for elem in buf:
                            yield pair(elem, self.word_tag_tab.get(elem, 'x'))
                    buf = ''
                yield pair(l_word, self.word_tag_tab.get(l_word, 'x'))
            x = y

        if buf:
            if len(buf) == 1:
                yield pair(buf, self.word_tag_tab.get(buf, 'x'))
            elif not self.tokenizer.FREQ.get(buf):
                recognized = self.__cut_detail(buf)
                for t in recognized:
                    yield t
            else:
                for elem in buf:
                    yield pair(elem, self.word_tag_tab.get(elem, 'x'))
     */

    private List<TaggedWord> cutInternal(String sentence, boolean HMM) {
        // self.makesure_userdict_loaded()
        // decode sentence
        List<TaggedWord> segs = new ArrayList<TaggedWord>();
        for (String blk : RegexUtils.split(RegexUtils.reHan_internal, sentence)) {
            if (CharUtils.isNotSpace(blk)) {
            // if (RegexUtils.reHan_internal.matcher(blk).matches()) {
                if (HMM) {
                    segs.addAll(cutDAG(blk));
                } else {
                    segs.addAll(cutDAG_noHMM(blk));
                }
            } else {
                for (String x : RegexUtils.split(RegexUtils.reSkip_internal, blk)) {
                    if (CharUtils.isNotSpace(x)) {
                    // if (RegexUtils.reHan_internal.matcher(x).matches()) {
                        segs.add(new TaggedWord(x, TaggedWord.FLAG_UNKNOWN));
                    } else {
                        for (char ch : x.toCharArray()) {
                            String xx = String.valueOf(ch);
                            if (CharUtils.isNumber(xx)) { // 等效
                                // if (RegexUtils.reNum.matcher(xx).matches()) {
                                segs.add(new TaggedWord(xx, TaggedWord.FLAG_NUMERAL));
                            } else if (CharUtils.isEnglish(xx)) {
                            // } else if (RegexUtils.reEng.matcher(x).matches()) { // xx or x ????????
                                segs.add(new TaggedWord(xx, TaggedWord.FLAG_ENGLISH));
                            } else {
                                segs.add(new TaggedWord(xx, TaggedWord.FLAG_UNKNOWN));
                            }
                        }
                    }
                }
            }
        }
        return segs;
    }
    /*
    def __cut_internal(self, sentence, HMM=True):
        self.makesure_userdict_loaded()
        sentence = strdecode(sentence)
        blocks = re_han_internal.split(sentence)
        if HMM:
            cut_blk = self.__cut_DAG
        else:
            cut_blk = self.__cut_DAG_NO_HMM

        for blk in blocks:
            if re_han_internal.match(blk):
                for word in cut_blk(blk):
                    yield word
            else:
                tmp = re_skip_internal.split(blk)
                for x in tmp:
                    if re_skip_internal.match(x):
                        yield pair(x, 'x')
                    else:
                        for xx in x:
                            if re_num.match(xx):
                                yield pair(xx, 'm')
                            elif re_eng.match(x):
                                yield pair(xx, 'eng')
                            else:
                                yield pair(xx, 'x')
     */

    public List<TaggedWord> cut(String sentence, boolean HMM) {
        initialize();
        syncUserDict();
        return cutInternal(sentence, HMM);
    }

    public List<TaggedWord> cut(String sentence) {
        return cut(sentence, true);
    }

    public List<TaggedWord> cut_noHMM(String sentence) {
        return cut(sentence, false);
    }
}
