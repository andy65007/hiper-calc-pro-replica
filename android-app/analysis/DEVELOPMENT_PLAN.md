# 1:1 原生复刻开发计划

## 验收定义

“1:1”在本计划中拆成四个可验证维度：

1. 功能等价：源码证据中的数学能力、页面和设置均有对应实现。
2. 行为等价：相同输入产生同一数学意义下的结果；可观察的编辑、历史和模式行为一致。
3. 视觉等价：使用本地 APK 可提取的同版本图标、主题命名、页面层级和控件密度。
4. 兼容等价：原生 Android、minSdk 29、无 WebView、无 Google 依赖，并输出未签名 APK。

PairIP 加密资源和专有服务不可能仅靠静态分析逐字节恢复，因此将使用独立实现，并把“无法证明等价”的项保持为显式 unknown。

## 阶段

### P0 源码盘点（已完成）

- 建立 Manifest/Activity 页面矩阵
- 解码显式设置键
- 从 `EnumC0275ra` 还原 309 个运算符/函数/token
- 识别六种结果显示模式、五种浮点格式、矩阵/复数/统计/回归/位运算能力
- 输出 `FUNCTION_INVENTORY.md`

### P1 原生 Android 基座（已完成）

- 纯 Java/DEX、无 WebView
- API 29–34
- 原生 Activity/View 页面
- 历史、变量、常量、换算、主题、设置、详情、解题、图片输入
- AAPT2 → javac → D8 → zipalign 可重复构建

### P2 数值内核升级（主要部分已完成）

- BigDecimal 高精度四则、幂、根、阶乘、组合/排列
- BigDecimal 三角、反三角、指数、对数、幂与双曲函数
- 最高 100 位显示
- NORM / FIX / SCI / ENG / ENG_SI
- 精确分数、带分数、DMS/DM、循环小数
- 2/8/10/16 进制精确整数路径
- 复数类型与极坐标/直角坐标转换

### P3 高级数学（主要部分已完成）

- 矩阵创建、加减乘、Hadamard/Kronecker
- determinant、rank、trace、transpose、RREF、inverse、adjugate、特征多项式
- 对称矩阵特征值、特征向量和对角化
- dot/cross/distance
- descriptive statistics
- linear/quadratic/cubic/exponential/logarithmic regression
- numeric limit/table
- 高次多项式数值求根与复根

### P4 交互与设置（进行中）

- 小数/千位/千分位分隔符
- 主根/全部根
- 重复小数、国际数字分组
- 横竖屏独立精度
- 光标闪烁、清空延迟、保持屏幕常亮
- 键盘布局导入/导出
- 历史容量、部分结果、去重

### P5 相机与识别（部分完成）

- 系统 Camera Intent 与原图输出
- 原生交互式裁剪
- 可替换识别接口
- 明确区分本地 OCR、原服务链路与手工校正

### P6 验证

- JVM 级计算测试
- AAPT/D8/Manifest 检查
- 资源键与功能矩阵覆盖检查
- 未签名 APK 校验
- 屏幕尺寸/横竖屏静态布局检查

## 当前优先级

1. 高精度结果显示
2. 复数、矩阵、统计和回归原生工具
3. 原版格式/显示设置
4. 键盘布局互操作
5. 相机裁剪与接入口

## 不可承诺项

- 原 PairIP 加密资源逐字节复原
- 原服务端 OCR 模型
- Google 许可/广告/升级行为
- 仅凭静态证据无法确认的内部算法等价

