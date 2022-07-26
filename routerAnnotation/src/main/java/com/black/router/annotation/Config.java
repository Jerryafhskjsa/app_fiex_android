package com.black.router.annotation;

public class Config {
    public Class clz;
    public String beforePath;
    public String fragmentParentPath;
    public int fragmentIndex;

    public Config(Class clz, String beforePath, String fragmentParentPath, int fragmentIndex) {
        this.clz = clz;
        this.beforePath = beforePath;
        this.fragmentParentPath = fragmentParentPath;
        this.fragmentIndex = fragmentIndex;
    }
}
