
# 第六回 组合模式大显身手

本回主要角色介绍
| 名称   | 类型 | 事迹 |
|----------|:-------------:|:------|
| **桶大侠** | `Suite` | `Runner` 全家**桶** |

上回说到要执行 **桶大侠** 的构造函数`2`.
## 构造函数`2`
构造函数`2`的逻辑如下(在 [第三回](chap3.md) 提到过)
```java
    /**
     * Called by this class and subclasses once the runners making up the suite have been determined
     *
     * @param klass root of the suite
     * @param runners for each class in the suite, a {@link Runner}
     */
    protected Suite(Class<?> klass, List<Runner> runners) throws InitializationError {
        // 1. 调用父类(即 ParentRunner)的构造函数. 注意: 构造函数1调用构造函数2时, klass 是 null
        super(klass);
        // 2. 把 runners 包装一下, 然后赋给 this.runners
        //    (直白一点说, 就是把 runners 作为子节点保存下来, 桶大侠 在这一步认领了若干个子节点)
        this.runners = Collections.unmodifiableList(runners);
    }
```
### 第`1`步
第`1`步是调用父类(`ParentRunner`)的构造函数.
父类的构造函数的内容如下
```java
    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     */
    protected ParentRunner(Class<?> testClass) throws InitializationError {
        // 被 桶大侠 调用时, testClass 参数是 null
        this.testClass = createTestClass(testClass);
        validate();
    }
```
在父类的构造函数中,
会通过调用 `createTestClass(...)` 方法生成一个 `TestClass`.
然后进行一些校验工作.
[第五回](chap5.md) 中也提到过 **皮掌门** 的构造函数的逻辑,
这里就不赘述了.
不过这次执行 **皮掌门** 的构造函数,
有一个特殊之处,
`testClass` 这个入参的值是 `null`.

### 第`2`步
第`2`步生成了一个只读的视图.
```java
    /**
     * Returns an unmodifiable view of the specified list.  This method allows
     * modules to provide users with "read-only" access to internal
     * lists.  Query operations on the returned list "read through" to the
     * specified list, and attempts to modify the returned list, whether
     * direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned list will be serializable if the specified list
     * is serializable. Similarly, the returned list will implement
     * {@link RandomAccess} if the specified list does.
     *
     * @param  <T> the class of the objects in the list
     * @param  list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified list.
     */
    public static <T> List<T> unmodifiableList(List<? extends T> list) {
        return (list instanceof RandomAccess ?
                new UnmodifiableRandomAccessList<>(list) :
                new UnmodifiableList<>(list));
    }
```

等构造函数`2`执行完,
**桶大侠** 的孩子节点也认领好了.
现在 [第二回](chap2.md) 中的第`2`步总算是执行完了.
[第二回](chap2.md) 中的三步具体如下
```java
    /**
     * Create a <code>Request</code> that, when processed, will run all the tests
     * in a set of classes.
     *
     * @param computer Helps construct Runners from classes
     * @param classes the classes containing the tests
     * @return a <code>Request</code> that will cause all tests in the classes to be run
     */
    public static Request classes(Computer computer, Class<?>... classes) {
        try {
            // 1. 创建 builder(从名字可以看出来, 后面会出现构建者模式)
            AllDefaultPossibilitiesBuilder builder = new AllDefaultPossibilitiesBuilder(true);
            // 2. 利用 builder 创建一个 Runner 的实例(实例的名称是 suite)
            Runner suite = computer.getSuite(builder, classes);
            // 3. 把第2步生成的 Runner 实例转化为 Request 类型的实例并返回
            return runner(suite);
        } catch (InitializationError e) {
            throw new RuntimeException(
                    "Bug in saff's brain: Suite constructor, called as above, should always complete");
        }
    }
```
到这里 `Request.classes(...)` 中的主线逻辑就说完了.


## 执行测试
我们继续看 `JUnitCore.run(...)` 的逻辑.
```java
    /**
     * Run all the tests in <code>classes</code>.
     *
     * @param computer Helps construct Runners from classes
     * @param classes the classes containing tests
     * @return a {@link Result} describing the details of the test run and the failed tests.
     */
    public Result run(Computer computer, Class<?>... classes) {
        return run(Request.classes(computer, classes));
    }
```
看来是调用了另一个 `run(...)` 方法(其代码如下).
```java
    /**
     * Run all the tests contained in <code>request</code>.
     *
     * @param request the request describing tests
     * @return a {@link Result} describing the details of the test run and the failed tests.
     */
    public Result run(Request request) {
        return run(request.getRunner());
    }
```
又调用了第三个 `run(...)` 方法(其代码如下).
```java
    /**
     * Do not use. Testing purposes only.
     */
    public Result run(Runner runner) {
        Result result = new Result();
        RunListener listener = result.createListener();
        notifier.addFirstListener(listener);
        try {
            notifier.fireTestRunStarted(runner.getDescription());
            // 运行测试, 这里的 runner 参数就是 桶大侠
            runner.run(notifier);
            notifier.fireTestRunFinished(result);
        } finally {
            removeListener(listener);
        }
        return result;
    }
```

我们直接看最核心的那一行 `runner.run(notifier);`,
看来会调用 `Runner` 类的 `run(...)` 方法.
实际被执行的是 `ParentRunner` 类中定义的 `run(...)` 方法(代码如下).
```java
    @Override
    public void run(final RunNotifier notifier) {
        EachTestNotifier testNotifier = new EachTestNotifier(notifier,
                getDescription());
        try {
            // 1. 生成 Statement 的实例
            Statement statement = classBlock(notifier);
            // 2. 执行 evaluate() 方法
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            testNotifier.addFailedAssumption(e);
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable e) {
            testNotifier.addFailure(e);
        }
    }
```
核心步骤就两步
1. 生成 `Statement` 的实例
2. 执行 `evaluate()` 方法

### 第`1`步
```java
    /**
     * Constructs a {@code Statement} to run all of the tests in the test class.
     * Override to add pre-/post-processing. Here is an outline of the
     * implementation:
     * <ol>
     * <li>Determine the children to be run using {@link #getChildren()}
     * (subject to any imposed filter and sort).</li>
     * <li>If there are any children remaining after filtering and ignoring,
     * construct a statement that will:
     * <ol>
     * <li>Apply all {@code ClassRule}s on the test-class and superclasses.</li>
     * <li>Run all non-overridden {@code @BeforeClass} methods on the test-class
     * and superclasses; if any throws an Exception, stop execution and pass the
     * exception on.</li>
     * <li>Run all remaining tests on the test-class.</li>
     * <li>Run all non-overridden {@code @AfterClass} methods on the test-class
     * and superclasses: exceptions thrown by previous steps are combined, if
     * necessary, with exceptions from AfterClass methods into a
     * {@link org.junit.runners.model.MultipleFailureException}.</li>
     * </ol>
     * </li>
     * </ol>
     *
     * @return {@code Statement}
     */
    protected Statement classBlock(final RunNotifier notifier) {
        Statement statement = childrenInvoker(notifier);
        if (!areAllChildrenIgnored()) {
            statement = withBeforeClasses(statement);
            statement = withAfterClasses(statement);
            statement = withClassRules(statement);
        }
        return statement;
    }
```