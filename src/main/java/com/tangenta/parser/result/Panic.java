package com.tangenta.parser.result;

public class Panic extends RuntimeException {
    private Panic(String errMsg) {
        super(errMsg);
    }

    public static Panic msg(String errMsg) {
        return new Panic(errMsg);
    }
}
