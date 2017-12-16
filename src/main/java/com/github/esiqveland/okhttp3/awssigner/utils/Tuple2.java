package com.github.esiqveland.okhttp3.awssigner.utils;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Tuple2<T1, T2> implements Comparable<Tuple2<T1, T2>> {
        public final T1 _1;
        public final T2 _2;

        public Tuple2(T1 t1, T2 t2) {
            _1 = t1;
            _2 = t2;
        }

        public <T3> Tuple2<T3, T2> map1(Function<T1, T3> mapper) {
            return new Tuple2<>(mapper.apply(_1), _2);
        }

        public <T3> Tuple2<T1, T3> map2(Function<T2, T3> mapper) {
            return new Tuple2<>(_1, mapper.apply(_2));
        }

        public <T3> T3 apply(BiFunction<T1, T2, T3> transformer) {
            return transformer.apply(_1, _2);
        }

        @SuppressWarnings("unchecked")
        private static <U1 extends Comparable<? super U1>, U2 extends Comparable<? super U2>> int compareTo(Tuple2<?, ?> o1, Tuple2<?, ?> o2) {
            final Tuple2<U1, U2> t1 = (Tuple2<U1, U2>) o1;
            final Tuple2<U1, U2> t2 = (Tuple2<U1, U2>) o2;

            final int check1 = t1._1.compareTo(t2._1);
            if (check1 != 0) {
                return check1;
            }

            final int check2 = t1._2.compareTo(t2._2);
            if (check2 != 0) {
                return check2;
            }

            // all components are equal
            return 0;
        }

        @Override
        public int compareTo(Tuple2<T1, T2> other) {
            return Tuple2.compareTo(this, other);
        }
    }
