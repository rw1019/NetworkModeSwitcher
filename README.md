# 网络制式切换（Network Mode Switcher）

面向三星双卡 Android 设备的移动网络制式与 5G 组网模式切换工具。应用可读取每张 SIM 的实际驻网状态及最高网络制式，并通过 Root 或 Shizuku 切换 3G、4G、5G。

## 当前版本：v1.3

- 自动显示 SIM 1 / SIM 2 的当前实际网络与最高网络制式
- 当前制式按钮自动高亮
- 支持 3G、4G、5G 最高制式切换与锁定
- 自动识别系统当前上网卡
- 仅在当前上网卡实际驻留 5G 时显示 5G 组网选项
- 支持 `SA+NSA 自动`、`仅 NSA`、`仅 SA` 三种三星基带配置
- Material 3 风格界面
- 自动跟随 Android 系统浅色/深色模式
- 支持 Root，基础制式切换也可使用 Shizuku

## 系统要求

- Android 12（API 31）或更高版本
- 三星设备；SA/NSA 功能依赖三星 ServiceMode 基带菜单
- Root 权限，或已安装并启动 Shizuku
- 允许读取手机状态，以识别 SIM 卡和当前驻网

## 下载

- [v1.3 调试版 APK](releases/NetworkModeSwitcher-v1.3-debug.apk)
- [v1.0 历史版 APK](releases/NetworkModeSwitcher-v1.0-debug.apk)

调试版使用 Android 调试签名。升级安装要求与旧版签名一致，否则需要先卸载旧版。

## 使用方法

1. 授予应用 Root 权限，或启动并授权 Shizuku。
2. 允许读取电话状态。
3. 选择 SIM 1 或 SIM 2，查看并切换最高网络制式。
4. 当系统当前上网卡实际连接 5G 时，可选择自动、NSA 或 SA。

SA/NSA 只作用于系统当前的移动数据卡，不跟随上方手动选中的 SIM。切换后基带需要重新驻网，能否连接取决于运营商和当地基站支持。

## 构建

使用 Android Studio 或 Gradle 8.9 构建：

```shell
gradle :app:assembleDebug
```

项目使用 Android SDK 35，源码位于 `app/src/main`。`recovered-source` 保留早期 APK 的恢复源码，仅供历史对照。

## 兼容性说明

应用依赖 Android 内部电话服务和三星基带菜单。不同机型、One UI、Android 版本、基带及运营商的接口可能不同。强制选择某种制式不能生成当地不存在的网络覆盖；例如扫描不到 3G 小区时，强制 3G 会显示无服务。

## 更新记录

完整记录见 [CHANGELOG.md](CHANGELOG.md)。

## 开源协议

本项目采用 [MIT License](LICENSE)。AndroidX、Shizuku 等第三方组件遵循各自许可证。
