package com.black.util;

//筛选器
public interface Filter<T> {
    boolean filter(T obj);
}
