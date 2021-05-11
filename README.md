# 智能织物 - 音乐播放器 - 开发文档

---

## 项目资料

* [原项目文档](./README.old.md)
* [电子纱线ppt](./data/电子纱线绳音乐器.pptx)
* [电子纱线论文](./data/E-textile%20Microinteractions%20Augmenting%20Twist%20with.pdf)

---

## 总需求

* 本地音乐播放器
* WebSocket通信
* 蓝牙通信
* 控制音乐接口
  * 状态：播放、暂停
  * 切歌：上一首、下一首
  * 音量：增加、减小
  * 列表：随机播放、单曲循环、顺序播放

![Task](./data/Task_2021-05-06.png)

---

## 参考资料

* WebSocket -> [Here](https://www.jianshu.com/p/7b919910c892)
* WebSocket 在线测试 -> [Here](http://www.websocket-test.com/)

---

## 工作记录

---

### 2021/5/11

* [x] ~~添加蓝牙通信页面，可以搜索连接设备、收发信息，未实现通信控制逻辑~~
  * 侧边栏打开 -> 蓝牙设备
  ![蓝牙设备界面](./pic/bluetooth%20activity.png)

---

### 2021/5/6

* [x] ~~适配安卓8.0~~
* [x] ~~修改了包名(侵权行为)~~

---

### 2021/5/3

* [x] ~~App会和原作者写好的后台通讯，会报错 - 已修复~~

```Java
java.net.SocketTimeoutException: failed to connect to guolin.tech/13.70.26.68 (port 80) from /10.202.46.171 (port 43832) after 10000ms
```

* [x] ~~扫描本地歌曲时，数据库出错 - 已修复~~

```Java
java.lang.NullPointerException: Attempt to invoke virtual method 'int java.lang.String.length()' on a null object reference
  at com.yzbkaka.kakamusic.util.ChineseToEnglish.StringToPinyinSpecial(ChineseToEnglish.java:75)
  at com.yzbkaka.kakamusic.activity.ScanActivity$4.run(ScanActivity.java:224)
```

* [ ] 通知栏常驻的控制组件无法交互（点击无效），可能和安卓8.0以上改动有关 - 待修复

```Java
W/Notification: Use of stream types is deprecated for operations other than volume control
W/Notification: See the documentation for what to use instead with android.media.AudioAttributes to qualify your playback use case
```

* ~~修改了包名和部分UI显示，修复了MediaPlayer报错~~

---
