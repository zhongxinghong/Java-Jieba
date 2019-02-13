package top.rabbit.jieba.dataset;

import top.rabbit.jieba.viterbi.WordFrequencyViterbi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public final class WordFrequencyDict extends Dict {

    private final Map<String, Integer> freqs = new HashMap<String, Integer>();
    private long total = 0L;

    public WordFrequencyDict(String DICT) {
        loadDict(DICT);
    }

    public WordFrequencyDict() {
        this(STD_WORD_DICT_TXT);
    }

    public Map<String, Integer> getFreqs() {
        return freqs;
    }

    public long getTotal() {
        return total;
    }

    public synchronized void addWord(String word, int freq) {
        if (freq < 0) {
            freq = 0;
        }
        String wFrag;

        freqs.put(word, freq);
        total += freq;

        for (int i = 0; i < word.length(); ++i) {
            wFrag = word.substring(0, i + 1);
            if (!freqs.containsKey(wFrag)) {
                freqs.put(wFrag, 0);
            }
        }

        if (freq == 0) {
            boolean flg = WordFrequencyViterbi.addForceSplitWord(word);
        }
        /*
        def add_word(self, word, freq=None, tag=None):
            """
            Add a word to dictionary.

            freq and tag can be omitted, freq defaults to be a calculated value
            that ensures the word can be cut out.
            """
            self.check_initialized()
            word = strdecode(word)
            freq = int(freq) if freq is not None else self.suggest_freq(word, False)
            self.FREQ[word] = freq
            self.total += freq
            if tag:
                self.user_word_tag_tab[word] = tag
            for ch in xrange(len(word)):
                wfrag = word[:ch + 1]
                if wfrag not in self.FREQ:
                    self.FREQ[wfrag] = 0
            if freq == 0:
                finalseg.add_force_split(word)

         */
    }

    private synchronized void loadDict(String DICT) {
        if (DICT.equals(NONE_DICT)) return;
        try {
            String line;
            String[] items;
            String word;
            int freq;
            String seg;

            long st = System.currentTimeMillis();
            Path p = Paths.get(DICT);
            System.out.print(String.format("Load word frequency dict '%s' ... ", p.getFileName()));
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
                    items = line.split(" ");
                    word = items[0];
                    freq = Integer.valueOf(items[1]);

                    freqs.put(word, freq);
                    total += (long) freq;

                    for (int i = 0; i < word.length() - 1; ++i) {
                        seg = word.substring(0, i + 1);
                        if (!freqs.containsKey(seg)) {
                            freqs.put(seg, 0);
                        }
                    }
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
