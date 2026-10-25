# 原版 → 复刻版差距矩阵

| 子系统 | 原版证据 | 当前复刻版 | 目标 |
| --- | --- | --- | --- |
| 基本算术 | BigDecimal/表达式树 | BigDecimal 四则、整数幂、阶乘、多数函数 | 补齐符号代数和区间 |
| 精度 | 多档/自定义 | 3–100 位；无函数表达式走 BigDecimal | 与专有核心逐算法比对 |
| 显示格式 | NORM/FIX/SCI/ENG/ENG_SI | 五种已实现 | 分横竖屏独立参数 |
| 显示语义 | Decimal/Expression/Mixed/DMS/DM/Polar | 六种均已实现 | 改进符号精确形式 |
| 复数 | i、real/imag/arg/conjugate | 原生复数页：四则、幂、极坐标、复函数 | 接入主表达式解析器 |
| 矩阵 | 20+ 矩阵函数 | 加减乘、Hadamard/Kronecker、det/inverse/rank/RREF 等 | 非对称一般特征分解 |
| 向量 | dot/cross/distance | 已实现 | 扩展集合/区间语法 |
| 统计 | 20+ 描述统计 | 主要描述统计已实现 | 补齐分组统计 |
| 回归 | 五类 | linear/quadratic/cubic/exponential/log | 增加拟合质量指标 |
| 位运算 | AND/OR/XOR/shift/rotate/NAND 等 | 原生工具，4–128 位 | 自定义任意位宽 |
| 极限/表格 | LIMIT/TABLE token | 数值双侧/单侧极限、函数表 | 符号极限 |
| 历史 | 容量/部分结果/去重 | 容量可配、连续去重 | 部分结果开关 |
| 变量 | A–F/M/Ans/LastX | A–F/M/Ans/LastX，精确文本持久化 | 导入原版变量格式 |
| 键盘 | 可导入导出布局 | 四页固定键盘及参数页 | 原版键盘布局互操作 |
| 主题 | 8 个资源主题 | 8 个重建配色与原图标 | 保持 |
| 相机 | CameraX、裁剪、识别 | 系统相机/选图、原生可交互裁剪 | 本地 OCR 或原服务替代 |
| 本地化 | 88 个 locale | 中文界面 | 多语言资源 |
| Google 依赖 | 许可、广告、升级 | 无 | 按用户要求保持移除 |
| APK 签名 | Play 签名 | 未签名 | 按用户要求保持未签名 |

