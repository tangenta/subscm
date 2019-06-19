package com.tangenta;

import com.tangenta.common.Expr;
import com.tangenta.parser.Parser;

public class Strategy {
    private static Parser programParser = Parser.program();

    public final String name;
    public final Expr code;

    private Strategy(String name, Expr code) {
        this.name = name;
        this.code = code;
    }

    public static StrategyBuilder builder() {
        return new StrategyBuilder();
    }

    public static class StrategyBuilder {
        private String name;
        private Expr code;

        private StrategyBuilder() {}

        public StrategyBuilder name(String name) {
            this.name = name;
            return this;
        }

        public StrategyBuilder code(String exp) {
            code = programParser.parseAndUnwrap(exp);
            return this;
        }

        public Strategy build() {
            if (code == null) {
                throw new RuntimeException("Strategy is not fully build: code");
            }
            return new Strategy(name, code);
        }
    }

    @Override
    public String toString() {
        return "Strategy{" +
                "name='" + name + '\'' +
                '}';
    }
}
