package com.study.junit.test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class NaiveTest {

    @Before
    public void b() {
        printCallerInfo();
    }

    @Test
    public void t() {
        printCallerInfo();
    }

    @BeforeClass
    public static void bc() {
        printCallerInfo();
    }

    private static void printCallerInfo() {
        System.out.println(new Throwable().getStackTrace()[1]);
    }
}
