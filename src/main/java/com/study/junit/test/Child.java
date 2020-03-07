package com.study.junit.test;

import org.junit.Test;

public class Child extends Parent {
    @Test
    public void f() {
        showCallerInfo();
    }
}
