package top.rabbit.jieba.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public final class POSDict extends Dict {

    private final Map<String, String> wordTagTab = new HashMap<String, String>();

    public POSDict(String DICT) {
        loadDict(DICT);
    }

    public POSDict() {
        this(STD_WORD_DICT_TXT);
    }

    public Map<String, String> getWordTagTab() {
        return wordTagTab;
    }

    public synchronized void addWord(String word, String tag) {
        wordTagTab.put(word, tag); // 未对 tag 做合理性校验
    }

    public synchronized void delWord(String word) {
        wordTagTab.remove(word);
    }

    private synchronized void loadDict(String DICT) {
        if (DICT.equals(NONE_DICT)) return;
        try {
            String line;
            String[] items;
            String word;
            String tag;

            long st = System.currentTimeMillis();
            Path p = Paths.get(DICT);
            System.out.print(String.format("Load POS dict '%s' ... ", p.getFileName()));
            BufferedReader fin;
            if (DICT.endsWith(".gz")) {
                fin = new BufferedReader(new InputStreamReader(
                        new GZIPInputStream(this.getClass().getResourceAsStream(DICT))));
            } else {
                fin = new BufferedReader(new InputStreamReader(
                        this.getClass().getResourceAsStream(DICT)));
            }
            while (fin.ready()) {
                line = fin.readLine().trim();
                if (!line.isEmpty()) {
                    items = line.trim().split(" ");
                    word = items[0];
                    tag = items[2];
                    wordTagTab.put(word, tag);
                }
            }
            fin.close();
            long et = System.currentTimeMillis();
            System.out.println(String.format("Done! cost %s ms", et-st));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
