package com.tangenta.parser.result;

import com.tangenta.common.Expr;

public class ParsePending implements ParseResult {
    public final Expr.ManySym manySym;
    public final String restStr;

    private ParsePending(Expr.ManySym manySym, String restStr) {
        this.manySym = manySym;
        this.restStr = restStr;
    }

    public static ParsePending of(Expr.ManySym manySym, String restStr) {
        return new ParsePending(manySym, restStr);
    }
}
