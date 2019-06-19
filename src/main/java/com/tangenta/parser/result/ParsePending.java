package com.tangenta.parser.result;

import com.tangenta.common.Expr;

// ParsePending is neither a success nor an error, but an intermediate state.
public class ParsePending implements ParseResult {
    public final Expr.ManySym manySym;
    public final String restStr;

    private ParsePending(Expr.ManySym manySym, String restStr) {
        this.manySym = manySym;
        this.restStr = restStr;
    }

    // of() create a ParsePending.
    public static ParsePending of(Expr.ManySym manySym, String restStr) {
        return new ParsePending(manySym, restStr);
    }
}
