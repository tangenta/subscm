package com.tangenta.common;

import java.util.List;
import java.util.function.Function;

// Val is a value after evaluating an Expr.
public interface Val {

    // Func represents a function.
    class Func implements Val {
        public final Function<List<Val>, Val> value;

        public Func(Function<List<Val>, Val> value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Func{" +
                    "value=" + value +
                    '}';
        }
    }

    // Int represents an integer.
    class Int implements Val {
        public final Integer value;

        public Int(Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Int{" +
                    "value=" + value +
                    '}';
        }
    }

    // Bool represents a boolean.
    class Bool implements Val {
        public final Boolean value;

        public Bool(Boolean value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Bool{" +
                    "value=" + value +
                    '}';
        }
    }

    // Def is a definition result in a Expr.
    class Def implements Val {
        public final Expr.Sym value;

        public Def(Expr.Sym value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Def{" +
                    "value=" + value +
                    '}';
        }
    }

    // Lst represents a list of Val.
    class Lst implements Val {
        public final List<Val> value;

        public Lst(List<Val> value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Lst{" +
                    "value=" + value +
                    '}';
        }
    }

    // Nil is a empty Val.
    class Nil implements Val {
        private Nil() {}
    }
    Nil nil = new Nil();
}
