package com.tangenta.evaluator;

import com.tangenta.parser.result.Panic;
import com.tangenta.common.Expr;
import com.tangenta.common.Val;
import com.tangenta.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Eval {
    static Val eval(Expr origin, Env env) {
        if (origin instanceof Expr.Sym) {
            Val val = env.get(((Expr.Sym) origin));
            if (val == null) {
                throw Panic.msg("Unresolved symbol: " + ((Expr.Sym) origin).value);
            }
            return val;
        } else if (origin instanceof Expr.Bool) {
            return new Val.Bool(((Expr.Bool) origin).value);
        } else if (origin instanceof Expr.Num) {
            return new Val.Int(((Expr.Num) origin).value);
        } else if (origin instanceof Expr.ManySym) {
            List<Expr> exps = ((Expr.ManySym) origin).value;
            if (exps.isEmpty()) throw Panic.msg("Eval on empty program");

            return exps.stream().map(e -> Eval.eval(e, env)).reduce(Val.nil, (__, v) -> v);
        } else if (origin instanceof Expr.Lst) {
            List<Expr> exps = ((Expr.Lst) origin).value;
            if (exps.isEmpty()) throw Panic.msg("Evaluate on empty: ()");

            Expr.Sym head = Util.carSym(exps);
            List<Expr> tail = Util.cdr(exps);

            switch (head.value) {
                case "define": {
                    Expr.Sym definedSym = Util.carSym(tail);
                    List<Expr> body = Util.cdr(tail);
                    if (body.size() != 1) throw Panic.msg("Define body should contains exactly one expression");
                    env.set(definedSym, eval(body.get(0), env));
                    return new Val.Def(definedSym);
                }

                case "lambda": {
                    Expr paramExp = Util.car(tail);
                    if (!(paramExp instanceof Expr.Lst)) throw Panic.msg("Expect a argument list");

                    List<Expr> body = Util.cdr(tail);
                    if (body.size() != 1) throw Panic.msg("Lambda body should contains exactly one expression");
                    Expr bodyExp = body.get(0);

                    List<Expr.Sym> params = ((Expr.Lst) paramExp).value.stream().map(p -> {
                        if (!(p instanceof Expr.Sym)) throw Panic.msg("parameters should be symbols: " + p);
                        return (Expr.Sym) p;
                    }).collect(Collectors.toList());

                    return new Val.Func(listVal -> eval(bodyExp, Env.expand(env, new HashMap<Expr.Sym, Val>() {{
                        if (params.size() != listVal.size()) throw Panic.msg("Arguments num != parameter num");
                        for (int i = 0; i < listVal.size(); i++) {
                            put(params.get(i), listVal.get(i));
                        }
                    }})));
                }

                case "if": {
                    if (tail.size() != 3) throw Panic.msg("if expression should contains pred, then and else");
                    Expr pred = tail.get(0);
                    Expr then = tail.get(1);
                    Expr els = tail.get(2);

                    Val predResult = eval(pred, env);
                    if (!(predResult instanceof Val.Bool)) throw Panic.msg("pred should be a boolean expression");
                    if (((Val.Bool) predResult).value) {
                        return eval(then, env);
                    } else {
                        return eval(els, env);
                    }
                }

                case "list": {
                    return new Val.Lst(tail.stream().map(p -> eval(p, env)).collect(Collectors.toList()));
                }

                // procedure call
                default: {
                    Val fnVal = env.get(head);
                    if (!(fnVal instanceof Val.Func)) throw Panic.msg("Unresolved function name " + head.value);
                    Function<List<Val>, Val> fn = ((Val.Func) fnVal).value;
                    List<Val> argList = tail.stream().map(arg -> eval(arg, env)).collect(Collectors.toList());
                    return fn.apply(argList);
                }
            }
        } else {
            throw Panic.msg("Unknown type");
        }
    }
}
