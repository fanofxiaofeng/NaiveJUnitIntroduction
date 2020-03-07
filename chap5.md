[第四回 一劳永逸](chap4.md)

# 第五回 皮掌门的校验

本回主要角色介绍
| 名称   |      类型      |  事迹 |
|----------|:-------------:|:------|
| **皮掌门** | `ParentRunner` | **桶大侠** 和 **布大侠** 的师父 |


**皮掌门** 的构造函数如下
```java
    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     */
    protected ParentRunner(Class<?> testClass) throws InitializationError {
        // 1. 将 testClass 转化为 TestClass 类型的变量, 并赋给 this.testClass
        this.testClass = createTestClass(testClass);
        // 2. 进行校验工作
        validate();
    }
```
其中的逻辑可以分为两步.
上回提到第`1`步会有 **泰大侠** 参与,
下面来说说第`2`步.

## 校验
**皮掌门** 的校验工作是通过调用 `validate()` 方法来完成的,
具体的代码如下
```java
    private void validate() throws InitializationError {
        // 1. 创建一个汇总错误的 List
        List<Throwable> errors = new ArrayList<Throwable>();
        // 2. 把错误放置在 errors 中
        collectInitializationErrors(errors);
        // 3. 如果 errors 中的元素数量不为 0, 则抛异常
        if (!errors.isEmpty()) {
            throw new InitializationError(errors);
        }
    }
```
可见有三步,
我们只看第`2`步.
第`2`步调用的方法的代码如下
```java
    /**
     * Adds to {@code errors} a throwable for each problem noted with the test class (available from {@link #getTestClass()}).
     * Default implementation adds an error for each method annotated with
     * {@code @BeforeClass} or {@code @AfterClass} that is not
     * {@code public static void} with no arguments.
     */
    protected void collectInitializationErrors(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(BeforeClass.class, true, errors);
        validatePublicVoidNoArgMethods(AfterClass.class, true, errors);
        validateClassRules(errors);
        applyValidators(errors);
    }
```
这个方法是 `protected` 级别的,
按照主线逻辑看过来,
实际上执行的是 **布大侠** 中的版本(具体如下)
```java
    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);

        validateNoNonStaticInnerClass(errors);
        validateConstructor(errors);
        // 关注点在这里
        validateInstanceMethods(errors);
        validateFields(errors);
        validateMethods(errors);
    }
```
注意,
上方的代码来自 `BlockJUnit4ClassRunner`.
看来会进行不少校验,
直接看 `validateInstanceMethods(...)` 的逻辑,
```java
    /**
     * Adds to {@code errors} for each method annotated with {@code @Test},
     * {@code @Before}, or {@code @After} that is not a public, void instance
     * method with no arguments.
     */
    @Deprecated
    protected void validateInstanceMethods(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(After.class, false, errors);
        validatePublicVoidNoArgMethods(Before.class, false, errors);
        // 关注点在这里
        validateTestMethods(errors);

        if (computeTestMethods().size() == 0) {
            errors.add(new Exception("No runnable methods"));
        }
    }
```
这方法竟然还有 `@Deprecated` 注解,
真别致.
为了不陷入各种细节中去,
我们直接看 `validateTestMethods(...)` 的逻辑.
```java
    /**
     * Adds to {@code errors} for each method annotated with {@code @Test}that
     * is not a public, void instance method with no arguments.
     */
    protected void validateTestMethods(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(Test.class, false, errors);
    }
```
只是一个函数调用而已,
那就接着看
```java
    /**
     * Adds to {@code errors} if any method in this class is annotated with
     * {@code annotation}, but:
     * <ul>
     * <li>is not public, or
     * <li>takes parameters, or
     * <li>returns something other than void, or
     * <li>is static (given {@code isStatic is false}), or
     * <li>is not static (given {@code isStatic is true}).
     * </ul>
     */
    protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation,
            boolean isStatic, List<Throwable> errors) {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);

        for (FrameworkMethod eachTestMethod : methods) {
            eachTestMethod.validatePublicVoidNoArg(isStatic, errors);
        }
    }
```
到这里其实还能继续看,
不过 `Javadoc` 已经说得挺清楚了,
到这个地方已经可以回答 [README.md](README.md) 里的提的第二个问题了(问题如下)
> 2. `@Test` 修饰的方法必须是无参数的方法,这个校验是在哪里做的?

`validatePublicVoidNoArgMethods(...)` 方法中会对此进行校验.

这样看下来,
校验工作整体上是 **皮掌门** 和 **布大侠** 合力完成的.
这里体现了 **模板方法** 的思想.
父类中把一些通用的逻辑写好,
子类根据自己的实际情况进行取舍.

## 校验通过
如果校验通过的话,
1. **老五** 会获取到一个 **布大侠** 的实例
2. **万大侠** 在 **老五** 的协助下, 生成了一个 `Runner`(其实就是上一步里 **布大侠** 的实例)


## 校验失败
如果校验失败,
会抛出异常,
这个异常会在 `RunnerBuilder` 里的 `safeRunnerForClass(...)` 中被捕获
```java
    /**
     * Always returns a runner, even if it is just one that prints an error instead of running tests.
     *
     * @param testClass class to be run
     * @return a Runner
     */
    public Runner safeRunnerForClass(Class<?> testClass) {
        try {
            return runnerForClass(testClass);
        } catch (Throwable e) {
            return new ErrorReportingRunner(testClass, e);
        }
    }
```
可以看到,
这里竟然 `catch` 的是 `Throwable`,
真是铁了心要解决所有异常.

所以在校验失败的情况下,
1. 异常一直向上抛, 连 **万大侠** 也处理不了
2. **万大侠** 的父类 `RunnerBuilder` 中会捕获异常, 并生成一个特殊的 `Runner` 来交差

也就是说,
无论校验成功与否,
回到 **桶大侠** 的构造函数`1`(代码如下)时,
```java
    /**
     * Call this when there is no single root class (for example, multiple class names
     * passed on the command line to {@link org.junit.runner.JUnitCore}
     *
     * @param builder builds runners for classes in the suite
     * @param classes the classes in the suite
     */
    public Suite(RunnerBuilder builder, Class<?>[] classes) throws InitializationError {
        this(null, builder.runners(null, classes));
    }
```
都一定会有 `builder.runners(...)` 总是可以返回元素数量正确的 `List<Runner>`.
绕了这么一大圈, **桶大侠** 总算能执行构造函数`2`了.
这样就回到了 [第三回](chap3.md) 中特意提到的话头.
**桶大侠** 的构造函数`2`的代码如下
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

[第六回 组合模式大显身手](chap6.md)