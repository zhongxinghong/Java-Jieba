package top.rabbit.jieba.rank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @param <T> 必须重写 hashCode, equals 方法，以确保 HashMap 键的唯一性，必须实现 Comparable<T> 接口，使之能够进行排序
 */
final class UndirectWeightedGraph<T> {

    private class Edge {
        final T start;
        final T end;
        int weight;

        Edge(T start, T end, int weight) {
            this.start = start;
            this.end = end;
            this.weight = weight;
        }
    }

    private static final double d = 0.85D;

    private final Map<T, List<Edge>> graph = new HashMap<T, List<Edge>>();

    void addEdge(T start, T end, int weight) {
        Edge e;
        List<Edge> lst;
        e = new Edge(start, end, weight);
        if (!graph.containsKey(start)) {
            lst = new ArrayList<Edge>();
            lst.add(e);
            graph.put(start, lst);
        } else {
            graph.get(start).add(e);
        }

        e = new Edge(end, start, weight);
        if (!graph.containsKey(end)) {
            lst = new ArrayList<Edge>();
            lst.add(e);
            graph.put(end, lst);
        } else {
            graph.get(end).add(e);
        }
    }

    Map<T, Double> rank() {
        Map<T, Double> ws = new HashMap<T, Double>();
        Map<T, Double> outSum = new HashMap<T, Double>();
        double wsdef = (graph.isEmpty()) ? 1.0D : ( 1.0D / (double)graph.size() );
        for (Map.Entry<T, List<Edge>> entry : graph.entrySet()) {
            T n = entry.getKey();
            ws.put(n, wsdef);
            outSum.put(n, entry.getValue().stream().mapToDouble(e -> e.weight).sum());
        }
        List<T> sortedKeys = graph.keySet().stream().sorted().collect(Collectors.toList());
        for (int x = 0; x < 10; ++x) {
            for (T n : sortedKeys) {
                double s = 0.0D;
                for (Edge e : graph.get(n)) {
                    s += (double) e.weight / outSum.get(e.end) * ws.get(e.end);
                }
                ws.put(n, ((1 - d) + d * s ));
            }
        }
        double minRank = Double.MAX_VALUE;
        double maxRank = Double.MIN_NORMAL;
        for (double w : ws.values()) {
            if (w < minRank) {
                minRank = w;
            }
            if (w > maxRank) {
                maxRank = w;
            }
        }
        for (Map.Entry<T, Double> entry : ws.entrySet()) {
            T n = entry.getKey();
            double w = entry.getValue();
            ws.put(n, ((w - minRank / 10.0D ) / (maxRank - minRank / 10.0)) );
        }
        return ws;
    }
    /*
    def rank(self):
        ws = defaultdict(float)
        outSum = defaultdict(float)

        wsdef = 1.0 / (len(self.graph) or 1.0)
        for n, out in self.graph.items():
            ws[n] = wsdef
            outSum[n] = sum((e[2] for e in out), 0.0)

        # this line for build stable iteration
        sorted_keys = sorted(self.graph.keys())
        for x in xrange(10):  # 10 iters
            for n in sorted_keys:
                s = 0
                for e in self.graph[n]:
                    s += e[2] / outSum[e[1]] * ws[e[1]]
                ws[n] = (1 - self.d) + self.d * s

        (min_rank, max_rank) = (sys.float_info[0], sys.float_info[3])

        for w in itervalues(ws):
            if w < min_rank:
                min_rank = w
            if w > max_rank:
                max_rank = w

        for n, w in ws.items():
            # to unify the weights, don't *100.
            ws[n] = (w - min_rank / 10.0) / (max_rank - min_rank / 10.0)

        return ws
     */

}

