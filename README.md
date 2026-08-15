# 网络制式切换（Network Mode Switcher）

一款面向三星 Android 设备的移动网络制式切换工具。它支持双卡设备，可通过 Root 或 Shizuku 调用系统电话服务，快速选择 3G、4G 或 5G 优先网络模式，并查看当前 SIM 卡的网络设置。

## 功能

- 支持 SIM 1 / SIM 2 选择
- 快速切换 3G、4G、5G 优先模式
- 支持 Root 和 Shizuku 两种授权方式
- 可读取当前首选网络模式
- 简洁的 Material Design 界面

## 系统要求

- Android 12（API 31）或更高版本
- 三星设备（其他品牌可能无法使用）
- Root 权限，或已安装并启动 Shizuku
- 允许应用读取手机状态，以识别 SIM 卡

## 下载与安装

仓库中的 `releases/NetworkModeSwitcher-v1.0-debug.apk` 是从现有调试构建保留的安装包。安装前请确认你了解调试签名应用的风险。

## 使用方法

1. 启动 Shizuku，或授予应用 Root 权限。
2. 打开应用并允许电话状态权限。
3. 选择需要控制的 SIM 卡。
4. 选择 3G、4G 或 5G 网络模式。

## 兼容性说明

本工具依赖 Android 内部电话服务接口。不同三星机型、One UI / Android 版本、基带及运营商可能采用不同接口，因此无法保证所有设备均可使用。切换后若无法联网，请恢复系统设置中的自动网络模式并重启设备。

## 源码说明

当前仓库源码由现有 APK 恢复，用于代码审查、研究和后续重建，不是原始 Android Studio 工程，也不保证能够直接编译。详情见 [SOURCE_NOTICE.md](SOURCE_NOTICE.md)。如找回原始工程，建议用它替换当前恢复版本。

## 开源协议

本项目采用 [MIT License](LICENSE)。第三方 AndroidX、Material Components 和 Shizuku 组件仍遵循各自的许可证。

