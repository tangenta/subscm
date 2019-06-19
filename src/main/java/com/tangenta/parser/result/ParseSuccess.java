package com.tangenta.parser.result;

import com.tangenta.common.Expr;

public class ParseSuccess implements ParseResult {
    public final Expr expr;
    public final String restStr;

    private ParseSuccess(Expr expr, String restStr) {
        this.expr = expr;
        this.restStr = restStr;
    }

    public static ParseSuccess of(Expr expr, String restStr) {
        return new ParseSuccess(expr, restStr);
    }
}
