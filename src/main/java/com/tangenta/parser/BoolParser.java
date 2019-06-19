package com.tangenta.parser;

import com.tangenta.parser.result.ParseError;
import com.tangenta.parser.result.ParseResult;
import com.tangenta.parser.result.ParseSuccess;
import com.tangenta.common.Expr;
import com.tangenta.util.Util;

public class BoolParser implements Parser {
    private BoolParser() {}

    @Override
    public ParseResult parse(String string) {
        String trimStr = Util.trimHead(string);
        int len = trimStr.length();

        if (len >= 4 && trimStr.substring(0, 4).equalsIgnoreCase("true")) {
            return ParseSuccess.of(new Expr.Bool(true), trimStr.substring(4));
        } else if (len >= 5 && trimStr.substring(0, 5).equalsIgnoreCase("false")) {
            return ParseSuccess.of(new Expr.Bool(false), trimStr.substring(5));
        } else {
            return ParseError.of("Unable to parse bool: " + string);
        }
    }

    public static BoolParser build() {
        return new BoolParser();
    }
}
