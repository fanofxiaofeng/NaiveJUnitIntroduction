package com.study.junit;

import com.study.junit.test.NaiveTest;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class Main {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(NaiveTest.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure);
        }
    }
}
