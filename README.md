# HiPER Calc Pro 12.1.4 · HarmonyOS 4.3 Android 复刻

交付物是原生 Android APK/工程，不是 WebView、浏览器应用或 PWA。

- 工程：[android-app](android-app)
- 已签名 APK：[HiPER-Calc-Pro-12.1.4-replica-debug-signed.apk](HiPER-Calc-Pro-12.1.4-replica-debug-signed.apk)
- 构建说明：[android-app/README.md](android-app/README.md)

## APK 参数

| 项目 | 值 |
| --- | --- |
| 应用名称 | HiPER Calc Pro |
| 包名 | `cz.hipercalc.pro` |
| 版本 | `12.1.4` / versionCode 287 |
| minSdk | 29 |
| targetSdk | 34 |
| 运行架构 | 纯 Java/DEX，无 ABI 限制 |
| 权限 | 仅 `VIBRATE` |
| 网络权限 | 无 |
| Google Play / 许可验证 | 无 |
| APK 签名 | 本地调试证书，APK Signature Scheme v2 |

公开包名和版本号已与原版对齐。复刻版使用独立调试证书；如设备上存在 Play 签名的原版，需先卸载原版，否则会出现签名不一致。

## 原生功能

- 自写表达式词法/语法引擎：隐式乘法、优先级、括号、比较、百分比和阶乘
- DEG/RAD/GRAD 与 DEC/HEX/BIN/OCT
- 三角、反三角、双曲、对数、指数、根、组合、排列、GCD/LCM
- 3–100 位 BigDecimal 路径：四则、幂、根、阶乘、组合/排列、三角/反三角、双曲、指数与对数
- 数值导数、定积分、求和、连乘、数值求根、极限与函数表
- 历史记录、A–F/M 变量、Ans、LastX、常量表、科学单位换算
- Decimal/Expression/带分数/DMS/DM/Polar 六种结果显示
- NORM/FIX/SCI/ENG/ENG_SI 五种格式与循环小数
- 一元至六次多项式方程、二/三次代数步骤与高次复数值根
- 原生矩阵/向量、复数、统计、五类回归、位运算和数论工具
- 八种原生主题、键盘参数、精度、触感、按键音、备份与本地持久化
- 本地图片选择、系统相机原图、可交互裁剪与手工校正表达式

全部页面使用 Android SDK 原生 Activity 和 View 实现，不加载 HTML 或 JavaScript。

## 构建

工程不依赖 Gradle，直接使用已安装的 Android SDK、build-tools 和 JDK 17。脚本会自动选择最高可用 platform/build-tools：

```powershell
cd android-app
.\build.ps1 -Clean
```

构建脚本依次执行：

1. AAPT2 编译并链接资源
2. `javac` 编译原生 Java 与生成的 `R.java`
3. D8 生成 `classes.dex`
4. 写入 APK
5. zipalign

构建脚本默认生成本地调试证书并调用 `apksigner`；`-Unsigned` 可显式跳过签名。未签名 APK 通常不能直接通过 HarmonyOS/Android 系统安装。

## 静态分析依据

分析范围仅为当前目录本地 APK：

- `HiPER-Calc-Pro-12.1.4-base.apk`
- `HiPER-Calc-Pro-12.1.4-split_config.arm64_v8a.apk`
- `HiPER-Calc-Pro-12.1.4-split-bundle.zip`

已观察：

- 包名 `cz.hipercalc.pro`，versionCode 287，minSdk 29，targetSdk/compileSdk 37
- Launcher 为 `app.hipercalc.CalculatorActivity`
- Manifest 声明历史、变量、常量、换算、设置、主题、详情、解题、帮助、相机与识别等 Activity
- 计算核心位于混淆的 `android.core`，使用 BigDecimal/BigInteger 和表达式树
- 数制包含 2、8、10、16
- 资源包含 classic、modern dark/light、shades、brushed、deep、luxfer、AMOLED 主题
- PairIP 包装、许可组件、加密封装资源和 arm64 原生图像库存在

REA 的 Android provider 因当前 MCP 进程未继承 `REA_JADX_MCP_JAR` 无法调用；静态核验使用本地 JADX 1.5.6、APKTool 3.0.3、AAPT 和 Android SDK。未分析连接的真实手机应用。

## 已知边界

- 原应用的专有符号代数内核、全部显示细节和特定算法尚未做到逐指令等价。
- 一般非对称矩阵的完整特征分解、原版键盘布局二进制格式仍待补齐。
- 当前界面以中文为主，原版约 88 个 locale 尚未全部翻译。
- 复刻版不包含专有 OCR 模型；图片流程保留选择、拍照、预览和手工校正。
- PairIP、Google 许可证验证、广告与升级链路没有复制。
- 当前无原始 APK 的完整 1:1 视觉资源，主题和控件是按本地资源用途重建的原生近似方案。

详细清单与计划：

- [源码级功能清单](android-app/analysis/FUNCTION_INVENTORY.md)
- [开发计划](android-app/analysis/DEVELOPMENT_PLAN.md)
- [差距矩阵](android-app/analysis/GAP_MATRIX.md)
- [APK 体积审计](android-app/analysis/APK_SIZE_AUDIT.md)
