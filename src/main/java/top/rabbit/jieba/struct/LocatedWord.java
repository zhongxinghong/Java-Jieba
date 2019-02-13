package top.rabbit.jieba.struct;

import java.util.Objects;

public final class LocatedWord {

    public final String word;
    public final int start;
    public final int end;

    public LocatedWord(String word, int start, int end) {
        this.word = word;
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return String.format("(%s, %d, %d)", word, start, end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, start, end);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LocatedWord)) {
            throw new ClassCastException();
        } else {
            LocatedWord that = (LocatedWord)o;
            return this.word.equals(that.word)
                    && (this.start == that.start)
                    && (this.end == that.end);
        }
    }
}
