package com.tangenta.evaluator;

public final class Predefine {
    private Predefine() {}


    public static String forall() {
        return "(define forall (lambda (lst v)\n" +
                "  (if (eq? lst (list))\n" +
                "    true\n" +
                "    (and (eq? (car lst) v) (forall (cdr lst) v))\n" +
                "  )\n" +
                "))";
    }

    public static String len() {
        return "(define len (lambda (lst) \n" +
                "  (if (eq? lst (list))\n" +
                "    0\n" +
                "    (+ 1 (len (cdr lst))))))";
    }

    public static String last() {
        return "(define last (lambda (lst)\n" +
                "  (if (eq? (cdr lst) (list))\n" +
                "    (car lst)\n" +
                "    (last (cdr lst))\n" +
                "  )))";
    }
}
