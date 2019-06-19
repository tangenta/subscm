package com.tangenta.common;

import java.util.List;
import java.util.function.Function;

public interface Val {
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

    class Nil implements Val {
        private Nil() {}
    }
    Nil nil = new Nil();
}
