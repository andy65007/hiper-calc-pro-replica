# HiPER Calc Pro 12.1.4 源码级功能清单

## 证据边界

本清单只依据当前目录中的安装产物及其本地反编译结果，不包含连接设备、运行时截图或外部服务。

- 包身份：`cz.hipercalc.pro`，version 12.1.4，versionCode 287
- Launcher：`app.hipercalc.CalculatorActivity`
- 应用层反编译源码：38 个文件
- 混淆核心：689 个文件
- 表达式运算符/函数注册表：`android.core.EnumC0275ra`，309 个枚举项
- 表达式节点工厂：`android.core.Bf`
- 设置页面实现：`android.core.C0195jg` 与 `app.hipercalc.view.settings.SettingsActivity`
- 说明：加密字符串已通过对应静态解码函数还原；PairIP 加封资源与专有算法仍属未知区域。

## 页面与流程

| 页面 | 源码证据 | 主要职责 |
| --- | --- | --- |
| 计算主页 | `app.hipercalc.CalculatorActivity` | 表达式编辑、结果显示、键盘、模式切换、跨页面结果回填 |
| 历史 | `view.resulthistory.ResultHistoryActivity` | 结果列表、删除、清空、载入 |
| 变量/内存 | `view.memory.MemoryActivity` | 变量集合、导入/导出、编辑 |
| 常量 | `view.constants.ConstantsActivity` | 按类别浏览常量并插入 |
| 单位换算 | `view.conversions.ConversionsActivity` | 类别/源单位/目标单位三段式换算 |
| 数值编辑 | `ValueEditActivity` / `VariableEditActivity` | 编辑数值与变量定义 |
| 设置主页 | `SettingsActivity` | 主从设置页、键盘布局、备份 |
| 键盘参数 | `KeyboardParamsActivity` | 键盘尺寸、分隔符、方向布局 |
| 按键功能 | `KeysFunctionsActivity` | 按键功能映射 |
| 主题 | `ThemeActivity` | 八组主题资源 |
| 数值详情 | `DetailActivity` | 多种表示、分享与复制 |
| 解题 | `SolutionActivity` | 方程/算法求解 |
| 解题反馈 | `SolutionFeedbackActivity` | 解题反馈 |
| 相机 | `CameraActivity` | CameraX 预览与拍照 |
| 图像裁剪 | `CropImageActivity` | 裁剪识别区域 |
| 识别结果 | `RecognitionActivity` | 识别表达式确认 |
| 帮助 | `HelpActivity` | 帮助目录 |
| 关于 | `AboutActivity` | 版本与许可信息 |
| 升级 | `UpgradeActivity` | 原商业版升级流程，复刻明确排除 |
| 崩溃报告 | `SendCrashReportActivity` | 异常报告 |

## 表达式与显示

### 输入/编辑

- 第二个功能键切换（`SECOND_FUNCTION`、`OFF`）
- 光标左/右、首页/末页、上下移动、退格、删除选区
- 复制、粘贴、剪切、换行、插入空白、清空
- 表达式改变、结果重放、历史与答案引用
- 可配置第二功能标签：2ndF / Shift
- 可配置除法符号：`÷` / `/`

### 结果显示模式

`android.core.EnumC0166ga` 明确包含六种：

- `DECIMAL`：十进制
- `EXPRESSION`：表达式/精确形式
- `MIXED_NUMBER`：带分数
- `DMS`：度-分-秒
- `DM`：度-分
- `POLAR`：极坐标

`EnumC0016Ca` 明确包含五种浮点显示格式：

- `NORM`
- `FIX`
- `SCI`
- `ENG`
- `ENG_SI`

设置中还可配置：

- 竖屏/横屏有效数字、指数位数
- NORM/FIX/SCI/ENG/ENG_SI 各自精度
- 小数分隔符、千位分隔符、千分位分隔符
- 国际数字分组与印度式分组
- 重复小数显示
- e 记数法
- 主平方根/全部根
- 立即计算、无限大结果
- 区间端点的圆括号/方括号表示

## 309 个运算符/函数/特殊 token

以下名称由 `EnumC0275ra` 的静态名称解码得到。它们不是都对应一个可执行的数学函数，部分为键盘、格式或 AST token；但它们完整界定了原版功能面。

### 算术、幂与数值

`ADDITION`、`SUBTRACTION`、`MULTIPLICATION`、`IMPLICIT_MULTIPLICATION`、`DIVISION`、`POW2`、`POW3`、`ROOT3`、`ROOTY`、`RECIPROC`、`MODULO`、`PERCENT`、`DELTA_PERCENTAGE`、`NEGATION`、`PLUS_MINUS`、`EXPONENT`、`EXPONENT_SI`、`SIGNUM`、`ABS`、`PARALLEL`、`MIXED_NUMBER`、`FRACTION`、`FACTOR`、`LCM`、`GCD` 相关组合 token。

### 对数、指数与常量

`LN`、`LOG10`、`LOGX`、`E_POW`、`TEN_POW`、`PI`、`E`、`RANDOM`、`I`。

### 角度与三角函数

`DEG`、`RAD`、`GRAD`、`TO_DEG`、`TO_RAD`、`TO_GRAD`、`DRG`、`DEG_TO_RAD`、`DEG_TO_GRAD`、`RAD_TO_DEG`、`RAD_TO_GRAD`、`GRAD_TO_DEG`、`GRAD_TO_RAD`、`DMS`、`REDMS`、`SIN`、`COS`、`TAN`、`COT`、`SEC`、`CSC`、`ASIN`、`ACOS`、`ATAN`、`ACOT`、`ASEC`、`ACSC`、`ATAN2`、`SINH`、`COSH`、`TANH`、`COTH`、`SECH`、`CSCH`、`ASINH`、`ACOSH`、`ATANH`、`ACOTH`、`ASECH`、`ACSCH`。

### 组合与取整

`PERMUTATION`、`PARTIAL_PERMUTATION`、`COMBINATION`、`DOUBLE_FACTORIAL`、`ROUND`、`ROUND_UP`、`FLOOR`、`CEILING`、`DECIMAL_PART`。

### 微积分、极限和表格

`DERIVATIVE`、`DER_APOS`、`INTEGRAL`、`INTEGRAL_BOUNDS`、`LIMIT`、`TABLE`。

### 复数

`POLAR`、`REAL_PART`、`IMAGINARY_PART`、`ARGUMENT`、`CONJUGATE`、`I`。

### 矩阵

- 构造与属性：`IDENTITY_MATRIX`、`ROW_COUNT`、`COLUMN_COUNT`、`SUBMATRIX`
- 基本运算：矩阵加法/减法/乘法、数乘、`HADAMARD_PRODUCT`、`KRONECKER_PRODUCT`
- 分解与化简：`LU_DECOMPOSITION`、`ROW_ECHELON`、`ROW_REDUCE`
- 标量性质：`DETERMINANT`、`TRACE`、`RANK`
- 变换：`TRANSPOSE`、`CONJUGATE_TRANSPOSE`、`ADJUGATE`
- 子式：`MINORS`、`COFACTORS`
- 特征问题：`CHARACTERISTIC_POLYNOMIAL`、`EIGEN_VALUES`、`EIGEN_VECTORS`、`DIAGONALIZE`

`android.core.YC` 是独立矩阵 AST 节点，`android.core.Bg` 含行列式、消元和伴随矩阵算法。`android.core.Bf` 的 AST 工厂明确创建 `Matrix` 节点。

### 向量

`DOT_PRODUCT`、`CROSS_PRODUCT`、`EUC_DISTANCE`。

### 统计

`MINIMUM`、`MAXIMUM`、`MEDIAN`、`PERCENTILE`、`SUM_SEQUENCE`、`SUM_SQUARES`、`ARITHMETIC_MEAN`、`GEOMETRIC_MEAN`、`HARMONIC_MEAN`、`QUADRATIC_MEAN`、`MODE`、`VARIANCE`、`STANDARD_DEVIATION`、`VARIATION_COEF`、`STD_DEV_OF_MEAN`、`COVARIANCE`、`PEARSON_COR_COEF`、`FREQUENCY`、`SKEWNESS`、`KURTOSIS`。

### 回归

`LINEAR_REG`、`CUBIC_REG`、`POLYNOMIAL_REG`、`EXPONENTIAL_REG`、`LOGARITHMIC_REG`。

### 整数与位运算

`BASE_DEC`、`BASE_OCT`、`BASE_HEX`、`DIGIT_A`–`DIGIT_F`、`AND`、`OR`、`XOR`、`NAND`、`NOR`、`XNOR`、`NOT`、`SHL`、`SHR`、`UNSIGNED_SHR`、`RL`、`RR`、`SHLN`、`SHRN`、`UNSIGNED_SHRN`、`RLN`、`RRN`。

设置项还明确支持二/八/十六进制的有符号/无符号、位宽、前导零和数字分组。

### 比较与集合

`GREATER`、`LESS_EQUAL`、`GREATER_OR_EQUAL`、`LESS_OR_GREATER`、`LESS_EQUAL_GREATER`、`NOT_EQUALS`、`ELEMENT_OF`、`NOT_ELEMENT_OF`、`UNION`。

## 变量、历史与持久化

- `Ans`、`LastX`、M+ / M− / M-in / M-recall / M-clear
- A–F、M 等变量，可编辑、插入、导入和导出
- 结果历史可设置最大条数
- 可保存部分结果、移除重复结果
- 键盘布局可导入/导出为 “HiPERCalc Keyboard Layouts” 格式

## 单位

`AbstractC0169gd` 与 `AbstractC0310vD` 包含以 `BigDecimal`/`MathContext` 实现的换算因子。已观察单位和常量包含：

- 长度：m、cm、mm、inch、foot、yard、mile、nautical mile、astronomical unit、light year 等
- 体积：L、mL、cm³、US gallon、US quart、US pint、US cup、fluid ounce、imperial gallon
- 质量：g、kg、oz、lb、stone、carat、short/long ton 等
- 角度：degree、radian、grad、arcminute、arcsecond
- 速度：m/s、km/h、mile/h、knot、foot/s
- 压力：Pa、bar、atm、psi、mmHg
- 能量/功率：J、cal、kWh、BTU、W、horsepower
- 温度：°C、°F、K

## 设置系统

`C0195jg` 中可直接还原出的设置键覆盖：

- `settings.portraitDigits` / `settings.landscapeDigits`
- `settings.decimalPrecision` / `fixPrecision` / `sciPrecision` / `engPrecision`
- `settings.signedness` 与二/八/十六进制显示
- `settings.leadingZeros`、`settings.internationalization`
- `settings.resultHistory`、`maxResultHistorySize`、`storePartialResults`、`removeDuplicates`
- `settings.hapticFeedback`：always / system / never
- `settings.buttonSound`、`buttonClickVolume`
- `settings.keyTitleStyle` 与第二功能标签
- `settings.divisionSign`、`settings.decimalSep`、`settings.thousandSep`、`settings.thousandthSep`
- `settings.repeatingDecimals.show`、`settings.indianDigitGrouping`
- `settings.display.portrait`、`hideStatusBar`、`hideNavigationButtons`
- `settings.display.landscape`、`fontSize`
- `settings.clearDisplay`、`requiredDelay`
- `settings.intervalNotation` 与区间括号方式
- `settings.keepScreenOn`、`blinkingCursor`
- `settings.emptyOneCursorPosition`、`eNotation`
- `settings.graphOneFingerMove`

## 相机与识别

- `CameraActivity` 使用 CameraX 和 `PreviewView`
- `CropImageActivity` 允许选择识别区域
- `RecognitionActivity` 将结果回填表达式
- 分卷中包含 `libimage_processing_util_jni.so` 与 `libsurface_util_jni.so`
- 网络与识别服务行为需要运行时或服务证据，静态分析无法证明具体模型或端点

## 原版明确缺失于当前复刻的部分

- 任意精度基本算术与 100 位显示
- 复数运算、极坐标与共轭
- 矩阵/向量完整运算
- 统计和回归
- 位运算与有符号二进制编辑
- DMS/DM、ENG_SI 与全部显示选项
- Newton 极限、函数表格
- 可导入/导出的自定义键盘布局
- 原 CameraX/裁剪/OCR 链路
- 原 PairIP、Google 许可、升级和广告流程

