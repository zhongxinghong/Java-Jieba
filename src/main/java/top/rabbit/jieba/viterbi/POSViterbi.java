package top.rabbit.jieba.viterbi;

import top.rabbit.jieba.struct.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public final class POSViterbi {

    private static final Double MIN_FLOAT = -3.14e100D;
    private static final Double MIN_INF = Double.NEGATIVE_INFINITY;

    private static final String PROB_START_TXT = "/prob_start.pos.txt";
    private static final String PROB_START_GZ = "/prob_start.pos.gz";
    private static final String PROB_TRANS_TXT = "/prob_trans.pos.txt";
    private static final String PROB_TRANS_GZ = "/prob_trans.pos.gz";
    private static final String PROB_EMIT_TXT = "/prob_emit.pos.txt";
    private static final String PROB_EMIT_GZ = "/prob_emit.pos.gz";
    private static final String CHAR_STATE_TAB_TXT = "/char_state_tab.pos.txt";
    private static final String CHAR_STATE_TAB_GZ = "/char_state_tab.pos.gz";
    private static final String NULL_VALUE = "NULL";

    private static final Map<Pair<String, String>, Double> startP = loadProbStart();
    private static final Map<Pair<String, String>, Map<Pair<String, String>, Double>> transP = loadProbTrans();
    private static final Map<Pair<String, String>, Map<Character, Double>> emitP = loadProbEmit();
    private static final Map<Character, List<Pair<String, String>>> states = loadStates();

    public static void initialize() {}; // 调用空函数，引发静态资源加载

    private static synchronized Map<Pair<String, String>, Double> loadProbStart() {
        Map<Pair<String, String>, Double> startP = new HashMap<Pair<String, String>, Double>();
        try {
            long st = System.currentTimeMillis();
            Path p = Paths.get(PROB_START_GZ);
            System.out.print(String.format("Load model '%s' ... ", p.getFileName()));

            BufferedReader fin = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    POSViterbi.class.getResourceAsStream(PROB_START_GZ))));
            String line;
            String[] items;
            while (fin.ready()) {
                line = fin.readLine();
                items = line.split(" ");
                startP.put(new Pair<String, String>(items[0], items[1]), Double.valueOf(items[2]));
            }
            fin.close();
            long et = System.currentTimeMillis();
            System.out.println(String.format("Done! cost %s ms", et - st));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return startP;
    }

    private static synchronized Map<Pair<String, String>, Map<Pair<String, String>, Double>> loadProbTrans() {
        Map<Pair<String, String>, Map<Pair<String, String>, Double>> transP =
                new HashMap<Pair<String, String>, Map<Pair<String, String>, Double>>();
        try {
            long st = System.currentTimeMillis();
            Path p = Paths.get(PROB_TRANS_GZ);
            System.out.print(String.format("Load model '%s' ... ", p.getFileName()));

            BufferedReader fin = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    POSViterbi.class.getResourceAsStream(PROB_TRANS_GZ))));
            boolean findNextKey = true;
            String line;
            String[] items;
            Pair<String, String> k1;
            Map<Pair<String, String>, Double> thisV = new HashMap<Pair<String, String>, Double>();
            while (fin.ready()) {
                line = fin.readLine();
                if (findNextKey) {
                    if (!line.isEmpty()) {
                        findNextKey = false;
                        items = line.split(" ");
                        k1 = new Pair<String, String>(items[0], items[1]);
                        thisV = new HashMap<Pair<String, String>, Double>();
                        transP.put(k1, thisV);
                    }
                } else {
                    if (line.isEmpty() || line.equals(NULL_VALUE)) {
                        findNextKey = true;
                    } else {
                        items = line.split(" ");
                        thisV.put(new Pair<String, String>(items[0], items[1]), Double.valueOf(items[2]));
                    }
                }
            }
            fin.close();
            long et = System.currentTimeMillis();
            System.out.println(String.format("Done! cost %s ms", et - st));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return transP;
    }

    private static synchronized Map<Pair<String, String>, Map<Character, Double>> loadProbEmit() {
        Map<Pair<String, String>, Map<Character, Double>> emitP = new HashMap<Pair<String, String>, Map<Character, Double>>();
        try {
            long st = System.currentTimeMillis();
            Path p = Paths.get(PROB_EMIT_GZ);
            System.out.print(String.format("Load model '%s' ... ", p.getFileName()));

            BufferedReader fin = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    POSViterbi.class.getResourceAsStream(PROB_EMIT_GZ))));
            boolean findNextKey = true;
            String line;
            String[] items;
            Pair<String, String> k1;
            Map<Character, Double> thisV = new HashMap<Character, Double>();
            while (fin.ready()) {
                line = fin.readLine();
                if (findNextKey) {
                    if (!line.isEmpty()) {
                        findNextKey = false;
                        items = line.split(" ");
                        k1 = new Pair<String, String>(items[0], items[1]);
                        thisV = new HashMap<Character, Double>();
                        emitP.put(k1, thisV);
                    }
                } else {
                    if (line.isEmpty()) {
                        findNextKey = true;
                    } else {
                        items = line.split(" ");
                        thisV.put(items[0].charAt(0), Double.valueOf(items[1]));
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

    private static synchronized Map<Character, List<Pair<String, String>>> loadStates() {
        Map<Character, List<Pair<String, String>>> states = new HashMap<Character, List<Pair<String, String>>>();
        try {
            long st = System.currentTimeMillis();
            Path p = Paths.get(CHAR_STATE_TAB_GZ);
            System.out.print(String.format("Load model '%s' ... ", p.getFileName()));

            BufferedReader fin = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    POSViterbi.class.getResourceAsStream(CHAR_STATE_TAB_GZ))));
            boolean findNextKey = true;
            String line;
            String[] items;
            Character k;
            List<Pair<String, String>> thisV = new ArrayList<Pair<String, String>>();
            while (fin.ready()) {
                line = fin.readLine();
                if (findNextKey) {
                    if (!line.isEmpty()) {
                        findNextKey = false;
                        k = line.charAt(0);
                        thisV = new ArrayList<Pair<String, String>>();
                        states.put(k, thisV);
                    }
                } else {
                    if (line.isEmpty()) {
                        findNextKey = true;
                    } else {
                        items = line.split(" ");
                        thisV.add(new Pair<String, String>(items[0], items[1]));
                    }
                }
            }
            fin.close();
            long et = System.currentTimeMillis();
            System.out.println(String.format("Done! cost %s ms", et - st));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return states;
    }

    public static Map<Pair<String, String>, Double> getStartP() {
        return startP;
    }

    public static Map<Pair<String, String>, Map<Pair<String, String>, Double>> getTransP() {
        return transP;
    }

    public static Map<Pair<String, String>, Map<Character, Double>> getEmitP() {
        return emitP;
    }

    public static Map<Character, List<Pair<String, String>>> getStates() {
        return states;
    }

    public static Pair<Double, List<Pair<String, String>>> viterbi(String obs) {
        final Set<Pair<String, String>> allStates = transP.keySet();
        List<Map<Pair<String, String>, Double>> V = new ArrayList<Map<Pair<String, String>, Double>>() {{}};
        V.add(new HashMap<Pair<String, String>, Double>());
        List<Map<Pair<String, String>, Pair<String, String>>> memPath =
                new ArrayList<Map<Pair<String, String>, Pair<String, String>>>();
        memPath.add(new HashMap<Pair<String, String>, Pair<String, String>>());
        List<Pair<String, String>> prevStates;
        Set<Pair<String, String>> prevStatesExpectNext;
        Set<Pair<String, String>> obsStates;
        double prob, thisProb;
        Pair<String, String> state;
        int _a;

        for (Pair<String, String> y : allStates) {
            V.get(0).put(y, startP.get(y) + emitP.get(y).getOrDefault(obs.charAt(0), MIN_FLOAT));
            memPath.get(0).put(y, null); // null 替换 ""
        }
        for (int t = 1; t < obs.length(); ++t) {
            V.add(new HashMap<Pair<String, String>, Double>());
            memPath.add(new HashMap<Pair<String, String>, Pair<String, String>>());
            prevStates = memPath.get(t-1).keySet().stream()
                    .filter(x -> (transP.get(x).size() > 0))
                    .collect(Collectors.toList());
            prevStatesExpectNext = new HashSet<Pair<String, String>>();
            for (Pair<String, String> x : prevStates) {
                prevStatesExpectNext.addAll(transP.get(x).keySet());
            }
            obsStates = new HashSet<Pair<String, String>>(
                    (states.containsKey(obs.charAt(t))) ?
                            states.get(obs.charAt(t)) : allStates);
            obsStates.retainAll(prevStatesExpectNext);
            if (obsStates.size() == 0) {
                obsStates = (prevStatesExpectNext.size() > 0) ? prevStatesExpectNext : allStates;
            }
            for (Pair<String,String> y : obsStates) {
                prob = MIN_INF;
                state = null;
                for (Pair<String, String> y0 : prevStates) {
                    thisProb = V.get(t-1).get(y0) + transP.get(y0).getOrDefault(y, MIN_INF) +
                            emitP.get(y).getOrDefault(obs.charAt(t), MIN_FLOAT);
                    if (thisProb >= prob) {
                        prob = thisProb;
                        state = y0;
                    } else if (thisProb == prob) {
                        _a = y0.a.compareTo(state.a); // y0 > state ?
                        if (_a > 0) {
                            state = y0;
                        } else if (_a == 0 && (y0.b.compareTo(state.b) > 0) ) {
                            state = y0;
                        }
                    }
                }
                V.get(t).put(y, prob);
                memPath.get(t).put(y, state);
            }
        }
        prob = MIN_INF;
        state = null;
        for (Pair<String, String> y : memPath.get(memPath.size()-1).keySet()) {
            thisProb = V.get(V.size()-1).get(y);
            if (thisProb >= prob) {
                prob = thisProb;
                state = y;
            } else if (thisProb == prob) {
                _a = y.a.compareTo(state.a); // y > state ?
                if (_a > 0) {
                    state = y;
                } else if (_a == 0 && (y.b.compareTo(state.b) > 0) ) {
                    state = y;
                }
            }
        }
        List<Pair<String, String>> route = new ArrayList<Pair<String, String>>(obs.length());
        for (int i = 0; i < obs.length(); ++i) {
            route.add(i, null);
        }
        for (int i = obs.length() - 1; i >= 0; --i) {
            route.set(i, state);
            state = memPath.get(i).get(state);
        }
        return new Pair<Double, List<Pair<String, String>>>(prob, route);
    }
    /*
    def viterbi(obs, states, start_p, trans_p, emit_p):
        V = [{}]  # tabular
        mem_path = [{}]
        all_states = trans_p.keys()
        for y in states.get(obs[0], all_states):  # init
            V[0][y] = start_p[y] + emit_p[y].get(obs[0], MIN_FLOAT)
            mem_path[0][y] = ''
        for t in xrange(1, len(obs)):
            V.append({})
            mem_path.append({})
            #prev_states = get_top_states(V[t-1])
            prev_states = [
                x for x in mem_path[t - 1].keys() if len(trans_p[x]) > 0]

            prev_states_expect_next = set(
                (y for x in prev_states for y in trans_p[x].keys()))
            obs_states = set(
                states.get(obs[t], all_states)) & prev_states_expect_next

            if not obs_states:
                obs_states = prev_states_expect_next if prev_states_expect_next else all_states

            for y in obs_states:
                prob, state = max((V[t - 1][y0] + trans_p[y0].get(y, MIN_INF) +
                                   emit_p[y].get(obs[t], MIN_FLOAT), y0) for y0 in prev_states)
                V[t][y] = prob
                mem_path[t][y] = state

        last = [(V[-1][y], y) for y in mem_path[-1].keys()]
        # if len(last)==0:
        #     print obs
        prob, state = max(last)

        route = [None] * len(obs)
        i = len(obs) - 1
        while i >= 0:
            route[i] = state
            state = mem_path[i][state]
            i -= 1
        return (prob, route)
     */
}
