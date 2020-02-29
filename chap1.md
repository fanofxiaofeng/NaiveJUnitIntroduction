# 第一回 初窥门径

经过几次简单的跳转后, 
就会来到 `JUnitCore` 类的 `run(Computer computer, Class<?>... classes)` 方法(如下图红框所示).
![IDEA 中的截图](pic/outline.png)
这个方法里只有如下的一行代码
```java
return run(Request.classes(computer, classes));
```
所以这个方法做的事情是如下两件
1. 调用 `Request` 类中的 `classes(Computer computer, Class<?>... classes)` 方法
2. 调用 `JUnitCore` 类中的 `run(Request request)` 方法

可以猜测一下 `JUnit` 运作的原理应该是先解析要测试的类,然后再执行测试.
上面的第`1`步中会解析要测试的类,
上面的第`2`步中会执行测试.

到这里,`JUnit` 最外层的逻辑就看完了,是不是并不难呢?
不过只理解到这种程度,肯定是不够用的,我们继续看这两个步骤的内部逻辑.
为了便于描述,就把上述的两个步骤分别称作
`Request.classes(...)` 和 `JUnitCore.run(...)` 吧.

欲知 `Request.classes(...)` 中细节如何,且听[下回](chap2.md)分解

[第二回 组合模式](chap2.md)