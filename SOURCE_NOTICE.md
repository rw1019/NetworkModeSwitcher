# 源码来源说明

本仓库是在原始 Android Studio 工程不可用的情况下，根据 `NetworkModeSwitcher-debug.apk` 使用 JADX 1.5.6 恢复整理的代码快照。

反编译会丢失注释、局部变量名称、构建脚本和部分 Kotlin/Java 语义，并可能产生无法直接编译的代码。仓库中的恢复源码主要用于审查应用行为、保存现有实现并为后续重建提供参考，不应被描述为原始源码或可复现构建。

APK 信息：

- 应用名称：网络制式切换
- 包名：`com.example.networkmodeswitcher`
- 版本：`1.0`（versionCode 1）
- minSdk：31
- targetSdk：35
- 签名：Android Debug 证书

