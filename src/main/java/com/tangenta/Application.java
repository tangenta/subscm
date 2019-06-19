package com.tangenta;

import com.tangenta.parser.result.*;
import com.tangenta.evaluator.Env;
import com.tangenta.evaluator.Eval;
import com.tangenta.common.Expr;
import com.tangenta.common.Val;
import com.tangenta.parser.Parser;
import com.tangenta.evaluator.Predefine;
import com.tangenta.util.Tuple;
import com.tangenta.util.Util;

import java.util.*;
import java.util.stream.Collectors;

public class Application {
    private static Parser expParser = Parser.expression();

    public static void main(String[] args) {
        List<Tuple<Strategy, Integer>> rst = sortStrategies(new ArrayList<Strategy>() {{
            add(stg1());
            add(stg2());
            add(stg3());
            add(stg4());
            add(stg5());
        }}, 200);
        System.out.println(Util.format(rst));
    }

    public static Strategy stg1() {
        return Strategy.builder()
                .name("Cooperate forever")
                .code("(define stg1 1) stg1").build();
    }
    public static Strategy stg2() {
        return Strategy.builder()
                .name("Random")
                .code("(define stg2 (lambda () (if (eq? (random 4) 3) 0 1))) (stg2)").build();
    }

    public static Strategy stg3() {
        return Strategy.builder()
                .name("Tit-for-tat")
                .code("(define stg3 (lambda () \n" +
                        "  (if (eq? (len other) 0)\n" +
                        "    1\n" +
                        "    (last other))\n" +
                        "))" +
                        "(stg3)").build();
    }
    public static Strategy stg4() {
        return Strategy.builder()
                .name("Detector")
                .code("(define stg4 (lambda ()\n" +
                        "  (if (eq? (len other) 0)\n" +
                        "    1\n" +
                        "    (if (eq? (random 10) 9)\n" +
                        "      0\n" +
                        "      (last other)))\n" +
                        "))" +
                        "(stg4)").build();
    }
    public static Strategy stg5() {
        return Strategy.builder()
                .name("Not forgiving")
                .code("(define stg5 (lambda () \n" +
                        "  (if (forall other 1)\n" +
                        "    1\n" +
                        "    0)\n" +
                        "))" +
                        "(stg5)").build();
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
}
