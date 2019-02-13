package top.rabbit.jieba.rank;

import top.rabbit.jieba.struct.Keyword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public abstract class KeywordExtractor <T> {

    private static final int DEFAULT_TOPK = 20;
    public static final String DEFAULT_STOP_WORDS_TXT = "/stopwords.default.txt";
    public static final String EXTENDED_STOP_WORDS_TXT = "/stopwords.extended.txt";

    protected Set<String> stopWords = new HashSet<String>();

    protected KeywordExtractor() {
        loadStopWords(DEFAULT_STOP_WORDS_TXT);
    };

    protected KeywordExtractor(String filename) {
        this();
        loadStopWords(filename);
    }

    public synchronized void loadStopWords(String filename) {
        try {
            long st = System.currentTimeMillis();
            Path p = Paths.get(filename);
            System.out.print(String.format("Load stop words from '%s' ... ", p.getFileName()));
            BufferedReader fin;
            if (filename.endsWith(".gz")) {
                fin = new BufferedReader(new InputStreamReader(
                        new GZIPInputStream(this.getClass().getResourceAsStream(filename))));
            } else {
                fin = new BufferedReader(new InputStreamReader(
                        this.getClass().getResourceAsStream(filename)));
            }
            while (fin.ready()) {
                String word = fin.readLine().trim();
                if (!word.isEmpty()) {
                    stopWords.add(word);
                }
            }
            fin.close();
            long et = System.currentTimeMillis();
            System.out.println(String.format("Done! cost %s ms", et - st));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected synchronized boolean addStopWord(String word) {
        return stopWords.add(word);
    }

    protected synchronized boolean addStopWords(String... words) {
        return stopWords.addAll(Arrays.asList(words));
    }

    protected synchronized boolean addStopWords(List<String> words) {
        return stopWords.addAll(words);
    }

    protected synchronized boolean delStopWord(String word) {
        return stopWords.remove(word);
    }

    public List<Keyword> extract(List<T> words) {
        return extract(words, DEFAULT_TOPK);
    };

    abstract public List<Keyword> extract(List<T> words, int topK);
}
