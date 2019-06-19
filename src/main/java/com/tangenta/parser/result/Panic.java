package com.tangenta.parser.result;

// Panic is an exception occurred when parsing strings or evaluating Expr.
public class Panic extends RuntimeException {
    private Panic(String errMsg) {
        super(errMsg);
    }

    // msg() build a Panic with an error message.
    public static Panic msg(String errMsg) {
        return new Panic(errMsg);
    }
}
