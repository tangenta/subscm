package com.tangenta;

import com.tangenta.common.Expr;
import com.tangenta.common.Val;
import com.tangenta.evaluator.Env;
import com.tangenta.evaluator.Eval;
import com.tangenta.evaluator.Predefine;
import com.tangenta.parser.Parser;
import com.tangenta.parser.result.Panic;
import com.tangenta.util.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class Strategy {
    private static Parser programParser = Parser.program();
    private static Parser expParser = Parser.expression();

    public final String name;
    public final Expr code;

    private Strategy(String name, Expr code) {
        this.name = name;
        this.code = code;
    }

    public static StrategyBuilder builder() {
        return new StrategyBuilder();
    }

    public static List<Tuple<Strategy, Integer>> sortStrategies(List<Strategy> strategies, int rounds) {
        Map<Strategy, Integer> scoreMap = new HashMap<>();

        Env env = Env.standard();
        for (String exp : new String[] { Predefine.len(), Predefine.last(), Predefine.forall() }) {
            exec(exp, env);
        }

        for (int i = 0; i < strategies.size(); i++) {
            for (int j = i + 1; j < strategies.size(); j++) {
                Strategy leftStg = strategies.get(i);
                Strategy rightStg = strategies.get(j);

                Tuple<Integer, Integer> score = startDuel(leftStg.code, rightStg.code, env, rounds);
                scoreMap.compute(leftStg, (stg, sco) -> sco == null ? score.left : sco + score.left);
                scoreMap.compute(rightStg, (stg, sco) -> sco == null ? score.right : sco + score.right);
            }
        }

        return scoreMap.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                .map(e -> Tuple.of(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public static Tuple<Integer, Integer> startDuel(Expr exp1, Expr exp2, Env env, int rounds) {
        Val.Lst own = new Val.Lst(new LinkedList<>());
        Val.Lst other = new Val.Lst(new LinkedList<>());
        int score1 = 0;
        int score2 = 0;

        Env duelEnv = Env.expand(env, new HashMap<Expr.Sym, Val>(){{
            put(new Expr.Sym("own"), own);
            put(new Expr.Sym("other"), other);
        }});

        for (int i = 0; i < rounds; i++) {
            Val eval1 = Eval.eval(exp1, duelEnv);
            Val eval2 = Eval.eval(exp2, duelEnv);

            if (!(eval1 instanceof Val.Int)) throw Panic.msg("Duel expression should return int");
            Integer res1 = ((Val.Int) eval1).value;

            if (!(eval2 instanceof Val.Int)) throw Panic.msg("Duel expression should return int");
            Integer res2 = ((Val.Int) eval2).value;

            if (res1 > 1 || res1 < 0 || res2 > 1 || res2 < 0) {
                throw Panic.msg("Dual expression should return 0 or 1");
            }

            own.value.add(new Val.Int(res1));
            own.value.add(new Val.Int(res2));

            if (res1 == 0) {
                if (res2 == 0) {
                    score1 += 1;
                    score2 += 1;
                } else {
                    score1 += 5;
                    score2 += 0;
                }
            } else {
                if (res2 == 0) {
                    score1 += 0;
                    score2 += 5;
                } else {
                    score1 += 3;
                    score2 += 3;
                }
            }
        }
        return Tuple.of(score1, score2);
    }

    public static void exec(String code, Env env) {
        Eval.eval(expParser.parseAndUnwrap(code), env);
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
