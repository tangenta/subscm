package com.tangenta.util;

import com.tangenta.Strategy;
import com.tangenta.common.Val;
import com.tangenta.parser.result.Panic;
import com.tangenta.common.Expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Util {
    public static String trimHead(String origin) {
        for (int i = 0; i < origin.length(); i++) {
            char x = origin.charAt(i);
            if (Character.isWhitespace(x)) continue;
            return origin.substring(i);
        }
        return "";
    }

    public static <T> T car(List<T> lst) {
        if (lst.isEmpty()) throw Panic.msg("Car on empty list");
        return lst.get(0);
    }

    public static <T> List<T> cdr(List<T> lst) {
        if (lst.isEmpty()) throw Panic.msg("Cdr on empty list");
        return lst.stream().skip(1).collect(Collectors.toList());
    }

    public static Expr.Sym carSym(List<Expr> lst) {
        if (lst.isEmpty()) throw Panic.msg("CarSymStr on empty list");
        Expr expr = lst.get(0);
        if (expr instanceof Expr.Sym) {
            return (Expr.Sym) expr;
        } else {
            throw Panic.msg("Expect symbol, actual: " + expr.getClass());
        }
    }

    public static String format(Expr result) {
        if (result instanceof Expr.Bool) {
            return ((Expr.Bool) result).value.toString();
        } else if (result instanceof Expr.Sym) {
            return ((Expr.Sym) result).value;
        } else if (result instanceof Expr.Num) {
            return ((Expr.Num) result).value.toString();
        } else if (result instanceof Expr.Lst) {
            return ((Expr.Lst) result).value.stream()
                    .map(Util::format)
                    .collect(Collectors.joining(", ", "(", ")"));
        } else if (result instanceof Expr.ManySym) {
            return "manySym";
        } else if (result instanceof Expr.Nil) {
            return "nil";
        } else {
            return "unknown";
        }
    }

    public static String format(List<Tuple<Strategy, Integer>> results) {
        return format(
                map(results, tup -> Arrays.asList(tup.left.name, tup.right.toString())),
                Arrays.asList("Strategy", "Score")
        );
    }

    public static String format(List<List<String>> content, List<String> headers) {
        // validation
        if (!content.stream().allMatch(l -> l.size() == headers.size())) {
            throw new IllegalArgumentException("Malformed arguments");
        }

        List<Integer> maxLens = content.stream().map(Util::toLength)
                .reduce(Util.toLength(headers), Util::maxList);
        List<Integer> rMaxLens = map(maxLens, l -> l + 2);

        return String.join("\n", new LinkedList<String>() {{
            String splitL = splitLine(rMaxLens);
            add(splitL);
            add(contentLine(headers, rMaxLens));
            add(splitL);
            addAll(map(content, line -> contentLine(line, rMaxLens)));
            add(splitL);
        }});
    }

    private static String splitLine(List<Integer> lens) {
        return wrapAround("+", lens.stream()
                .map(len -> many('-', len))
                .collect(Collectors.joining("+")));
    }

    public static void main(String[] args) {
        String s = contentLine(Arrays.asList("abc", "12", "4"), Arrays.asList(8, 5, 2));
        System.out.println(s);
    }

    private static String contentLine(List<String> contents, List<Integer> maxLens) {
        return wrapAround("|", zip(contents, maxLens).stream().map(tup -> {
            String con = tup.left;
            Integer len = tup.right;

            int bp = (len - con.length()) / 2;
            int ap = (len - con.length() - bp);

            return many(' ', bp) + con + many(' ', ap);
        }).collect(Collectors.joining("|")));
    }

    private static String wrapAround(String content, String body) {
        return content + body + content;
    }

    private static String many(char x, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(x);
        }
        return builder.toString();
    }

    private static <T, U> List<Tuple<T, U>> zip(List<T> first, List<U> second) {
        int size = Integer.min(first.size(), second.size());
        List<Tuple<T, U>> res = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            res.add(Tuple.of(first.get(i), second.get(i)));
        }
        return res;
    }

    private static List<Integer> maxList(List<Integer> first, List<Integer> second) {
        int size = Integer.min(first.size(), second.size());
        List<Integer> res = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            res.add(Integer.max(first.get(i), second.get(i)));
        }
        return res;
    }

    private static List<Integer> toLength(List<String> strs) {
        return strs.stream().map(String::length).collect(Collectors.toList());
    }

    private static <T, R> List<R> map(List<T> origin, Function<T, R> fn) {
        return origin.stream().map(fn).collect(Collectors.toList());
    }
}
