package app.hipercalc.replica;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ConstantCatalog {
    public static final class Entry {
        public final String group;
        public final String name;
        public final String symbol;
        public final String display;
        public final String expression;

        Entry(String group, String name, String symbol, String display, String expression) {
            this.group = group;
            this.name = name;
            this.symbol = symbol;
            this.display = display;
            this.expression = expression;
        }
    }

    public static final List<Entry> ALL = Arrays.asList(
            new Entry("数学", "圆周率", "π", "3.141592653589793", "pi"),
            new Entry("数学", "自然常数", "e", "2.718281828459045", "e"),
            new Entry("数学", "黄金比例", "φ", "1.618033988749895", "phi"),
            new Entry("数学", "欧拉-马歇罗尼常数", "γ", "0.5772156649015329", "0.5772156649015329"),
            new Entry("数学", "卡塔兰常数", "G", "0.915965594177219", "0.915965594177219"),
            new Entry("物理", "光速", "c", "299 792 458 m/s", "299792458"),
            new Entry("物理", "重力加速度", "g", "9.80665 m/s²", "9.80665"),
            new Entry("物理", "标准大气压", "atm", "101 325 Pa", "101325"),
            new Entry("物理", "万有引力常数", "G", "6.67430 × 10⁻¹¹", "6.67430e-11"),
            new Entry("物理", "基本电荷", "e", "1.602176634 × 10⁻¹⁹ C", "1.602176634e-19"),
            new Entry("物理", "玻尔兹曼常数", "kᵦ", "1.380649 × 10⁻²³", "1.380649e-23"),
            new Entry("物理", "阿伏伽德罗常数", "Nₐ", "6.02214076 × 10²³", "6.02214076e23"),
            new Entry("物理", "理想气体常数", "R", "8.314462618", "8.314462618"),
            new Entry("天文", "天文单位", "au", "149 597 870 700 m", "149597870700"),
            new Entry("天文", "光年", "ly", "9 460 730 472 580 800 m", "9460730472580800"),
            new Entry("天文", "秒差距", "pc", "3.085677581 × 10¹⁶ m", "3.085677581e16"),
            new Entry("天文", "太阳质量", "M☉", "1.98847 × 10³⁰ kg", "1.98847e30"),
            new Entry("天文", "地球质量", "M⊕", "5.9722 × 10²⁴ kg", "5.9722e24"),
            new Entry("化学", "原子质量单位", "u", "1.660539066 × 10⁻²⁷ kg", "1.660539066e-27"),
            new Entry("化学", "法拉第常数", "F", "96 485.33212 C/mol", "96485.33212"),
            new Entry("化学", "玻尔半径", "a₀", "5.291772109 × 10⁻¹¹ m", "5.291772109e-11"),
            new Entry("化学", "电子质量", "mₑ", "9.1093837015 × 10⁻³¹ kg", "9.1093837015e-31"),
            new Entry("化学", "质子质量", "mₚ", "1.67262192369 × 10⁻²⁷ kg", "1.67262192369e-27")
    );

    public static List<Entry> group(String group) {
        List<Entry> result = new ArrayList<>();
        for (Entry entry : ALL) if (entry.group.equals(group)) result.add(entry);
        return result;
    }
}
