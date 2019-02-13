package top.rabbit.jieba.struct;

import java.util.Objects;


/**
 * An immutable pair of values.  The values may be null.  The values
 * themselves may be mutable.
 *
 * @param <A> the type of the first element of the pair
 * @param <B> the type of the second element of the pair
 *
 * @since 1.7
 * @see   <a href="http://mail.openjdk.java.net/pipermail/core-libs-dev/2010-March/003988.html">java.util.Pair</a>
 */
public final class Pair<A, B> {

    public final A a;
    public final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    /**
     * TBD
     */
    @Override
    public String toString() {
        return "(" + Objects.toString(a) + ", " + Objects.toString(b) + ")";
    }

    /**
     * TBD
     */
    @Override
    public boolean equals(Object x) {
        if (!(x instanceof Pair))
            return false;
        else {
            Pair<?,?> that = (Pair<?,?>) x;
            return Objects.equals(this.a, that.a) && Objects.equals(this.b, that.b);
        }
    }

    /**
     * TBD
     */
    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }
}
