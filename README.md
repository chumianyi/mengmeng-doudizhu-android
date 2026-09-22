# 萌萌斗地主 (Android版)

从 iOS IPA 移植的安卓版斗地主游戏。

## 技术栈
- Kotlin + 原生 Android
- OkHttp 网络请求
- Gson JSON 解析
- 横屏游戏界面

## 接口
- Base URL: `https://ddz.oldipa.com/api/app/v1/`
- `POST login` - 邮箱密码登录
- `POST logout` - 退出登录
- `GET version/check` - 版本检查
- `GET me` - 恢复会话
- `GET room/active` - 获取活跃房间
- `GET room/state` - 轮询房间状态
- `POST room/action` - 发送游戏动作
- `POST room/leave` - 离开房间
- `GET game/history` - 战绩列表

## 功能
- 邮箱登录/注册
- 练习模式（本地AI对战）
- 联网对战（原IPA服务器接口）
- 完整斗地主流程：发牌、叫地主、抢地主、加倍、出牌、结算
- 战绩查询
- 设置（音效、音乐、主题）
- 使用IPA内全部图片和音频资源

## 构建
```bash
./gradlew assembleRelease
```
APK输出: `app/build/outputs/apk/release/`
