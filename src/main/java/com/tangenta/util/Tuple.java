package com.tangenta.util;

// Tuple is a tuple of two values.
public class Tuple<L, R> {
    public final L left;
    public final R right;

    private Tuple(L left, R right) {
        this.left = left;
        this.right = right;
    }

    // of() create a tuple.
    public static <L, R> Tuple<L, R> of(L left, R right) {
        return new Tuple<>(left, right);
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}
