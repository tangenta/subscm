package com.tangenta.common;

import java.util.List;
import java.util.Objects;

public interface Expr {
    class Sym implements Expr {
        public final String value;

        public Sym(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Sym{" +
                    "value='" + value + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Sym sym = (Sym) o;
            return Objects.equals(value, sym.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    class ManySym implements Expr {
        public final List<Expr> value;

        public ManySym(List<Expr> value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "ManySym{" +
                    "value=" + value +
                    '}';
        }
    }

    class Lst implements Expr {
        public final java.util.List<Expr> value;

        public Lst(java.util.List<Expr> value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Lst{" +
                    "value=" + value +
                    '}';
        }
    }

    class Num implements Expr {
        public final Integer value;

        public Num(Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Num{" +
                    "value=" + value +
                    '}';
        }
    }

    class Nil implements Expr {
        private Nil() {}

        @Override
        public String toString() {
            return "Nil";
        }
    }

    Expr nil = new Nil();

    class Bool implements Expr {
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

}
