package com.study.junit.test;

import org.junit.Test;

public class Parent {

    protected void showCallerInfo() {
        System.out.println(new Throwable().getStackTrace()[1]);
    }

    @Test
    public void f() {
        showCallerInfo();
    }

    @Test
    public void g() {
        showCallerInfo();
    }
}
