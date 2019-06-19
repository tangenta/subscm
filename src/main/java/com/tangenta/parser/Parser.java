package com.tangenta.parser;

import com.tangenta.parser.result.*;
import com.tangenta.common.Expr;
import com.tangenta.util.Util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface Parser {
    ParseResult parse(String string);
    default Expr parseAndUnwrap(String string) {
        ParseResult parseResult = parse(string);
        if (parseResult instanceof ParseSuccess) {
            return ((ParseSuccess) parseResult).expr;
        } else if (parseResult instanceof ParsePending) {
            throw Panic.msg("Parse error");
        } else {
            throw Panic.msg(((ParseError)parseResult).errMsg);
        }
    }

    static Parser of(String match) {
        return string -> {
            String trim = Util.trimHead(string);
            return trim.contains(match) && trim.substring(0, match.length()).equals(match) ?
                    ParseSuccess.of(new Expr.Sym(match), string.substring(match.length())) :
                    ParseError.of("Not match " + match + " in: " + trim);
        };
    }

    static Parser ignore(Parser parser) {
        return string -> {
            ParseResult originRes = parser.parse(string);
            return originRes instanceof ParseSuccess ?
                    ParseSuccess.of(Expr.nil, ((ParseSuccess) originRes).restStr) : originRes;
        };
    }

    static Parser program() {
        return string -> {
            ParseResult result = new ManyParser(Parser::expression).parse(string);
            if (result instanceof ParseError) return result;
            else if (result instanceof ParsePending) {
                return ParseSuccess.of(((ParsePending) result).manySym, ((ParsePending) result).restStr);
            } else {
                throw Panic.msg("Program should be consist of expressions");
            }
        };
    }

    static Parser expression() {
        return alter(new ArrayList<Supplier<Parser>>(){{
            add(IntParser::build);
            add(BoolParser::build);
            add(IdParser::build);
            add(Parser::sList);
        }});
    }

    static Parser sList() {
        return seq(new ArrayList<Supplier<Parser>>(){{
            add(() -> ignore(of("(")));
            add(() -> new ManyParser(Parser::expression));
            add(() -> ignore(of(")")));
        }});
    }

    static Parser alter(List<Supplier<Parser>> parsers) {
        return new AlterParser(parsers);
    }

    static Parser seq(List<Supplier<Parser>> parsers) {
        return new SeqParser(parsers);
    }

    class AlterParser implements Parser {
        private List<Supplier<Parser>> parsers;

        private AlterParser(List<Supplier<Parser>> parsers) {
            this.parsers = parsers;
        }

        @Override
        public ParseResult parse(String string) {
            List<ParseResult> parseResults = parsers.stream().map(p -> p.get().parse(string))
                    .filter(res -> res instanceof ParseSuccess)
                    .collect(Collectors.toList());
            if (parseResults.isEmpty()) return ParseError.of("Unable to parse: " + string);
            return parseResults.get(0);
        }
    }

    class SeqParser implements Parser {
        private List<Supplier<Parser>> parsers;

        public SeqParser(List<Supplier<Parser>> parsers) {
            this.parsers = parsers;
        }

        @Override
        public ParseResult parse(String string) {
            String restString = string;
            List<Expr> results = new LinkedList<>();
            for (Supplier<Parser> p : parsers) {
                ParseResult parseResult = p.get().parse(Util.trimHead(restString));
                if (parseResult instanceof ParseError) {
                    return parseResult;
                } else if (parseResult instanceof ParsePending) {
                    results.addAll(((ParsePending) parseResult).manySym.value);
                    restString = ((ParsePending) parseResult).restStr;
                } else {
                    ParseSuccess success = (ParseSuccess) parseResult;
                    if (!(success.expr instanceof Expr.Nil)) results.add(success.expr);
                    restString = success.restStr;
                }
            }
            return ParseSuccess.of(new Expr.Lst(results), restString);
        }
    }

    class ManyParser implements Parser {
        private final Supplier<Parser> parser;

        public ManyParser(Supplier<Parser> parser) {
            this.parser = parser;
        }

        @Override
        public ParseResult parse(String string) {
            List<Expr> manySym = new LinkedList<>();

            ParseResult lv;
            String restStr = string;
            Parser p = parser.get();
            while (((lv = p.parse(Util.trimHead(restStr))) instanceof ParseSuccess)) {
                ParseSuccess success = (ParseSuccess)lv;
                manySym.add(success.expr);
                restStr = success.restStr;
            }

            if (manySym.isEmpty()) return ParseSuccess.of(Expr.nil, string);
            else return ParsePending.of(new Expr.ManySym(manySym), restStr);
        }
    }
}
