package com.github.linwancen.plugin.graph.ui;

public interface TriConsumer<K, V, S> {
    void accept(K k, V v, S s);
}
