package com.tangenta.parser.result;

// ParseError represents an error occurred when parsing.
public class ParseError implements ParseResult {
    public final String errMsg;

    private ParseError(String errMsg) {
        this.errMsg = errMsg;
    }

    // of() create a ParseError.
    public static ParseError of(String errMsg) {
        return new ParseError(errMsg);
    }
}
