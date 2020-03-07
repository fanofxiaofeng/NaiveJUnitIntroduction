package com.study.junit;

import com.study.junit.test.Child;
import com.study.junit.test.NaiveTest;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        Executors.newSingleThreadExecutor();
        Result result = JUnitCore.runClasses(Child.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure);
        }
    }
}
