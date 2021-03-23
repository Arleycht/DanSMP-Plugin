package io.github.arleycht.SMP.util;

import java.util.Objects;

public class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof Pair) {
            Pair pair = (Pair) o;

            return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
        }

        return false;
    }
}
