package com.tangenta.parser;

import com.tangenta.parser.result.ParseError;
import com.tangenta.parser.result.ParseResult;
import com.tangenta.parser.result.ParseSuccess;
import com.tangenta.common.Expr;
import com.tangenta.util.Util;

public class IdParser implements Parser {
    private IdParser() {}

    @Override
    public ParseResult parse(String string) {
        String trimString = Util.trimHead(string);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < trimString.length(); i++) {
            char target = trimString.charAt(i);

            if (!Character.isWhitespace(target) && target != '(' && target != ')') {
                builder.append(target);
            } else break;
//            if (i == 0 && Character.isJavaIdentifierStart(target) || i != 0 && Character.isJavaIdentifierPart(target)) {
//                builder.append(target);
//            } else break;
        }

        String result = builder.toString();
        if (result.isEmpty())
            return ParseError.of("Cannot parse identifier in: " + trimString);
        else
            return ParseSuccess.of(new Expr.Sym(result), trimString.substring(result.length()));
    }

    public static IdParser build() {
        return new IdParser();
    }
}
