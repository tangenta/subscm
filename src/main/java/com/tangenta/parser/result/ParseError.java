package com.tangenta.parser.result;

public class ParseError implements ParseResult {
    public final String errMsg;

    private ParseError(String errMsg) {
        this.errMsg = errMsg;
    }

    public static ParseError of(String errMsg) {
        return new ParseError(errMsg);
    }
}
