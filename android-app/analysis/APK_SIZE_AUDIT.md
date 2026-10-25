# APK 体积差异审计

原始 base APK 为 11,467,734 字节，当前原生复刻 APK 约 135 KB。体积差异主要来自依赖、语言包和 PairIP，不代表功能数量差异。

## 原始 base APK 解包统计

| 类别 | 文件数 | 解压大小 |
| --- | ---: | ---: |
| DEX | 2 | 5,957,304 B |
| 多语言 messages 属性文件 | 36 | 9,103,863 B |
| assets | 12 | 1,781,819 B |
| resources | 572 | 1,522,354 B |
| Kotlin metadata | 8 | 53,396 B |
| META-INF / 其他 | 72 | 299,429 B |

最大的单个成员是未压缩的 `classes.dex`，约 5.96 MB。随后是数十个 `messages_*.properties` 本地化文件，每个约 300–500 KB。原应用还包含 88 个 locale（AAPT 计数）、CameraX、Room、WorkManager、Ktor、OkHttp、Jackson、Kotlin 协程、Google Play Core 和许可组件。

## arm64 配置分卷

| 成员 | 大小 |
| --- | ---: |
| `libpairipcore.so` | 640,064 B |
| `libimage_processing_util_jni.so` | 32,528 B |
| `libsurface_util_jni.so` | 4,896 B |

这些是 native ELF，不能直接成为 Java 功能实现的体积基线。

## 当前复刻 APK

| 类别 | 解压大小 |
| --- | ---: |
| `classes.dex` | 约 208 KB |
| 原应用图标 | 25,337 B |
| Manifest/resources | 约 8 KB |

当前 APK 只有应用自身 Java 代码和一个原版图标资源，不包含第三方 GUI、网络、相机或序列化框架，因此压缩后明显更小。

## 结论

- 文件大小不能证明 UI、数学能力或交互与原版一致。
- 若仅为了接近 11 MB 而加入未使用的 AndroidX/Ktor/多语言包，只会增加攻击面和维护成本，不会提高功能复刻度。
- 1:1 验收应使用 `FUNCTION_INVENTORY.md` 的功能覆盖、输入输出测试、页面/设置对照和视觉资源对照。
- 当前仍在补齐矩阵、复数、统计、回归、位运算、格式与设置；未完成项列在 `GAP_MATRIX.md`。
