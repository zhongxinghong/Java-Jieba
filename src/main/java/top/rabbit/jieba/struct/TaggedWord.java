package top.rabbit.jieba.struct;

import java.util.Objects;

public final class TaggedWord implements Comparable<TaggedWord> {

    public static final String FLAG_NUMERAL = "m";
    public static final String FLAG_UNKNOWN = "x";
    public static final String FLAG_ENGLISH = "eng";

    public final String word;
    public final String flag;

    public TaggedWord(String word, String flag) {
        this.word = word;
        this.flag = flag;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", word, flag);
    }

    /* 重写 hashCode 和 equals 方法，使得其在 HashMap 中的键值唯一 */
    @Override
    public int hashCode() {
        return Objects.hash(word, flag);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TaggedWord)) {
            throw new ClassCastException();
        } else {
            TaggedWord that = (TaggedWord)o;
            return this.word.equals(that.word)
                    && this.flag.equals(that.flag);
        }
    }

    @Override
    public int compareTo(TaggedWord other) {
        int i = word.compareTo(other.word);
        if (i != 0) return i;
        return flag.compareTo(other.flag);
    }
}
