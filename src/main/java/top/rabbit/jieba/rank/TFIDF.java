package top.rabbit.jieba.rank;

import top.rabbit.jieba.struct.Keyword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

public final class TFIDF extends KeywordExtractor<String> {

    public static final String STD_IDF_DICT_TXT = "/idf.std.txt";
    public static final String STD_IDF_DICT_GZ = "/idf.std.gz";
    public static final String DEFAULT_DICT = STD_IDF_DICT_GZ;

    private final String IDF_DICT;
    private final String STOP_WORDS_FILE;
    private Map<String, Double> freqs = null;
    private double median;

    public TFIDF(String IDF_DICT, String STOP_WORDS_FILE) {
        // super(EXTENDED_STOP_WORDS_TXT, STOP_WORDS_FILE); // 默认添加 extended
        super(); // 先初始化默认停词表
        this.IDF_DICT = IDF_DICT;
        this.STOP_WORDS_FILE = STOP_WORDS_FILE;
    }

    public TFIDF(String IDF_DICT) {
        // super(EXTENDED_STOP_WORDS_TXT); // 默认添加 extended
        super();
        this.IDF_DICT = IDF_DICT;
        this.STOP_WORDS_FILE = null;
    }

    public TFIDF() {
        this(DEFAULT_DICT);
    }

    private boolean isInitialized() {
        return (freqs != null);
    }

    public synchronized void initialize() {
        if ( isInitialized() ) return;
        freqs = new HashMap<String, Double>();
        loadIDFDict();
        if (STOP_WORDS_FILE != null) {
            loadStopWords(STOP_WORDS_FILE);
        }
    }

    private synchronized void loadIDFDict() {
        try {
            String line;
            String[] kv;
            long st = System.currentTimeMillis();
            Path p = Paths.get(IDF_DICT);
            System.out.print(String.format("Load IDF dict '%s' ... ", p.getFileName()));
            BufferedReader fin;
            if (IDF_DICT.endsWith(".gz")) {
                fin = new BufferedReader(new InputStreamReader(
                        new GZIPInputStream(this.getClass().getResourceAsStream(IDF_DICT))));
            } else {
                fin = new BufferedReader(new InputStreamReader(
                        this.getClass().getResourceAsStream(IDF_DICT)));
            }
            while (fin.ready()) {
                line = fin.readLine().trim();
                if (!line.isEmpty()) {
                    kv = line.split(" ");
                    freqs.put(kv[0], Double.valueOf(kv[1]));
                }
            }
            fin.close();
            double[] sortedVals = freqs.entrySet().stream()
                    .mapToDouble(x -> x.getValue()).sorted().toArray();
            median = sortedVals[freqs.size() / 2];
            long et = System.currentTimeMillis();
            System.out.println(String.format("Done! cost %s ms", et-st));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<Keyword> extract(List<String> words, int topK) {
        initialize();
        Map<String, Double> freq = new HashMap<String, Double>();
        for (String w : words) {
            if ((w.length() < 2) || stopWords.contains(w.toLowerCase())) {
                continue;
            }
            freq.put(w, freq.getOrDefault(w, 0.0) + 1.0);
        }
        double total = freq.entrySet().stream().mapToDouble(x->x.getValue()).sum();
        for (String k : freq.keySet()) {
            freq.put(k, freq.get(k) * freqs.getOrDefault(k, median) / total);
        }
        List<Map.Entry<String, Double>> sortedFreq = freq.entrySet().stream()
                .sorted((x1, x2)-> Double.compare(x2.getValue(),x1.getValue()) )
                .collect(Collectors.toList());
        return IntStream.range(0, freq.size()) // 引入 rank
                .filter(x-> ( (topK <= 0) || (x < topK) )) // topK <= 0 则不过滤
                .mapToObj(x->{
                    Map.Entry<String, Double> item = sortedFreq.get(x);
                    return new Keyword(item.getKey(), item.getValue(), x + 1);
                }).collect(Collectors.toList());
        /*
        def extract_tags(self, sentence, topK=20, withWeight=False, allowPOS=(), withFlag=False):
            if allowPOS:
                allowPOS = frozenset(allowPOS)
                words = self.postokenizer.cut(sentence)
            else:
                words = self.tokenizer.cut(sentence)
            freq = {}
            for w in words:
                if allowPOS:
                    if w.flag not in allowPOS:
                        continue
                    elif not withFlag:
                        w = w.word
                wc = w.word if allowPOS and withFlag else w
                if len(wc.strip()) < 2 or wc.lower() in self.stop_words:
                    continue
                freq[w] = freq.get(w, 0.0) + 1.0
            total = sum(freq.values())
            for k in freq:
                kw = k.word if allowPOS and withFlag else k
                freq[k] *= self.idf_freq.get(kw, self.median_idf) / total

            if withWeight:
                tags = sorted(freq.items(), key=itemgetter(1), reverse=True)
            else:
                tags = sorted(freq, key=freq.__getitem__, reverse=True)
            if topK:
                return tags[:topK]
            else:
                return tags
         */
    };
}
