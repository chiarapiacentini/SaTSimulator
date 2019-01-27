package main;

import java.io.Serializable;

public class Pair<A, B> implements Serializable {
    public A first;
    public B second;

    public Pair(A a, B b) {
        first = a;
        second = b;
    }
}