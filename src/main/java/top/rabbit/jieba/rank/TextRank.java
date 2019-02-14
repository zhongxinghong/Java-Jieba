package top.rabbit.jieba.rank;

import top.rabbit.jieba.struct.Keyword;
import top.rabbit.jieba.struct.Pair;
import top.rabbit.jieba.struct.TaggedWord;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TextRank extends KeywordExtractor<TaggedWord> {

    private static final List<String> DEFAULT_ALLOWED_POS =
            new ArrayList<String>(Arrays.asList("ns", "n", "vn", "v"));
    private static final int span = 5;

    public TextRank(String STOP_WORDS_FILE) {
        super();
        loadStopWords(STOP_WORDS_FILE);
    }

    public TextRank() {
        super();
    }

    private boolean filter(TaggedWord tw, List<String> posList) {
        return (posList.contains(tw.flag))
                && (tw.word.trim().length() >= 2)
                && (!stopWords.contains(tw.word.toLowerCase()));
        /*
        def pairfilter(self, wp):
            return (wp.flag in self.pos_filt and len(wp.word.strip()) >= 2
                    and wp.word.lower() not in self.stop_words)
         */
    }

    public List<Keyword> extract(List<TaggedWord> words, int topK, List<String> posList) {
        UndirectWeightedGraph<TaggedWord> g = new UndirectWeightedGraph<TaggedWord>();
        Map<Pair<TaggedWord, TaggedWord>, Integer> cm = new HashMap<Pair<TaggedWord, TaggedWord>, Integer>();
        Pair<TaggedWord, TaggedWord> p;
        for (int i = 0; i < words.size(); ++i) {
            TaggedWord tw = words.get(i);
            if (filter(tw, posList)) {
                for (int j = i + 1; j < i + span; ++j) {
                    if (j >= words.size()) {
                        break;
                    }
                    if (!filter(words.get(j), posList)) {
                        continue;
                    }
                    p = new Pair<TaggedWord, TaggedWord>(tw, words.get(j));
                    cm.put(p, cm.getOrDefault(p, 0) + 1);
                }
            }
        }
        for (Map.Entry<Pair<TaggedWord, TaggedWord>, Integer> entry : cm.entrySet()) {
            p = entry.getKey();
            g.addEdge(p.a, p.b, entry.getValue());
        }
        Map<TaggedWord, Double> nodesRank = g.rank();
        List<Map.Entry<TaggedWord, Double>> sortedNodesRank = nodesRank.entrySet().stream()
                .sorted((e1,e2)-> Double.compare(e2.getValue(),e1.getValue()) )
                .collect(Collectors.toList());
        return IntStream.range(0, nodesRank.size())
                .filter(x-> ( (topK <= 0) || (x < topK) )) // topK <= 0 则不过滤
                .mapToObj(x->{
                    Map.Entry<TaggedWord,Double> item = sortedNodesRank.get(x);
                    return new Keyword(item.getKey(), item.getValue(), x + 1);
                }).collect(Collectors.toList()); // 还需考虑词性
        /*
        def textrank(self, sentence, topK=20, withWeight=False, allowPOS=('ns', 'n', 'vn', 'v'), withFlag=False):
            self.pos_filt = frozenset(allowPOS)
            g = UndirectWeightedGraph()
            cm = defaultdict(int)
            words = tuple(self.tokenizer.cut(sentence))
            for i, wp in enumerate(words):
                if self.pairfilter(wp):
                    for j in xrange(i + 1, i + self.span):
                        if j >= len(words):
                            break
                        if not self.pairfilter(words[j]):
                            continue
                        if allowPOS and withFlag:
                            cm[(wp, words[j])] += 1
                        else:
                            cm[(wp.word, words[j].word)] += 1

            for terms, w in cm.items():
                g.addEdge(terms[0], terms[1], w)
            nodes_rank = g.rank()
            if withWeight:
                tags = sorted(nodes_rank.items(), key=itemgetter(1), reverse=True)
            else:
                tags = sorted(nodes_rank, key=nodes_rank.__getitem__, reverse=True)

            if topK:
                return tags[:topK]
            else:
                return tags
             */
    }

    @Override
    public List<Keyword> extract(List<TaggedWord> words, int topK) {
        return extract(words, topK, DEFAULT_ALLOWED_POS);
    }

    @Override
    public List<Keyword> extract(List<TaggedWord> words) {
        return extract(words, DEFAULT_TOPK);
    }
}
