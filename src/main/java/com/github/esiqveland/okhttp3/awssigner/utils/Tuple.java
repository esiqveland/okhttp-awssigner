package com.github.esiqveland.okhttp3.awssigner.utils;

public class Tuple {

    public static <T1,T2> Tuple2<T1,T2> of(T1 first, T2 second) {
            return new Tuple2<>(first, second);
        }

}
