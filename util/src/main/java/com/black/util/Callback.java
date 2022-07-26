package com.black.util;

public abstract class Callback<T> extends CallbackObject<T> {
    public abstract void error(int type, Object error);
}
