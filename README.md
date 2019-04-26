## 背景
之前面试一些校招同学，聊到微信小程序是什么launchMode，其任务栈是如何实现的？很多同学只提到singleInstance，显然是不合适的。

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
