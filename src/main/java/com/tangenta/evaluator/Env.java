package com.tangenta.evaluator;

import com.tangenta.parser.result.Panic;
import com.tangenta.common.Expr;
import com.tangenta.common.Val;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

// Env is an key-value environment when evaluating Expr.
public class Env {
    private final Map<Expr.Sym, Val> env;

    private Env(Map<Expr.Sym, Val> env) {
        this.env = env;
    }

    // set() is used to set a key-value pair.
    public void set(Expr.Sym sym, Val v) {
        env.put(sym, v);
    }

    // get() is used to get a key-value from environment.
    public Val get(Expr.Sym sym) {
        return env.get(sym);
    }

    // standard() defined basic operators and functions for valuating, such as `random`, `car`, 'cdr', etc.
    public static Env standard() {
        Random random = new Random();

        return new Env(new HashMap<Expr.Sym, Val>(){{
            put(new Expr.Sym("+"), new Val.Func(args -> args.stream().map(v -> {
                if (!(v instanceof Val.Int)) throw Panic.msg("+ should be use on integer");
                return ((Val.Int) v).value;
            }).reduce(Integer::sum).map(Val.Int::new).orElseThrow(() -> {
                return Panic.msg("+ should apply to at lease 1 arguments");
            })));

            put(new Expr.Sym("*"), new Val.Func(args -> args.stream().map(v -> {
                if (!(v instanceof Val.Int)) throw Panic.msg("* should be use on integer");
                return ((Val.Int) v).value;
            }).reduce((i1, i2) -> i1 * i2).map(Val.Int::new).orElseThrow(() -> {
                return Panic.msg("* should apply to at lease 1 arguments");
            })));

            put(new Expr.Sym("-"), new Val.Func(args -> args.stream().map(v -> {
                if (!(v instanceof Val.Int)) throw Panic.msg("- should be use on integer");
                Integer ret = ((Val.Int) v).value;
                return args.size() == 1 ? -ret : ret;
            }).reduce((i1, i2) -> i1 - i2).map(Val.Int::new).orElseThrow(() -> {
                return Panic.msg("- should apply to at lease 1 arguments");
            })));

            put(new Expr.Sym("<"), new Val.Func(args -> {
                if (args.size() != 2 || !args.stream().allMatch(e -> e instanceof Val.Int))
                    throw Panic.msg("< can only compare two int");
                Integer first = ((Val.Int) args.get(0)).value;
                Integer second = ((Val.Int) args.get(1)).value;
                return new Val.Bool(first < second);
            }));

            put(new Expr.Sym("random"), new Val.Func(args -> {
                if (args.size() != 1 || !(args.get(0) instanceof Val.Int)) {
                    throw Panic.msg("random can only be applied to 1 int");
                }
                Integer max = ((Val.Int) args.get(0)).value;
                return new Val.Int(random.nextInt(max));
            }));

            put(new Expr.Sym("eq?"), new Val.Func(args -> {
                if (args.size() != 2) {
                    throw Panic.msg("eq? can only be applied to 2 symbols");
                }
                Val left = args.get(0);
                Val right = args.get(1);
                if (left instanceof Val.Int && right instanceof Val.Int) {
                    return new Val.Bool(((Val.Int) left).value.equals(((Val.Int) right).value));
                } else if (left instanceof Val.Bool && right instanceof Val.Bool) {
                    return new Val.Bool(((Val.Bool) left).value.equals(((Val.Bool) right).value));
                } else if (left instanceof Val.Lst && right instanceof Val.Lst) {
                    return new Val.Bool(((Val.Lst) left).value.equals(((Val.Lst) right).value));
                } else return new Val.Bool(false);
            }));

            put(new Expr.Sym("car"), new Val.Func(args -> {
                if (args.size() != 1 || !(args.get(0) instanceof Val.Lst)) {
                    throw Panic.msg("Car must apply to one list");
                }
                List<Val> lstVals = ((Val.Lst) args.get(0)).value;
                if (lstVals.isEmpty()) {
                    throw Panic.msg("car on empty list");
                }
                return lstVals.get(0);
            }));

            put(new Expr.Sym("cdr"), new Val.Func(args -> {
                if (args.size() != 1 || !(args.get(0) instanceof Val.Lst)) {
                    throw Panic.msg("Cdr must apply to one list");
                }
                List<Val> lstVals = ((Val.Lst) args.get(0)).value;
                if (lstVals.isEmpty()) {
                    throw Panic.msg("cdr on empty list");
                }
                return new Val.Lst(lstVals.stream().skip(1).collect(Collectors.toList()));
            }));

            put(new Expr.Sym("and"), new Val.Func(args ->
                    new Val.Bool(args.stream().map(arg -> {
                        if (!(arg instanceof Val.Bool)) {
                            throw Panic.msg("And must be applied to booleans");
                        }
                        return ((Val.Bool) arg).value;
                    }).reduce(true, (a, b) -> a && b))));

            put(new Expr.Sym("or"), new Val.Func(args ->
                    new Val.Bool(args.stream().map(arg -> {
                        if (!(arg instanceof Val.Bool)) {
                            throw Panic.msg("Or must be applied to booleans");
                        }
                        return ((Val.Bool) arg).value;
                    }).reduce(false, (a, b) -> a || b))));

        }});
    }

    // create() create an environment from a given map.
    public static Env create(Map<Expr.Sym, Val> env) {
        return new Env(env);
    }

    // expand() create an environment with the old one adding more kv pairs.
    public static Env expand(Env old, Map<Expr.Sym, Val> newEnv) {
        return new Env(new HashMap<Expr.Sym, Val>(){{
            putAll(old.env);
            putAll(newEnv);
        }});
    }
}
