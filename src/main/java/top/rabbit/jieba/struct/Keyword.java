package top.rabbit.jieba.struct;

import java.util.Objects;

public final class Keyword {

    public final String word;
    public final String flag;
    public final double score;
    public final int rank;

    public Keyword(String word, String flag, double score, int rank) {
        this.word = word;
        this.flag = flag; // 允许空
        this.score = score;
        this.rank = rank;
    }

    public Keyword(String word, double score, int rank) {
        this(word, null, score, rank);
    }

    public Keyword(TaggedWord tw, double score, int rank) {
        this(tw.word, tw.flag, score, rank);
    }

    @Override
    public String toString() {
        return word;
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, flag, score, rank);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Keyword)) {
            throw new ClassCastException();
        } else {
            Keyword that = (Keyword)o;
            return this.word.equals(that.word)
                    && this.flag.equals(that.flag)
                    && (this.score == that.score)
                    && (this.rank == that.rank);

        }
    }
}
