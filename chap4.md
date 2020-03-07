[第三回 五兄弟同心造`Runner`](chap3.md)

# 第四回 一劳永逸

本回主要角色介绍
| 名称 | 类型 | 事迹 |
|----------|:-------------:|:------|
| **布大侠** | `BlockJUnit4ClassRunner` | 可以生成 `Runner` |
| **泰大侠** | `TestClass` | 将测试类的信息进行解析与整合, 供 `JUnit` 的其他类使用 |
| **皮掌门** | `ParentRunner` | **桶大侠** 和 **布大侠** 的师父 |

上回说到 **五兄弟** 中的 **老五** 在登场后, 
总能将 `Class<?>` 类型的 `testClass` 转化成 `Runner`,
说得直白一点就是 **老五** 会把程序员写的测试类转化为 `JUnit` 可以直接操作的类(即 `Runner`).
我们在 [第一回](chap1.md) 里提到过 `JUnit` 的两大步骤
> * 解析要测试的类
> * 执行测试

其实 [第二回](chap2.md) 和 [第三回](chap3.md) 都是铺垫,
本回才是真正进行解析工作.

[第三回](chap3.md) 提到过 **老五** 生成 `Runner` 的逻辑(如下).
```java
    @Override
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        return new BlockJUnit4ClassRunner(testClass);
    }
```
其实就是 `new` 了一个 `BlockJUnit4ClassRunner` 类的实例.

这个 `BlockJUnit4ClassRunner` 是什么来头呢?
![IDEA 中的截图](pic/BlockJUnit4Runner.png)
看来和 `Suite` 有点像.
`Suite` 是 `ParentRunner` 的子类,
`BlockJUnit4ClassRunner` 也是 `ParentRunner<FrameworkMethod>` 的子类.
不过 `Suite` 是 `Runner` 的全家桶,
而 `BlockJUnit4ClassRunner` 则是测试方法的全家桶.

既然 **老五** 会生成 `BlockJUnit4ClassRunner` 类的实例,
而这个实例又有不少戏份,
按照惯例,
还是给它起个名字吧.
这次用谐音,
称它为 **布大侠**.
那么 **老五** 会 `new` 出 **布大侠**.

下面我们开始研究 **布大侠** 的行为.
既然是被 `new` 出来的,
那就顺着这个构造函数看吧.
```java
    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @throws InitializationError if the test class is malformed.
     */
    public BlockJUnit4ClassRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }
```
看来逻辑也不复杂,
就是调用父类(`ParentRunner`)的构造函数而已.

**桶大侠** 和 **布大侠** 的关系和同门师兄弟类似,
那我们把这两位大侠的师父(其实是父类) `ParentRunner` 称为 **皮掌门** 吧.

接着前往 **皮掌门** 的构造函数.
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
共有两步(请参考上方代码).
两步的逻辑都不少,
那就顺着看吧.

先看第`1`步.
第`1`步主要逻辑是创建 `TestClass` 类型的实例(赋值给 `this.testClass` 的逻辑比较简单, 就略过了).
那就直接看 `createTestClass(Class<?> testClass)` 的逻辑
```java
    protected TestClass createTestClass(Class<?> testClass) {
        return new TestClass(testClass);
    }
```
所以继续看 `TestClass` 的这个构造函数.
![IDEA 中的截图](pic/TestClass.png)
这个构造函数有十几行代码,
看来还是做了不少事情的.

先看看它的 `Javadoc`
```java
    /**
     * Creates a {@code TestClass} wrapping {@code clazz}. Each time this
     * constructor executes, the class is scanned for annotations, which can be
     * an expensive process (we hope in future JDK's it will not be.) Therefore,
     * try to share instances of {@code TestClass} where possible.
     */
```

我的理解是 `Class<?>` 类型的 `clazz` 可以(间接)提供测试类的各种信息(哪些方法加了注解, 加了什么注解).
但是这些信息 `JUnit` 用起来不方便(而且有些边边角角的特殊情况也需要处理), 
所以需要把这些信息整合一下, 
方便后续使用.

这个构造函数里就是在做各种信息解析与整合的工作.
`TestClass` 里逻辑挺多的,
且又比较重要,
我们还是先起名字,
继续用谐音的方式,
叫它 **泰大侠**.

## 生活中的例子
### 历史课
假设历史课有18课时,
上课时老师一般会提到哪些部分比较重要.
考前复习的时候,
会着重关注这些重点部分,
而不是去回想18个课时中的所有细节.
这些重点部分,
就有点像历史课本被解析的结果.
而老师的工作也和 **泰大侠** 有些相通之处(都需要对信息进行解析和整合).

### 复盘
生活中对不顺利的事情进行复盘的过程也是类似的.
这个过程是在对不顺利的事情进行解析和整合.
解析与整合后的结果,
使用起来就很方便(有点 **前事不忘后事之师** 的意思).

切回正题,
**泰大侠** 做的事情可以分成`5`步(如下方代码所示)
```java
    /**
     * Creates a {@code TestClass} wrapping {@code clazz}. Each time this
     * constructor executes, the class is scanned for annotations, which can be
     * an expensive process (we hope in future JDK's it will not be.) Therefore,
     * try to share instances of {@code TestClass} where possible.
     */
    public TestClass(Class<?> clazz) {
        // 1. 将入参 clazz 赋值给 this.clazz
        this.clazz = clazz;
        
        // 2. 如果 clazz 不是 null 的话, 则必须刚好有1个构造函数
        if (clazz != null && clazz.getConstructors().length > 1) {
            throw new IllegalArgumentException(
                    "Test class can only have one constructor");
        }

        // 3. new 两个 Map, 在第4步会用到
        Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations =
                new LinkedHashMap<Class<? extends Annotation>, List<FrameworkMethod>>();
        Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations =
                new LinkedHashMap<Class<? extends Annotation>, List<FrameworkField>>();

        // 4. 扫描被注解的成员(其实就是解析 clazz 中的信息, 并将相关信息保存在第3步生成的 Map 中)
        scanAnnotatedMembers(methodsForAnnotations, fieldsForAnnotations);

        // 5. 将第4步的处理结果包装一下, 保存在对应的数据成员里
        this.methodsForAnnotations = makeDeeplyUnmodifiable(methodsForAnnotations);
        this.fieldsForAnnotations = makeDeeplyUnmodifiable(fieldsForAnnotations);
    }
```


上面五个步骤中,
前三步都比较简单,
就不赘述了.
第`5`步的逻辑对主线剧情无影响,
也跳过.
第`4`步的逻辑最多, 
我们着重看一下.
第`4`步的调用了 `scanAnnotatedMembers(...)`(入参的信息较长, 就用 `...` 代替了).
这个方法的字面意思是 **扫描被注解的成员**.
成员包括 `method` 和 `field`.

该方法的内容如下
```java
    protected void scanAnnotatedMembers(Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations, Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations) {
        // 外层 for 循环遍历 clazz 的继承链
        for (Class<?> eachClass : getSuperClasses(clazz)) {
            // 内层 for 循环1
            for (Method eachMethod : MethodSorter.getDeclaredMethods(eachClass)) {
                addToAnnotationLists(new FrameworkMethod(eachMethod), methodsForAnnotations);
            }
            // ensuring fields are sorted to make sure that entries are inserted
            // and read from fieldForAnnotations in a deterministic order
            // 内层 for 循环2
            for (Field eachField : getSortedDeclaredFields(eachClass)) {
                addToAnnotationLists(new FrameworkField(eachField), fieldsForAnnotations);
            }
        }
    }
```

## 三个 `for` 循环
其中一共有三个 `for` 循环
1. 外层 `for` 循环在遍历类
2. 内层 `for` 循环`1`在遍历 `method`
3. 内层 `for` 循环`2`在遍历 `field`

我们一个一个来看

### 外层 `for` 循环
外层 `for` 循环在执行前,
会先调用 `getSuperClasses(Class<?> testClass)` 方法,
该方法内容如下
```java
    private static List<Class<?>> getSuperClasses(Class<?> testClass) {
        ArrayList<Class<?>> results = new ArrayList<Class<?>>();
        Class<?> current = testClass;
        while (current != null) {
            results.add(current);
            current = current.getSuperclass();
        }
        return results;
    }
```
结合方法名和方法的逻辑可知,
这个方法返回的是 `testClass` 参数的继承链中的所有类.
以 [NaiveTest.java](src/main/java/com/study/junit/test/NaiveTest.java) 中的 `NaiveTest` 类为例,
它的父类是 `Object`,
所以如果入参 `testClass` 的值为 `NaiveTest.class` 的话,
这个方法的返回值就会是 `NaiveTest.class` 和 `Object.class` 组成的 `List`.

所以外层 `for` 循环是在遍历 `clazz` 继承链中的所有类.

### 内层 `for` 循环`1`

内层 `for` 循环`1`在执行前,
会先调用 `MethodSorter` 的 `getDeclaredMethods(Class<?> clazz)` 方法.
该方法的内容如下
```java
    /**
     * Gets declared methods of a class in a predictable order, unless @FixMethodOrder(MethodSorters.JVM) is specified.
     *
     * Using the JVM order is unwise since the Java platform does not
     * specify any particular order, and in fact JDK 7 returns a more or less
     * random order; well-written test code would not assume any order, but some
     * does, and a predictable failure is better than a random failure on
     * certain platforms. By default, uses an unspecified but deterministic order.
     *
     * @param clazz a class
     * @return same as {@link Class#getDeclaredMethods} but sorted
     * @see <a href="http://bugs.sun.com/view_bug.do?bug_id=7023180">JDK
     *      (non-)bug #7023180</a>
     */
    public static Method[] getDeclaredMethods(Class<?> clazz) {
        Comparator<Method> comparator = getSorter(clazz.getAnnotation(FixMethodOrder.class));

        Method[] methods = clazz.getDeclaredMethods();
        if (comparator != null) {
            Arrays.sort(methods, comparator);
        }

        return methods;
    }
```
可以概括为三步
1. 将 `clazz` 中声明的所有方法保存在 `methods` 变量中
2. 把 `methods` 中的元素排序
3. 返回 `methods`

我觉得看源码时,
可以借鉴 **擒贼先擒王** 或者 **抓住主要矛盾** 的思想.
要先把主线逻辑弄清楚,
不要陷入过多的细节中.
例如这里的第`2`步看起来就是个排序,
至于排序的规则,
这里大可不必关心(需要深入了解时, 再回来看这里就行了).

所以内层 `for` 循环`1`是在(按某种顺序)遍历 `eachClass` 中定义的所有 `method`
接下来接着看 `for` 循环的内部逻辑(如下)
```java
addToAnnotationLists(new FrameworkMethod(eachMethod), methodsForAnnotations);
```
虽然就一行,
但其实是两步
1. 生成 `FrameworkMethod` 的实例
2. 调用 `addToAnnotationLists(...)` 方法

先说第`1`步.
照我的理解, 
`FrameworkMethod` 就是把 `java.lang.reflect.Method` 简单包装了一下,
便于后续的处理(例如用 `FrameworkMethod` 的实例来触发方法调用更方便).

第`2`步调用的方法的内容如下
```java
    protected static <T extends FrameworkMember<T>> void addToAnnotationLists(T member,
            Map<Class<? extends Annotation>, List<T>> map) {
        for (Annotation each : member.getAnnotations()) {
            Class<? extends Annotation> type = each.annotationType();
            List<T> members = getAnnotatedMembers(map, type, true);
            if (member.isShadowedBy(members)) {
                return;
            }
            if (runsTopToBottom(type)) {
                members.add(0, member);
            } else {
                members.add(member);
            }
        }
    }
```
它是个泛型方法(内层 `for` 循环`2`里也会调用它).

从方法名来看,
这个方法的作用是 **把被同一个注解修饰的 `member` 放置在同一个 `List` 里**.
大致看一下方法的逻辑,
和方法名吻合(但是该方法中会处理一些边边角角的情况).

顺便提一下,
在下面这一行,
`@Test` 会被提起出来, 这也是 [README.md](README.md) 中第一个问题的答案
```java
Class<? extends Annotation> type = each.annotationType();
```

#### 主线之外
如果仔细看的话,
会发现这个函数里还会处理一些边边角角的情况.
我来举个例子.
如果父类 `Parent`(在 [Parent.java](src/main/java/com/study/junit/test/Parent.java) 里) 中有一个方法 `f()`,
子类 `Child` (在 [Child.java](src/main/java/com/study/junit/test/Child.java) 里) 中也有一个签名完全相同的方法 `f()`(此时 `Child` 其实 `Override` 了 `Parent` 中的方法).
那么我们测试 `Child` 类时,
父类中的 `f()` 方法是否会被执行呢?
答案是**不会**.
不过本文只是梳理主线剧情,
有兴趣的读者可以自己打个断点,
看看具体原因是什么,
这里就不赘述了.


### 内层 `for` 循环`2`

内层 `for` 循环`2`在执行前,
会先调用 `getSortedDeclaredFields(Class<?> clazz)` 方法(该方法内容如下)
```java
    private static Field[] getSortedDeclaredFields(Class<?> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        Arrays.sort(declaredFields, FIELD_COMPARATOR);
        return declaredFields;
    }
```
看起来和内层 `for` 循环`1`差不多,
也是三个步骤
1. 将 `clazz` 中声明的所有 `field` 保存在 `declaredFields` 变量中
2. 把 `declaredFields` 中的元素排序
3. 返回 `declaredFields`

内层 `for` 循环`2`是在(按某种顺序)遍历 `eachClass` 中定义的所有 `field`
接下来看 `for` 循环的内部逻辑(如下)
```java
addToAnnotationLists(new FrameworkField(eachField), fieldsForAnnotations);
```
跟内层 `for` 循环`1`一样,
也是两步,
具体如下
1. 生成 `FrameworkField` 的实例
2. 调用 `addToAnnotationLists(...)` 方法

第`1`步中的 `FrameworkField` 类其实就把 `java.lang.reflect.Field` 类给包装了一下.
第`2`步中的 `addToAnnotationLists(...)` 方法和内层 `for` 循环`1`中的那个完全一样,
我再贴一下
```java
    protected static <T extends FrameworkMember<T>> void addToAnnotationLists(T member,
            Map<Class<? extends Annotation>, List<T>> map) {
        for (Annotation each : member.getAnnotations()) {
            Class<? extends Annotation> type = each.annotationType();
            List<T> members = getAnnotatedMembers(map, type, true);
            if (member.isShadowedBy(members)) {
                return;
            }
            if (runsTopToBottom(type)) {
                members.add(0, member);
            } else {
                members.add(member);
            }
        }
    }
```
该方法的作用仍旧是
> 把被同一个注解修饰的 `member` 放置在同一个 `List` 里

## 本回剧情回顾
**泰大侠** 在本回做的事情是解析测试类中的 `member`(`method` 和 `field` 都是 `member`)
并按照注解归类(其实测试类的祖先类中的 `member` 也会被考虑在内).
1. 带有相同注解的 `method` 会放在同一个 `List` 中
2. 带有相同注解的 `field` 会放在同一个 `List` 中
归类的结果会保存在 `methodsForAnnotations` 和 `fieldsForAnnotations` 中.
在 **泰大侠** 做完归类工作后,
后面再需要查找测试类中被 `@Test` 注解修饰的方法时,
就很轻松了(不必再通过反射去查看每个方法上是否有 `@Test` 注解).

## 第一个问题的答案
本回中提到了第一个问题的答案,
这里再强调一下
```java
    protected static <T extends FrameworkMember<T>> void addToAnnotationLists(T member,
            Map<Class<? extends Annotation>, List<T>> map) {
        for (Annotation each : member.getAnnotations()) {
            // 如果一个方法被 @Test 修饰, 那么在 @Test 注解在下一行会被获取到
            Class<? extends Annotation> type = each.annotationType();
            List<T> members = getAnnotatedMembers(map, type, true);
            if (member.isShadowedBy(members)) {
                return;
            }
            if (runsTopToBottom(type)) {
                members.add(0, member);
            } else {
                members.add(member);
            }
        }
    }
```

欲知 **皮掌门** 如何执行校验,
且听[下回](chap5.md)分解

[第五回 皮掌门的校验](chap5.md)