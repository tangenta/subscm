package com.tangenta.evaluator;

// Predefine define some commonly used functions.
public final class Predefine {
    private Predefine() {}

    // forall() is used to check whether all elements in a list satisfy the condition.
    public static String forall() {
        return "(define forall (lambda (lst v)\n" +
                "  (if (eq? lst (list))\n" +
                "    true\n" +
                "    (and (eq? (car lst) v) (forall (cdr lst) v))\n" +
                "  )\n" +
                "))";
    }

    // len() is used to calculate the length of a list.
    public static String len() {
        return "(define len (lambda (lst) \n" +
                "  (if (eq? lst (list))\n" +
                "    0\n" +
                "    (+ 1 (len (cdr lst))))))";
    }

    // last() is used to find the last element in a list.
    public static String last() {
        return "(define last (lambda (lst)\n" +
                "  (if (eq? (cdr lst) (list))\n" +
                "    (car lst)\n" +
                "    (last (cdr lst))\n" +
                "  )))";
    }
}
