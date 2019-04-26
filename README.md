## 背景
之前面试一些校招同学，聊到微信小程序是什么launchMode，其任务栈是如何实现的？很多同学只提到singleInstance，这是不合适的。

今天我们就猜测并解析一下微信主程序与小程序的关系与大致实现，最后给出源码，可以给大家作一个简单参考。

## 初探
既然要研究微信，那么我们就先打开几个小程序，再用adb命令看看任务栈信息。

在终端使用 `adb shell dumpsys activity activities` 命令后，可以找到最近任务列表的Activity信息：
```
Running activities (most recent first):
  TaskRecord{caccd90 #3239 A=.AppBrandUI3 U=0 StackId=1 sz=1}
    Run #4: ActivityRecord{bb162b8 u0 com.tencent.mm/.plugin.appbrand.ui.AppBrandUI3 t3239}
  TaskRecord{d6c62d6 #3190 A=com.tencent.mm U=0 StackId=1 sz=1}
    Run #3: ActivityRecord{7f2d805 u0 com.tencent.mm/.ui.LauncherUI t3190}
  TaskRecord{34a386a #3238 A=.AppBrandUI2 U=0 StackId=1 sz=1}
    Run #2: ActivityRecord{16cfede u0 com.tencent.mm/.plugin.appbrand.ui.AppBrandUI2 t3238}
  TaskRecord{7ade2d1 #3237 A=.AppBrandUI U=0 StackId=1 sz=1}
    Run #1: ActivityRecord{ccfd8ae u0 com.tencent.mm/.plugin.appbrand.ui.AppBrandUI t3237}
  ...
```
可以发现这里的#3是微信主Activity，4、2、1都是我开的小程序，且位于不同的任务栈中，Activity名称都是AppBrandUI+数字的形式。

然后再看看其他关键信息（这里我单独筛出来）：
```
packageName=com.tencent.mm processName=com.tencent.mm
taskAffinity=com.tencent.mm

packageName=com.tencent.mm processName=com.tencent.mm:appbrand3
taskAffinity=.AppBrandUI3

packageName=com.tencent.mm processName=com.tencent.mm:appbrand2
taskAffinity=.AppBrandUI2

packageName=com.tencent.mm processName=com.tencent.mm:appbrand
taskAffinity=.AppBrandUI
```
很简单，和我们平时实现多进程差不多，说明是给Activity设置了process属性。

## 思考
转念一想，小程序那么多，难道这些不同后缀的Activity都写死在代码里吗？

显然不能这么干，只能两种途径可以达成目的：
- 不在Manifest里面静态注册Activity，使用类似Hook的方式动态创建进程和Activity
- 动静结合，设计一个Activity池，在本地写死有限数量的Activity，通过复用的方式承载小程序

对于第一种，我查阅了一些资料，理论上讲是可以做到的，涉及到NDK开发，需要我们对AMS的源码很熟悉，不走常规流程启动Activity，且小程序是多进程的，可能还需要手动fork进程。

这种方式显然具有较大的风险，属于黑科技范畴，而且谷歌官方是不推荐的，微信作为十几亿用户的常驻App，几乎不太可能使用这一方案。

那么只剩第二种了，预先在本地写死n个一样的Activity（当然也可以通过继承形式），同时在Manifest中注册好。

然后打开一个小程序就占用一个Activity，当打开第n+1个小程序时，覆盖第1个小程序所在的Activity，这样就相当于第1个小程序被顶掉了。

分析到此，就很明显了，如果真的是第二种方案，那么小程序就不能无限数量地打开咯？果断打开微信试了一下，果然，最多只能开5个！当你启动第6个小程序时，第1个就被销毁了。

其实这也是符合我们上述预期的，每个小程序的进程不一样，taskAffinity也不一样，类名也不一样。原生API是不支持动态设置taskAffinity和进程名的。

## 简单实现
小程序所在的Activity：
```java
public class SmallActivity extends AppCompatActivity {

    public static class Small0 extends SmallActivity {}
    public static class Small1 extends SmallActivity {}
    public static class Small2 extends SmallActivity {}
    public static class Small3 extends SmallActivity {}
    public static class Small4 extends SmallActivity {}

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_small);
        
        // 动态地给小程序Activity设置名称和图标，下面代码只是举例，实际信息肯定是动态获取的
        // 由于iconRes这个构造参数的API 28才加入的，所以建议区分版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            int iconRes = 0; // 这里应该是小程序图标的资源索引
            setTaskDescription(new ActivityManager.TaskDescription("小程序名", iconRes));
        } else {
            Bitmap iconBmp = null; // 这里应该是小程序图标的bitmap
            setTaskDescription(new ActivityManager.TaskDescription("小程序名", iconBmp));
        }
    }
}
```
Manifest：
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.ysy.smallapp">

    <application
        ...>

        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".SmallActivity$Small0"
            android:label="Small0"
            android:launchMode="singleTask"
            android:process=":Small0"
            android:taskAffinity=".Small0" />

        <activity
            android:name=".SmallActivity$Small1"
            android:label="Small1"
            android:launchMode="singleTask"
            android:process=":Small1"
            android:taskAffinity=".Small1" />

        <activity
            android:name=".SmallActivity$Small2"
            android:label="Small2"
            android:launchMode="singleTask"
            android:process=":Small2"
            android:taskAffinity=".Small2" />

        <activity
            android:name=".SmallActivity$Small3"
            android:label="Small3"
            android:launchMode="singleTask"
            android:process=":Small3"
            android:taskAffinity=".Small3" />

        <activity
            android:name=".SmallActivity$Small4"
            android:label="Small4"
            android:launchMode="singleTask"
            android:process=":Small4"
            android:taskAffinity=".Small4" />

    </application>

</manifest>
```
具体的复用逻辑这里暂时就这样简单地实现了，实际情况肯定比此复杂：
```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val edtText = findViewById<EditText>(R.id.edt_main)

        findViewById<View>(R.id.btn_main).setOnClickListener {
            startActivity(Intent().apply {
                val id = edtText.text.toString().toInt() % 5
                setClassName(this@MainActivity, "com.ysy.smallapp.SmallActivity\$Small$id")
            })
        }
    }
}
```
