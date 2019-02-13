package top.rabbit.jieba.tokenizer;

import top.rabbit.jieba.dataset.Dict;
import top.rabbit.jieba.struct.LocatedWord;
import top.rabbit.jieba.util.RegexUtils;
import top.rabbit.jieba.dataset.WordFrequencyDict;
import top.rabbit.jieba.viterbi.WordFrequencyViterbi;
import top.rabbit.jieba.util.CharUtils;
import top.rabbit.jieba.struct.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;

public final class Tokenizer {

    private static final Double MIN_INF = Double.NEGATIVE_INFINITY;

    private final String DICT;
    private final List<String> userDicts = new ArrayList<String>();
    private WordFrequencyDict dict = null;

    public static final String DEFAULT_DICT = Dict.STD_WORD_DICT_GZ;

    public Tokenizer(String filename) {
        DICT = filename;
    }

    public Tokenizer() {
        this(DEFAULT_DICT);
    }

    boolean isInitialized() {
        return (dict != null);
    }

    public synchronized void initialize() {
        if ( isInitialized() ) return;
        dict = new WordFrequencyDict(DICT);
        WordFrequencyViterbi.initialize();
    }

    public String getDict() {
        return DICT;
    }

    public List<String> getUserDicts() {
        return new ArrayList<String>(userDicts);
    }

    public int getUserDictsSize() {
        return userDicts.size();
    }

    public synchronized void loadUserDict(String filename) {
        initialize();
        try {
            String line;
            Matcher mat;
            String word;
            String _freq;
            int freq;

            long st = System.currentTimeMillis();
            Path p = Paths.get(filename);
            System.out.print(String.format("Load custom word frequency dict '%s' ... ", p.getFileName()));
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
                    _freq = mat.group(2);
                    if (_freq != null) {
                        freq = Integer.valueOf(_freq.trim());
                        addWord(word, freq);
                    } else {
                        addWord(word);
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

    Map<Integer, Pair<Double, Integer>> calc(String sentence, Map<Integer, List<Integer>> DAG) {
        Map<String, Integer> freqs = dict.getFreqs();
        long total = dict.getTotal();

        Map<Integer, Pair<Double, Integer>> route = new HashMap<Integer, Pair<Double, Integer>>();

        int N = sentence.length();
        route.put(N, new Pair<Double, Integer>(0.0D, 0));
        double logTotal = Math.log(total);

        int maxX;
        double score, maxScore;

        for (int i = N-1; i > -1; --i) {
            maxScore = MIN_INF;
            maxX = -1;
            for (int x : DAG.get(i)) {

                score = Math.log(freqs.getOrDefault(sentence.substring(i,x+1), 1))
                            - logTotal + route.get(x+1).a;
                if (score > maxScore) {
                    maxScore = score;
                    maxX = x;
                } else if ((score == maxScore) && (x > maxX)) {
                    maxX = x;
                }
            }
            route.put(i, new Pair<Double, Integer>(maxScore, maxX));
        }
        return route;
        /*
        def calc(self, sentence, DAG, route):
            N = len(sentence)
            route[N] = (0, 0)
            logtotal = log(self.total)
            for idx in xrange(N - 1, -1, -1):
                route[idx] = max((log(self.FREQ.get(sentence[idx:x + 1]) or 1) -
                                  logtotal + route[x + 1][0], x) for x in DAG[idx])
         */
    }

    Map<Integer, List<Integer>> getDAG(String sentence) {
        Map<String, Integer> freqs = dict.getFreqs();
        Map<Integer, List<Integer>> DAG = new HashMap<Integer, List<Integer>>();
        int N = sentence.length();
        char[] chars = sentence.toCharArray();

        int i;
        List<Integer> tmpList;
        String frag;
        for (int k = 0; k < N; ++k) {
            tmpList = new ArrayList<Integer>();
            i = k;
            frag = String.valueOf(chars[k]);
            while (i < N && freqs.containsKey(frag)) {
                if (freqs.get(frag) > 0) {
                    tmpList.add(i);
                }
                i += 1;
                if (i == N) // 原 jieba 中此处可能超出索引
                    frag = sentence.substring(k, N);
                else
                    frag = sentence.substring(k, i + 1);
            }
            if (tmpList.isEmpty()) {
                tmpList.add(k);
            }
            DAG.put(k, tmpList);
        }
        return DAG;
        /*
        def get_DAG(self, sentence):
            self.check_initialized()
            DAG = {}
            N = len(sentence)
            for k in xrange(N):
                tmplist = []
                i = k
                frag = sentence[k]
                while i < N and frag in self.FREQ:
                    if self.FREQ[frag]:
                        tmplist.append(i)
                    i += 1
                    frag = sentence[k:i + 1]
                if not tmplist:
                    tmplist.append(k)
                DAG[k] = tmplist
            return DAG
         */
    }

    private List<String> _cutAll(String sentence) {
        List<String> segs = new ArrayList<String>();
        Map<Integer, List<Integer>> DAG = getDAG(sentence);
        int old_j = -1;
        int k;
        for (Map.Entry<Integer, List<Integer>> entry: DAG.entrySet()) {
            k = entry.getKey();
            List<Integer> L = entry.getValue();
            if (L.size() == 1 && k > old_j) {
                segs.add(sentence.substring(k, L.get(0)+1));
                old_j = L.get(0);
            } else {
                for (int j : L) {
                    if (j > k) {
                        segs.add(sentence.substring(k, j+1));
                        old_j = j;
                    }
                }
            }
        }
        return segs;
        /*
        def __cut_all(self, sentence):
            dag = self.get_DAG(sentence)
            old_j = -1
            for k, L in iteritems(dag):
                if len(L) == 1 and k > old_j:
                    yield sentence[k:L[0] + 1]
                    old_j = L[0]
                else:
                    for j in L:
                        if j > k:
                            yield sentence[k:j + 1]
                            old_j = j
         */
    }

    private List<String> cutDAG_noHMM(String sentence) {
        List<String> segs = new ArrayList<String>();
        Map<Integer, List<Integer>> DAG = getDAG(sentence);
        Map<Integer, Pair<Double, Integer>> route = calc(sentence, DAG);
        int x = 0;
        int N = sentence.length();
        StringBuilder sb = new StringBuilder();
        int y;
        String lWord;
        while (x < N) {
            y = route.get(x).b + 1;
            lWord = sentence.substring(x, y);
            if ( (lWord.length() == 1) && (CharUtils.isEnglish(lWord.charAt(0))) ) {
                sb.append(lWord);
                x = y;
            } else {
                if (sb.length() > 0) {
                    segs.add(sb.toString());
                    sb.setLength(0);
                }
                segs.add(lWord);
                x = y;
            }
        }
        if (sb.length() > 0) {
            segs.add(sb.toString());
            sb.setLength(0);
        }
        return segs;
        /*
        def __cut_DAG_NO_HMM(self, sentence):
            DAG = self.get_DAG(sentence)
            route = {}
            self.calc(sentence, DAG, route)
            x = 0
            N = len(sentence)
            buf = ''
            while x < N:
                y = route[x][1] + 1
                l_word = sentence[x:y]
                if re_eng.match(l_word) and len(l_word) == 1:
                    buf += l_word
                    x = y
                else:
                    if buf:
                        yield buf
                        buf = ''
                    yield l_word
                    x = y
            if buf:
                yield buf
                buf = ''
         */
    }

    private List<String> cutDAG(String sentence) {
        List<String> segs = new ArrayList<String>();
        Map<Integer, List<Integer>> DAG = getDAG(sentence);
        Map<String, Integer> freqs = dict.getFreqs();
        Map<Integer, Pair<Double, Integer>> route = calc(sentence, DAG);
        int x = 0;
        int N = sentence.length();
        StringBuilder sb = new StringBuilder();
        int y;
        String lWord;
        while (x < N) {
            y = route.get(x).b + 1;
            lWord = sentence.substring(x, y);
            if (y - x == 1) {
                sb.append(lWord);
            } else {
                if (sb.length() > 0) {
                    if (sb.length() == 1) {
                        segs.add(sb.toString());
                        sb.setLength(0);
                    } else {
                        if (freqs.getOrDefault(sb.toString(), 0) == 0) {
                            segs.addAll(WordFrequencyViterbi.cut(sb.toString()));
                        } else {
                            for (char ch : sb.toString().toCharArray()) {
                                segs.add(String.valueOf(ch));
                            }
                        }
                        sb.setLength(0);
                    }
                }
                segs.add(lWord);
            }
            x = y;
        }
        if (sb.length() > 0) {
            if (sb.length() == 1) {
                segs.add(sb.toString());
                sb.setLength(0);
            } else {
                if (freqs.getOrDefault(sb.toString(), 0) == 0) {
                    segs.addAll(WordFrequencyViterbi.cut(sb.toString()));
                } else {
                    for (char ch : sb.toString().toCharArray()) {
                        segs.add(String.valueOf(ch));
                    }
                }
                sb.setLength(0);
            }
        }
        return segs;
        /*
        def __cut_DAG(self, sentence):
            DAG = self.get_DAG(sentence)
            route = {}
            self.calc(sentence, DAG, route)
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
                            yield buf
                            buf = ''
                        else:
                            if not self.FREQ.get(buf):
                                recognized = finalseg.cut(buf)
                                for t in recognized:
                                    yield t
                            else:
                                for elem in buf:
                                    yield elem
                            buf = ''
                    yield l_word
                x = y

            if buf:
                if len(buf) == 1:
                    yield buf
                elif not self.FREQ.get(buf):
                    recognized = finalseg.cut(buf)
                    for t in recognized:
                        yield t
                else:
                    for elem in buf:
                        yield elem
         */
    }

    /*  def cut(self, sentence, cut_all=False, HMM=True):
            sentence = strdecode(sentence)
            if cut_all:
                re_han = re_han_cut_all
                re_skip = re_skip_cut_all
            else:
                re_han = re_han_default
                re_skip = re_skip_default
            if cut_all:
                cut_block = self.__cut_all
            elif HMM:
                cut_block = self.__cut_DAG
            else:
                cut_block = self.__cut_DAG_NO_HMM
            blocks = re_han.split(sentence)
            for blk in blocks:
                if not blk:
                    continue
                if re_han.match(blk):
                    for word in cut_block(blk):
                        yield word
                else:
                    tmp = re_skip.split(blk)
                    for x in tmp:
                        if re_skip.match(x):
                            yield x
                        elif not cut_all:
                            for xx in x:
                                yield xx
                        else:
                            yield x
     */
    public List<String> cut(String sentence, boolean HMM) {
        // re_han = re_han_default
        // re_skip = re_skip_default
        // cut_block = self.__cut_DAG
        // cut_block = self.__cut_DAG_NO_HMM
        initialize();
        List<String> segs = new ArrayList<String>();
        // decode sentence
        for (String blk : RegexUtils.split(RegexUtils.reHan_cut, sentence)) {
            //if (CharUtils.isSpace(blk)) // if not blk 意味着 blk 为空字符串，并不是非空格
            if (blk.isEmpty()) {          // 为了保证分词后字符数不发生变化，此处不能用 isSpace 来判断
                continue;                 // 否则会影响探索模式
            }
            if (CharUtils.isNotSpace(blk)) { // 等效
            //if (RegexUtils.reHan_cut.matcher(blk).matches()) {
                if (HMM) {
                    segs.addAll(cutDAG(blk));
                } else {
                    segs.addAll(cutDAG_noHMM(blk));
                }
            } else {
                for (String tmp : RegexUtils.split(RegexUtils.reSkip_cut, blk)) {
                    // 省略 re_skip.match 如果 match 则为 whitespace
                    for (char ch : tmp.toCharArray()) {
                        segs.add(String.valueOf(ch));
                    }
                }
            }
        }
        return segs;
        /*
                else:
                    tmp = re_skip.split(blk)
                    for x in tmp:
                        if re_skip.match(x):
                            yield x
                        elif not cut_all:
                            for xx in x:
                                yield xx
                        else:
                            yield x

             => else:
                    tmp = re_skip.split(blk)
                    for x in tmp:
                        for xx in x:
                            yield xx
         */
    }

    public List<String> cut(String sentence) {
        return cut(sentence, true);
    }

    public List<String> cut_noHMM(String sentence) {
        return cut(sentence, false);
    }

    public List<String> cutAll(String sentence) {
        // re_han = re_han_cut_all
        // re_skip = re_skip_cut_all
        // cut_block = self.__cut_all
        initialize();
        List<String> segs = new ArrayList<String>();
        for (String blk : RegexUtils.split(RegexUtils.reHan_cutAll, sentence)) {
            if (CharUtils.isSpace(blk))
                continue;
            if (CharUtils.isChinese(blk)) {
                segs.addAll(_cutAll(blk));
            } else {
                segs.addAll(RegexUtils.split(RegexUtils.reSkip_cutAll, blk));
            }
        }
        return segs;
        /*
                else:
                    tmp = re_skip.split(blk)
                    for x in tmp:
                        if re_skip.match(x):
                            yield x
                        elif not cut_all:
                            for xx in x:
                                yield xx
                        else:
                            yield x

             => else:
                    tmp = re_skip.split(blk)
                    for x in tmp:
                        yield x
         */
    }

    public List<String> cutForSearch(String sentence, boolean HMM) {
        List<String> _segs = cut(sentence, HMM); // cut_all = False
        List<String> segs = new ArrayList<String>(_segs.size()); // 之后还会扩容
        Map<String, Integer> freqs = dict.getFreqs();
        String gram2, gram3;
        int width;
        for (String word : _segs) {
            width = word.length();
            if (width > 2) {
                for (int i = 0; i < width-1; ++i) {
                    gram2 = word.substring(i, i+2);
                    if (freqs.getOrDefault(gram2, 0) != 0) {
                        segs.add(gram2);
                    }
                }
            }
            if (width > 3) {
                for (int i = 0; i < width-2; ++i) {
                    gram3 = word.substring(i, i+3);
                    if (freqs.getOrDefault(gram3, 0) != 0) {
                        segs.add(gram3);
                    }
                }
            }
            segs.add(word);
        }
        return segs;
        /*
        def cut_for_search(self, sentence, HMM=True):
            words = self.cut(sentence, HMM=HMM)
            for w in words:
                if len(w) > 2:
                    for i in xrange(len(w) - 1):
                        gram2 = w[i:i + 2]
                        if self.FREQ.get(gram2):
                            yield gram2
                if len(w) > 3:
                    for i in xrange(len(w) - 2):
                        gram3 = w[i:i + 3]
                        if self.FREQ.get(gram3):
                            yield gram3
                yield w
         */
    }

    public List<String> cutForSearch(String sentence) {
        return cutForSearch(sentence, true);
    }

    public List<String> cutForSearch_noHMM(String sentence) {
        return cutForSearch(sentence, false);
    }

    public List<LocatedWord> cutWithIndex(String sentence, boolean HMM) {
        List<String> _segs = cut(sentence, HMM);
        List<LocatedWord> segs = new ArrayList<LocatedWord>(_segs.size());
        int start = 0;
        int width;
        for (String word : _segs) {
            width = word.length();
            segs.add(new LocatedWord(word, start, start + width));
            start += width;
        }
        return segs;
        /*
        start = 0
        if mode == 'default':
            for w in self.cut(unicode_sentence, HMM=HMM):
                width = len(w)
                yield (w, start, start + width)
                start += width
         */
    }

    public List<LocatedWord> cutWithIndex(String sentence) {
        return cutWithIndex(sentence, true);
    }

    public List<LocatedWord> cutWithIndex_noHMM(String sentence) {
        return cutWithIndex(sentence, false);
    }

    public List<LocatedWord> cutForSearchWithIndex(String sentence, boolean HMM) {
        List<String> _segs = cut(sentence, HMM);
        List<LocatedWord> segs = new ArrayList<LocatedWord>(_segs.size()); // 之后还会扩容
        Map<String, Integer> freqs = dict.getFreqs();
        String gram2, gram3;
        int start = 0, width;
        for (String word : _segs) {
            width = word.length();
            if (width > 2) {
                for (int i = 0; i < width-1; ++i) {
                    gram2 = word.substring(i, i+2);
                    if (freqs.getOrDefault(gram2, 0) != 0) {
                        segs.add(new LocatedWord(gram2, start+i, start+ i + 2));
                    }
                }
            }
            if (width > 3) {
                for (int i = 0; i < width-2; ++i) {
                    gram3 = word.substring(i, i+3);
                    if (freqs.getOrDefault(gram3, 0) != 0) {
                        segs.add(new LocatedWord(gram3, start + i, start + i + 3));
                    }
                }
            }
            segs.add(new LocatedWord(word, start, start + width));
            start += width;
        }
        return segs;
        /*  for w in self.cut(unicode_sentence, HMM=HMM):
                width = len(w)
                if len(w) > 2:
                    for i in xrange(len(w) - 1):
                        gram2 = w[i:i + 2]
                        if self.FREQ.get(gram2):
                            yield (gram2, start + i, start + i + 2)
                if len(w) > 3:
                    for i in xrange(len(w) - 2):
                        gram3 = w[i:i + 3]
                        if self.FREQ.get(gram3):
                            yield (gram3, start + i, start + i + 3)
                yield (w, start, start + width)
                start += width
         */
    }

    public List<LocatedWord> cutForSearchWithIndex(String sentence) {
        return cutForSearchWithIndex(sentence, true);
    }

    public List<LocatedWord> cutForSearchWithIndex_noHMM(String sentence) {
        return cutForSearchWithIndex(sentence, false);
    }

    public int getFreq(String word) {
        return dict.getFreqs().getOrDefault(word, -1);
    }

    public synchronized void addWord(String word, int freq) {
        initialize();
        dict.addWord(word, freq);
    }

    public synchronized void addWord(String word) {
        initialize();
        dict.addWord(word, suggestFreqForJoin(word));
    }

    public synchronized void delWord(String word) {
        initialize();
        addWord(word, 0);
    }
    /*
    def suggest_freq(self, segment, tune=False):
        """
        Suggest word frequency to force the characters in a word to be
        joined or splitted.

        Parameter:
            - segment : The segments that the word is expected to be cut into,
                        If the word should be treated as a whole, use a str.
            - tune : If True, tune the word frequency.

        Note that HMM may affect the final struct. If the struct doesn't change,
        set HMM=False.
        """
        self.check_initialized()
        ftotal = float(self.total)
        freq = 1
        if isinstance(segment, string_types):
            word = segment
            for seg in self.cut(word, HMM=False):
                freq *= self.FREQ.get(seg, 1) / ftotal
            freq = max(int(freq * self.total) + 1, self.FREQ.get(word, 1))
        else:
            segment = tuple(map(strdecode, segment))
            word = ''.join(segment)
            for seg in segment:
                freq *= self.FREQ.get(seg, 1) / ftotal
            freq = min(int(freq * self.total), self.FREQ.get(word, 0))
        if tune:
            add_word(word, freq)
        return freq
     */
    public int suggestFreqForJoin(String word) {
        initialize();
        Map<String, Integer> freqs = dict.getFreqs();
        double dTotal = dict.getTotal();
        double dFreq = 1.0D;
        int freq;
        double factor;
        for (String seg : cut_noHMM(word)) {
            dFreq *= (double)freqs.getOrDefault(seg, 1) / dTotal;
        }
        freq = Math.max((int)(dFreq*dTotal)+1, freqs.getOrDefault(word, 1));
        return freq;
    }

    public int suggestFreqForSplit(String... segs) {
        initialize();
        Map<String, Integer> freqs = dict.getFreqs();
        double dTotal = dict.getTotal();
        double dFreq = 1.0D;
        int freq;
        for (String seg: segs) {
            dFreq *= (double)freqs.getOrDefault(seg, 1) / dTotal;
        }
        String word = String.join("", segs);
        freq = Math.min((int)(dFreq*dTotal), freqs.getOrDefault(word, 0));
        return freq;
    }

    public synchronized int forcedSplit(String... segs) {
        int freq = suggestFreqForSplit(segs);
        String word = String.join("", segs);
        dict.addWord(word, freq);
        return freq;
    }

    public synchronized int forcedJoin(String word) {
        int freq = suggestFreqForJoin(word);
        dict.addWord(word, freq);
        return freq;
    }
}
