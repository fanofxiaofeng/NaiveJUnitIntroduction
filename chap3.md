[第二回 `Runner`全家桶](chap2.md)
# 第三回 五兄弟同心造`Runner`

本回主要角色介绍
| 名称 | 类型 | 事迹 |
|----------|:-------------:|:------|
| **匿大侠** | `Suite` 的子类 | 作用像是万大侠的静态代理 |
| **万大侠** | `AllDefaultPossibilitiesBuilder` | `Runner` 的**万**能构建者 |
| **桶大侠** | `Suite` | `Runner` 全家**桶** |
| **五兄弟** | `JUnit4Builder` | 协助 **万大侠** 构建 `Runner` 的**五**个构建者 |


上回说到通过调用 `Suite` 类的构造函数, 
我们就会得到 **桶大侠**.
但是还没有说到其中的细节.

我们再看一下 **桶大侠** 是如何登场的
![IDEA 中的截图](pic/Computer.png)
其实就是调用 `Suite` 类中的构造函数,
具体来说, 是 `Suite(RunnerBuilder builder, Class<?>[] classes)` 这个构造函数(简称为构造函数`1`).
构造函数`1`里有如下两个参数
* `RunnerBuilder` 类型的 `builder`
* `Class<?>[]` 类型的 `classes`

我们再看一下上面的这张图, 
在调用构造函数`1`时, 
需要提供 `builder` 参数和 `classes` 参数.
就 **桶大侠** 登场这个场景而言,
构造函数`1`里的 `builder` 参数是通过匿名内部类方式创建的 `RunnerBuilder` 的子类的实例.
构造函数`1`里的 `classes` 参数和我们一开始在 `main` 函数里填写的测试类对应.

这个匿名内部类在本回中还会再出现, 
为了便于称呼, 
就叫它 **匿大侠** 吧.

**匿大侠** 戏份比较少, 
主要还是靠 **万大侠** 来干活(**匿大侠** 有点像是 **桶大侠** 的 **静态代理** ).
注意: **匿大侠** 是 `RunnerBuilder` 的子类的实例,
**万大侠** 是 `AllDefaultPossibilitiesBuilder` 的实例,
所以 **匿大侠** 和 **万大侠** 都是 `RunnerBuilder` 类型的.

**万大侠** 是如何依附在 **匿大侠** 这里的呢? 可以参考下图.
![IDEA 中的截图](pic/greatBuilder.png)

我们看看构造函数`1`内部的逻辑
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
看起来不复杂, 就两件事
1. 执行 `builder.runners(null, classes)`
2. 调用了另一个构造函数(简称为构造函数`2`)

第`1`步中的主要逻辑是 **万大侠** 把 `classes` 转化为 `List<Runner>`.
里面的逻辑还挺多的, 
我们一会儿拐回来看里面的逻辑.
第`2`步的逻辑比较简单, 
先把它解决掉.
构造函数`2`里的逻辑如下
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
构造函数`2`里做了两件事(已经写在上方的注释里了).

构造函数`2`的逻辑看完了, 
我们还回到构造函数`1`的第`1`步,
看看 **万大侠** 是如何干活的.
构造函数`1`的第`1`步,
虽然可以简单改成成 **万大侠** 生成 `List<Runner>`,
但是里面的逻辑有很多层,
直到 [第五回](chap5.md) **万大侠** 才能把生成 `List<Runner>` 的逻辑执行完.
看官牢记这段话头,
[第五回](chap5.md) 还会再回到构造函数`1`的第`2`步.

第`1`步是执行 `builder.runners(null, classes)` 方法.
这个方法的逻辑可以分为三步(请参考下方代码)
```java
    /**
     * Constructs and returns a list of Runners, one for each child class in
     * {@code children}.  Care is taken to avoid infinite recursion:
     * this builder will throw an exception if it is requested for another
     * runner for {@code parent} before this call completes.
     */
    public List<Runner> runners(Class<?> parent, Class<?>[] children)
            throws InitializationError {
        // 第1步
        addParent(parent);

        try {
            // 第2步
            return runners(children);
        } finally {
            // 第3步
            removeParent(parent);
        }
    }
```

看起来中间第`2`步比较核心(我不清楚第`1`步和第`3`步的作用, 但是看起来不是核心步骤),
那就看第`2`步的逻辑吧
```java
    private List<Runner> runners(Class<?>[] children) {
        ArrayList<Runner> runners = new ArrayList<Runner>();
        for (Class<?> each : children) {
            Runner childRunner = safeRunnerForClass(each);
            if (childRunner != null) {
                runners.add(childRunner);
            }
        }
        return runners;
    }
```

这里的逻辑看起来不复杂.
主逻辑是一个 `for` 循环, 
`for` 循环每运行一次, 
`children` 中就有一个元素被转化为 `Runner` 的实例 `childRunner`.
这些 `childRunner` 会被收集到一个名为 `runners` 的 `List` 里.

所以这里的关键点就是那个转化逻辑了.
转化逻辑是通过 `safeRunnerForClass(Class<?> testClass)` 完成的.
那就看看里面发生了什么吧.
![IDEA 中的截图](pic/safeRunnerForClass.png)
看起来是保证将入参 `testClass` 转化为一个 `Runner`, 
方法名的字面意思也是如此.

我们抓住主要矛盾,
`catch` 语句块里的逻辑本文就不管了, 
我们看看 `runnerForClass(testClass)` 里做了什么.
如果是打断点运行到这里, 
就会发现现在会跳回下图的位置
![IDEA 中的截图](pic/back.png)
本回中 **匿大侠** 已经出现过了, 
这里又是 **匿大侠** 表现的时候.
**匿大侠** `Override` 了 `runnerForClass(Class<?> testClass)` 方法.
其逻辑只有如下一行
```java
return getRunner(builder, testClass);
```
话休絮烦, 
我们打断点看 `getRunner(builder, testClass)` 里的逻辑,
经过几次简单的跳转之后, 
就到了 `AllDefaultPossibilitiesBuilder` 类的 `runnerForClass(Class<?> testClass)` 方法里.
```java
    @Override
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        // 1. 找5个 RunnerBuilder 来构成 List
        List<RunnerBuilder> builders = Arrays.asList(
                ignoredBuilder(),
                annotatedBuilder(),
                suiteMethodBuilder(),
                junit3Builder(),
                junit4Builder());

        // 2. 5个 RunnerBuilder 轮流生成 Runner 的实例 runner, 
        //    当出现第一个不为 null 的 runner 时, 返回那个 runner
        for (RunnerBuilder each : builders) {
            Runner runner = each.safeRunnerForClass(testClass);
            if (runner != null) {
                return runner;
            }
        }
        return null;
    }
```
现在是 **万大侠** 的时间.
**万大侠** 的 `runnerForClass(Class<?> testClass)` 函数里,
做了两件事情(请参考上方代码)

从第`1`步来看, **万大侠** 手下还有`5`个帮手.
由于这`5`个帮手都是 `RunnerBuilder` 的子类的实例,
就把它们称为 **五兄弟** 吧.
**五兄弟** 都能执行 `runnerForClass(Class<?> testClass)` 方法.

## 五兄弟
为了不脱离主线逻辑,
这里只简单介绍一下 **五兄弟** 的分工,
### 老大
老大类型为 `IgnoredBuilder`, 
其代码如下(这里只关心 `runnerForClass(...)` 方法)
```java
public class IgnoredBuilder extends RunnerBuilder {
    @Override
    public Runner runnerForClass(Class<?> testClass) {
        if (testClass.getAnnotation(Ignore.class) != null) {
            return new IgnoredClassRunner(testClass);
        }
        return null;
    }
}
```
可见只有当测试类上带有 `@Ignore` 注解时,
`runnerForClass(...)` 方法的返回值才不是 `null`.
### 老二
老二类型为 `AnnotatedBuilder`, 
其 `runnerForClass(Class<?> testClass)` 方法的内容如下
```java
    @Override
    public Runner runnerForClass(Class<?> testClass) throws Exception {
        for (Class<?> currentTestClass = testClass; currentTestClass != null;
             currentTestClass = getEnclosingClassForNonStaticMemberClass(currentTestClass)) {
            RunWith annotation = currentTestClass.getAnnotation(RunWith.class);
            if (annotation != null) {
                return buildRunner(annotation.value(), testClass);
            }
        }

        return null;
    }
```
可见它会处理测试类带有 `@RunWith` 注解的情形(这里不展开细节)
## 老三
老三的类型为 `SuiteMethodBuilder`, 其代码我没细看, 这里跳过

### 老四
老四的类型为 `JUnit3Builder`, 其代码我没细看, 这里跳过

## 老五
老五的类型为 `JUnit4Builder`,
其代码如下
```java
public class JUnit4Builder extends RunnerBuilder {
    @Override
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        return new BlockJUnit4ClassRunner(testClass);
    }
}
```
它在 `runnerForClass(Class<?> testClass)` 方法里直接 `new` 了一个 `Runner`, 
可见到 **老五** 出场时, 
它一定能把 `testClass` 参数转化为 `Runner`,
**老五** 真出色呀. 


**五兄弟** 一起帮助 **万大侠** 生成 `Runner`,
这里有 **策略模式** 的思想.

我把 [README.md](README.md) 中提到的 `main` 函数在这里再贴一下
```java
public static void main(String[] args) {
  Result result = JUnitCore.runClasses(NaiveTest.class);
  for (Failure failure : result.getFailures()) {
    System.out.println(failure);
  }
}
```
当这个 `main` 函数执行时,
`NaiveTest` 这个测试类最终会被 **老五** 处理(`NaiveTest` 会被 **老五** 被转化成一个 `Runner`)

在这一回里, 
我们看到了 **万大侠** 在 **五兄弟** 的协助下, 
成功将 `Class<?>` 类型的 `testClass` 转化成了 `Runner`.
值得一提的是 **老五**,
在前面四位都处理不了的情况下, 
**老五** 总是能返回一个非 `null` 的 `Runner`,
不愧是最终的王牌角色.


我们简单回顾一下本回的主要角色.
**匿大侠** 像是 **万大侠** 的静态代理,
**匿大侠** 自己没做什么事, 重要的逻辑都交给 **万大侠** 了.
**五兄弟** 是 **万大侠** 的好帮手.
不过 **五兄弟** 中前四位的戏份比较少, 
而 **老五** 总是能不辱使命(实际上 **五兄弟** 作用不同, 没有高下之分).

欲知 **老五** 具体是如何完成任务的, 
且听[下回](chap4.md)分解

[第四回 一劳永逸](chap4.md)