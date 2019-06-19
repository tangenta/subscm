package com.tangenta.parser;

import com.tangenta.parser.result.ParseError;
import com.tangenta.parser.result.ParseResult;
import com.tangenta.parser.result.ParseSuccess;
import com.tangenta.common.Expr;
import com.tangenta.util.Util;

// IntParser is used to parse a integer.
public class IntParser implements Parser {
    private IntParser() {}

    @Override
    public ParseResult parse(String string) {
        String trimString = Util.trimHead(string);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < trimString.length(); i++) {
            char target = string.charAt(i);
            if (Character.isDigit(target)) {
                builder.append(target);
            } else {
                break;
            }
        }

        String result = builder.toString();
        if (result.isEmpty()) return ParseError.of("Unable to parse int: " + string);
        return ParseSuccess.of(new Expr.Num(Integer.parseInt(result)), trimString.substring(result.length()));
    }

    // build() create IntParser.
    public static IntParser build() {
        return new IntParser();
    }
}
