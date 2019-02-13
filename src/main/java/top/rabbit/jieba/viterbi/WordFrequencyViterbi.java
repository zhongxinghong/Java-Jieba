package top.rabbit.jieba.viterbi;

import top.rabbit.jieba.util.RegexUtils;
import top.rabbit.jieba.util.CharUtils;
import top.rabbit.jieba.struct.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;

public final class WordFrequencyViterbi {

    private static final Double MIN_FLOAT = -3.14e100D;
    private static final Double MIN_INF = Double.NEGATIVE_INFINITY;

    private static final Map<Character, Character[]>prevStatus = new HashMap<Character, Character[]>(){{
        put('B', new Character[]{'E','S'});
        put('M', new Character[]{'M','B'});
        put('S', new Character[]{'S','E'});
        put('E', new Character[]{'B','M'});
    }};
    private static final String PROB_EMIT_TXT = "/prob_emit.wf.txt";
    private static final String PROB_EMIT_GZ = "/prob_emit.wf.gz";

    private static final Set<String> forceSplitWords = new HashSet<String>();

    //private static Viterbi outInstance = null;

    private WordFrequencyViterbi() {} // NO INSTANCE !

    /*public static synchronized Viterbi getInstance() {
        if (outInstance == null) {
            outInstance = new Viterbi();
        }
        return outInstance;
    }*/

    public static void initialize() {}; // 调用空函数，引发静态资源加载

    private static final Map<Character, Double> startP = new HashMap<Character, Double>() {{
        put('B', -0.26268660809250016D);
        put('E', -3.14e+100D);
        put('M', -3.14e+100D);
        put('S', -1.4652633398537678D);
    }};

    private static final Map<Character, Map<Character, Double>> transP = new HashMap<Character, Map<Character, Double>>() {{
        put('B', new HashMap<Character, Double>() {{
            put('E', -0.51082562376599D);
            put('M', -0.916290731874155D);
        }});
        put('E', new HashMap<Character, Double>() {{
            put('B', -0.5897149736854513D);
            put('S', -0.8085250474669937D);
        }});
        put('M', new HashMap<Character, Double>() {{
            put('E', -0.33344856811948514D);
            put('M', -1.2603623820268226D);
        }});
        put('S', new HashMap<Character, Double>() {{
            put('B', -0.7211965654669841D);
            put('S', -0.6658631448798212D);
        }});
    }};

    private static final Map<Character, Map<Character, Double>> emitP = loadProbEmit();

    private static synchronized Map<Character, Map<Character, Double>> loadProbEmit() {
        Map<Character, Map<Character, Double>> emitP = new HashMap<Character, Map<Character, Double>>();
        try {
            long st = System.currentTimeMillis();
            Path p = Paths.get(PROB_EMIT_GZ);
            System.out.print(String.format("Load model '%s' ... ", p.getFileName()));

            BufferedReader fin = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    WordFrequencyViterbi.class.getResourceAsStream(PROB_EMIT_GZ))));
            boolean findNextKey = true;
            String line;
            String[] kv;
            Map<Character, Double> thisV = new HashMap<Character, Double>();
            while (fin.ready()) {
                line = fin.readLine();
                if (findNextKey) {
                    if (!line.isEmpty()) {
                        findNextKey = false;
                        thisV = new HashMap<Character, Double>();
                        emitP.put(line.charAt(0), thisV);
                    }
                } else {
                    if (line.isEmpty()) {
                        findNextKey = true;
                    } else {
                        kv = line.split(" ");
                        thisV.put(kv[0].charAt(0), Double.valueOf(kv[1]));
                    }
                }
            }
            fin.close();
            long et = System.currentTimeMillis();
            System.out.println(String.format("Done! cost %s ms", et - st));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return emitP;
    }

    public static Map<Character, Map<Character, Double>> getEmitP() {
        return emitP;
    }

    public static boolean addForceSplitWord(String word) {
        synchronized (WordFrequencyViterbi.class) {
            return forceSplitWords.add(word);
        }
    }

    public static boolean delForceSplitWord(String word) {
        synchronized (WordFrequencyViterbi.class) {
            return forceSplitWords.remove(word);
        }
    }

    private static Pair<Double, List<Character>> viterbi(String obs, Character[] states) {
        List<Map<Character, Double>> V = new ArrayList<Map<Character, Double>>();
        V.add(new HashMap<Character, Double>());

        Map<Character, List<Character>> path = new HashMap<Character, List<Character>>();
        Map<Character, List<Character>> newPath = new HashMap<Character, List<Character>>();

        double emP, prob, thisProb;
        Character state;

        for (Character y : states) {
            V.get(0).put(y, startP.get(y) + emitP.get(y).getOrDefault(obs.charAt(0), MIN_FLOAT));
            path.put(y, new ArrayList<Character>(){{ add(y); }});
        }
        for (int t = 1; t < obs.length(); ++t) {
            V.add(new HashMap<Character, Double>());
            newPath = new HashMap<Character, List<Character>>();
            for (Character y : states) {
                emP = emitP.get(y).getOrDefault(obs.charAt(t), MIN_FLOAT);
                prob = MIN_INF; // maxProb
                state = null; // maxState
                for (Character y0 : prevStatus.get(y)) {
                    thisProb = V.get(t-1).get(y0) + transP.get(y0).getOrDefault(y, MIN_FLOAT) + emP;
                    if (thisProb >= prob) {
                        prob = thisProb;
                        state = y0;
                    } else if (thisProb == prob && y0 > state) {
                        state = y0;
                    }
                }
                V.get(t).put(y, prob);
                newPath.put(y, new ArrayList<Character>(path.get(state)) {{ add(y); }});
            }
            path = newPath;
        }
        prob = MIN_INF;
        state = null;
        for (Character y : new Character[]{'E','S'}) {
            thisProb = V.get(obs.length()-1).get(y);
            if (thisProb >= prob) {
                prob = thisProb;
                state = y;
            }
        }
        return new Pair<Double, List<Character>>(prob, path.get(state));
        /*
        def viterbi(obs, states, start_p, trans_p, emit_p):
            V = [{}]  # tabular
            path = {}
            for y in states:  # init
                V[0][y] = start_p[y] + emit_p[y].get(obs[0], MIN_FLOAT)
                path[y] = [y]
            for t in xrange(1, len(obs)):
                V.append({})
                newpath = {}
                for y in states:
                    em_p = emit_p[y].get(obs[t], MIN_FLOAT)
                    (prob, state) = max(
                        [(V[t - 1][y0] + trans_p[y0].get(y, MIN_FLOAT) + em_p, y0) for y0 in PrevStatus[y]])
                    V[t][y] = prob
                    newpath[y] = path[state] + [y]
                path = newpath

            (prob, state) = max((V[len(obs) - 1][y], y) for y in 'ES')

            return (prob, path[state])
         */
    }

    private static List<String> _cut(String sentence) {
        List<String> segs = new ArrayList<String>();
        Pair<Double, List<Character>> resP = viterbi(sentence, new Character[]{'B','M','E','S'});
        Double prob = resP.a;
        List<Character> posList = resP.b;
        int begin = 0, next_i = 0;
        char ch;
        for (int i = 0; i < sentence.length(); ++i) {
            switch (posList.get(i)) {
                case 'B':
                    begin = i;
                    break;
                case 'E':
                    segs.add(sentence.substring(begin, i+1));
                    next_i = i + 1;
                    break;
                case 'S':
                    segs.add(sentence.substring(i, i+1));
                    next_i = i + 1;
                    break;
                default:
                    break;
            }
        }
        if (next_i < sentence.length()) {
            segs.add(sentence.substring(next_i));
        }
        return segs;
        /*
        def __cut(sentence):
            global emit_P
            prob, pos_list = viterbi(sentence, 'BMES', start_P, trans_P, emit_P)
            begin, nexti = 0, 0
            # print pos_list, sentence
            for i, char in enumerate(sentence):
                pos = pos_list[i]
                if pos == 'B':
                    begin = i
                elif pos == 'E':
                    yield sentence[begin:i + 1]
                    nexti = i + 1
                elif pos == 'S':
                    yield char
                    nexti = i + 1
            if nexti < len(sentence):
                yield sentence[nexti:]
         */
    }

    public static List<String> cut(String sentence) {
        // decode sentence
        List<String> segs = new ArrayList<String>();
        for (String blk : RegexUtils.split(RegexUtils.reHan_viterbi, sentence)) {
            if (CharUtils.isChinese(blk)) {
                for (String word : _cut(blk)) {
                    if (!forceSplitWords.contains(word)) {
                        segs.add(word);
                    } else {
                        for (char c : word.toCharArray()) {
                            segs.add(String.valueOf(c));
                        }
                    }
                }
            } else {
                for (String x : RegexUtils.split(RegexUtils.reSkip_viterbi, blk)) {
                    if (!CharUtils.isSpace(x)) {
                        segs.add(x);
                    }
                }
            }
        }
        return segs;
        /*
        def cut(sentence):
            sentence = strdecode(sentence)
            blocks = re_han.split(sentence)
            for blk in blocks:
                if re_han.match(blk):
                    for word in __cut(blk):
                        if word not in Force_Split_Words:
                            yield word
                        else:
                            for c in word:
                                yield c
                else:
                    tmp = re_skip.split(blk)
                    for x in tmp:
                        if x:
                            yield x
         */
    }
}
