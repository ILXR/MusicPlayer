# 智能织物 - 音乐播放器 - 开发文档

---

## 原项目资料

* [README.md](./README.old.md)

---

## 总需求

* [ ] 本地音乐播放器
* [ ] WebSocket通信
* [ ] 蓝牙通信
* [ ] 控制音乐：播放、暂停、上一首、下一首

---

## 参考资料

* WebSocket -> [Here](https://www.jianshu.com/p/7b919910c892)
* WebSocket 在线测试 -> [Here](http://www.websocket-test.com/)

---

## 工作记录

---

### 2021/5/3

* [ ] App会和原作者写好的后台通讯，会报错

```Java
java.net.SocketTimeoutException: failed to connect to guolin.tech/13.70.26.68 (port 80) from /10.202.46.171 (port 43832) after 10000ms
```

* [ ] 扫描本地歌曲时，数据库出错

```Java
java.lang.NullPointerException: Attempt to invoke virtual method 'int java.lang.String.length()' on a null object reference
  at com.yzbkaka.kakamusic.util.ChineseToEnglish.StringToPinyinSpecial(ChineseToEnglish.java:75)
  at com.yzbkaka.kakamusic.activity.ScanActivity$4.run(ScanActivity.java:224)
```

* 其他均未测试

---
