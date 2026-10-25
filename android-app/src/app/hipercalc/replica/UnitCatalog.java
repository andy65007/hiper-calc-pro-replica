package app.hipercalc.replica;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class UnitCatalog {
    public static final class Unit {
        public final String id;
        public final String label;
        public final String symbol;
        public final double factor;
        public final double offset;

        Unit(String id, String label, String symbol, double factor, double offset) {
            this.id = id;
            this.label = label;
            this.symbol = symbol;
            this.factor = factor;
            this.offset = offset;
        }
    }

    public static final class Category {
        public final String id;
        public final String label;
        public final String icon;
        public final List<Unit> units;

        Category(String id, String label, String icon, Unit... units) {
            this.id = id;
            this.label = label;
            this.icon = icon;
            this.units = Arrays.asList(units);
        }
    }

    private static Unit unit(String id, String label, String symbol) {
        return new Unit(id, label, symbol, 1, 0);
    }

    private static Unit unit(String id, String label, String symbol, double factor) {
        return new Unit(id, label, symbol, factor, 0);
    }

    public static final List<Category> CATEGORIES = Arrays.asList(
            new Category("length", "长度", "↔",
                    unit("m", "米", "m"), unit("km", "千米", "km", 1000),
                    unit("cm", "厘米", "cm", .01), unit("mm", "毫米", "mm", .001),
                    unit("um", "微米", "µm", 1e-6), unit("nm", "纳米", "nm", 1e-9),
                    unit("mi", "英里", "mi", 1609.344), unit("yd", "码", "yd", .9144),
                    unit("ft", "英尺", "ft", .3048), unit("in", "英寸", "in", .0254),
                    unit("nmi", "海里", "nmi", 1852)),
            new Category("area", "面积", "▭",
                    unit("m2", "平方米", "m²"), unit("km2", "平方千米", "km²", 1e6),
                    unit("cm2", "平方厘米", "cm²", 1e-4), unit("ha", "公顷", "ha", 10000),
                    unit("acre", "英亩", "acre", 4046.8564224), unit("ft2", "平方英尺", "ft²", .09290304),
                    unit("in2", "平方英寸", "in²", .00064516), unit("mi2", "平方英里", "mi²", 2589988.110336)),
            new Category("volume", "体积", "◫",
                    unit("m3", "立方米", "m³"), unit("l", "升", "L", .001),
                    unit("ml", "毫升", "mL", 1e-6), unit("cm3", "立方厘米", "cm³", 1e-6),
                    unit("gal_us", "美制加仑", "gal", .003785411784),
                    unit("qt_us", "美制夸脱", "qt", .000946352946),
                    unit("cup_us", "美制杯", "cup", .0002365882365),
                    unit("floz_us", "美制液量盎司", "fl oz", .0000295735295625),
                    unit("gal_uk", "英制加仑", "gal UK", .00454609)),
            new Category("mass", "质量", "⚖",
                    unit("kg", "千克", "kg"), unit("g", "克", "g", .001),
                    unit("mg", "毫克", "mg", 1e-6), unit("t", "公吨", "t", 1000),
                    unit("lb", "磅", "lb", .45359237), unit("oz", "盎司", "oz", .028349523125),
                    unit("st", "英石", "st", 6.35029318), unit("ct", "克拉", "ct", .0002)),
            new Category("temperature", "温度", "°",
                    unit("c", "摄氏度", "°C"),
                    new Unit("f", "华氏度", "°F", 5.0 / 9, -17.77777777777778),
                    new Unit("k", "开尔文", "K", 1, -273.15),
                    new Unit("r", "兰氏度", "°R", 5.0 / 9, -273.15)),
            new Category("time", "时间", "◷",
                    unit("s", "秒", "s"), unit("ms", "毫秒", "ms", .001),
                    unit("min", "分钟", "min", 60), unit("h", "小时", "h", 3600),
                    unit("day", "天", "day", 86400), unit("week", "周", "week", 604800),
                    unit("year", "年（365 天）", "yr", 31536000)),
            new Category("speed", "速度", "➜",
                    unit("mps", "米/秒", "m/s"), unit("kmh", "千米/时", "km/h", 1.0 / 3.6),
                    unit("mph", "英里/时", "mph", .44704), unit("knot", "节", "kn", .5144444444444445),
                    unit("fps", "英尺/秒", "ft/s", .3048)),
            new Category("pressure", "压力", "⬒",
                    unit("pa", "帕斯卡", "Pa"), unit("kpa", "千帕", "kPa", 1000),
                    unit("mpa", "兆帕", "MPa", 1e6), unit("bar", "巴", "bar", 100000),
                    unit("atm", "标准大气压", "atm", 101325), unit("psi", "磅力/平方英寸", "psi", 6894.757293168),
                    unit("mmhg", "毫米汞柱", "mmHg", 133.322387415)),
            new Category("force", "力", "⇉",
                    unit("n", "牛顿", "N"), unit("kn", "千牛顿", "kN", 1000),
                    unit("dyn", "达因", "dyn", 1e-5), unit("kgf", "千克力", "kgf", 9.80665),
                    unit("lbf", "磅力", "lbf", 4.4482216152605)),
            new Category("torque", "力矩", "↻",
                    unit("nm", "牛顿·米", "N·m"), unit("knm", "千牛顿·米", "kN·m", 1000),
                    unit("lbf_ft", "磅力·英尺", "lbf·ft", 1.3558179483314),
                    unit("kgf_m", "千克力·米", "kgf·m", 9.80665)),
            new Category("acceleration", "加速度", "⤒",
                    unit("mps2", "米/秒²", "m/s²"), unit("gal", "伽", "Gal", .01),
                    unit("g0", "标准重力", "g₀", 9.80665), unit("ftps2", "英尺/秒²", "ft/s²", .3048)),
            new Category("frequency", "频率", "∿",
                    unit("hz", "赫兹", "Hz"), unit("khz", "千赫兹", "kHz", 1e3),
                    unit("mhz", "兆赫兹", "MHz", 1e6), unit("ghz", "吉赫兹", "GHz", 1e9),
                    unit("rpm", "每分钟转数", "rpm", 1.0 / 60)),
            new Category("density", "密度", "◍",
                    unit("kgm3", "千克/立方米", "kg/m³"), unit("gcm3", "克/立方厘米", "g/cm³", 1000),
                    unit("kgL", "千克/升", "kg/L", 1000),
                    unit("lbft3", "磅/立方英尺", "lb/ft³", 16.01846337396)),
            new Category("flow", "流量", "⇢",
                    unit("m3s", "立方米/秒", "m³/s"), unit("lps", "升/秒", "L/s", .001),
                    unit("lpm", "升/分钟", "L/min", .001 / 60),
                    unit("gpm_us", "美制加仑/分钟", "gal/min", .003785411784 / 60),
                    unit("cfm", "立方英尺/分钟", "ft³/min", .028316846592 / 60)),
            new Category("energy", "能量", "ϟ",
                    unit("j", "焦耳", "J"), unit("kj", "千焦", "kJ", 1000),
                    unit("cal", "卡路里", "cal", 4.184), unit("kcal", "千卡", "kcal", 4184),
                    unit("wh", "瓦时", "Wh", 3600), unit("kwh", "千瓦时", "kWh", 3600000),
                    unit("ev", "电子伏特", "eV", 1.602176634e-19),
                    unit("btu", "英热单位", "BTU", 1055.05585262)),
            new Category("power", "功率", "◉",
                    unit("w", "瓦特", "W"), unit("kw", "千瓦", "kW", 1000),
                    unit("mw", "兆瓦", "MW", 1e6), unit("hp", "机械马力", "hp", 745.6998715822702),
                    unit("ps", "公制马力", "PS", 735.49875)),
            new Category("data", "数据", "▦",
                    unit("bit", "比特", "bit"), unit("byte", "字节", "B", 8),
                    unit("kb", "千字节", "kB", 8000), unit("kib", "KiB", "KiB", 8192),
                    unit("mb", "兆字节", "MB", 8e6), unit("mib", "MiB", "MiB", 8388608),
                    unit("gb", "吉字节", "GB", 8e9), unit("gib", "GiB", "GiB", 8.589934592e9),
                    unit("tb", "太字节", "TB", 8e12)),
            new Category("angle", "角度", "∠",
                    unit("rad", "弧度", "rad"), unit("deg", "度", "°", Math.PI / 180),
                    unit("grad", "百分度", "grad", Math.PI / 200), unit("turn", "整转", "turn", Math.PI * 2),
                    unit("arcmin", "角分", "′", Math.PI / 10800),
                    unit("arcsec", "角秒", "″", Math.PI / 648000))
    );

    public static Category category(String id) {
        for (Category category : CATEGORIES) if (category.id.equals(id)) return category;
        return CATEGORIES.get(0);
    }

    public static Unit unit(Category category, String id) {
        for (Unit unit : category.units) if (unit.id.equals(id)) return unit;
        return category.units.get(0);
    }

    public static double convert(Category category, Unit from, Unit to, double value) {
        double base = value * from.factor + from.offset;
        return (base - to.offset) / to.factor;
    }

    public static List<Unit> alternatives(Category category, Unit from) {
        List<Unit> result = new ArrayList<>();
        for (Unit unit : category.units) if (!unit.id.equals(from.id)) result.add(unit);
        return result;
    }
}
