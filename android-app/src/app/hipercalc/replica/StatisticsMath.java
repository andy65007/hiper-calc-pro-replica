package app.hipercalc.replica;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StatisticsMath {
    private StatisticsMath() {
    }

    public static double[] parse(String input) throws CalcEngine.CalcException {
        String source = input == null ? "" : input.trim().replace('，', ',').replace('；', ',').replace(';', ',');
        if (source.isEmpty()) throw new CalcEngine.CalcException("请输入数据");
        String[] values = source.split("[,\\s]+");
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            try {
                result[i] = Double.parseDouble(values[i]);
            } catch (NumberFormatException error) {
                throw new CalcEngine.CalcException("数据不是数值：" + values[i]);
            }
        }
        return result;
    }

    public static double minimum(double[] values) {
        requireData(values);
        double result = Double.POSITIVE_INFINITY;
        for (double value : values) result = Math.min(result, value);
        return result;
    }

    public static double maximum(double[] values) {
        requireData(values);
        double result = Double.NEGATIVE_INFINITY;
        for (double value : values) result = Math.max(result, value);
        return result;
    }

    public static double sum(double[] values) {
        requireData(values);
        double result = 0;
        for (double value : values) result += value;
        return result;
    }

    public static double mean(double[] values) {
        return sum(values) / values.length;
    }

    public static double geometricMean(double[] values) throws CalcEngine.CalcException {
        requireData(values);
        double sumLog = 0;
        for (double value : values) {
            if (value <= 0) throw new CalcEngine.CalcException("几何平均数要求全部数据为正");
            sumLog += Math.log(value);
        }
        return Math.exp(sumLog / values.length);
    }

    public static double harmonicMean(double[] values) throws CalcEngine.CalcException {
        requireData(values);
        double reciprocal = 0;
        for (double value : values) {
            if (value == 0) throw new CalcEngine.CalcException("调和平均数要求数据非零");
            reciprocal += 1 / value;
        }
        return values.length / reciprocal;
    }

    public static double quadraticMean(double[] values) {
        requireData(values);
        double sum = 0;
        for (double value : values) sum += value * value;
        return Math.sqrt(sum / values.length);
    }

    public static double median(double[] input) {
        requireData(input);
        double[] values = input.clone();
        Arrays.sort(values);
        int middle = values.length / 2;
        return (values.length & 1) == 1 ? values[middle] : (values[middle - 1] + values[middle]) / 2;
    }

    public static double percentile(double[] input, double percentile) throws CalcEngine.CalcException {
        requireData(input);
        if (percentile < 0 || percentile > 100) throw new CalcEngine.CalcException("百分位必须在 0–100 之间");
        double[] values = input.clone();
        Arrays.sort(values);
        if (values.length == 1) return values[0];
        double position = percentile / 100 * (values.length - 1);
        int lower = (int) Math.floor(position);
        int upper = (int) Math.ceil(position);
        double fraction = position - lower;
        return values[lower] + (values[upper] - values[lower]) * fraction;
    }

    public static double variancePopulation(double[] values) {
        double mean = mean(values);
        double sum = 0;
        for (double value : values) sum += (value - mean) * (value - mean);
        return sum / values.length;
    }

    public static double varianceSample(double[] values) throws CalcEngine.CalcException {
        if (values.length < 2) throw new CalcEngine.CalcException("样本方差至少需要两个数据");
        double mean = mean(values);
        double sum = 0;
        for (double value : values) sum += (value - mean) * (value - mean);
        return sum / (values.length - 1);
    }

    public static double standardDeviationPopulation(double[] values) {
        return Math.sqrt(variancePopulation(values));
    }

    public static double standardDeviationSample(double[] values) throws CalcEngine.CalcException {
        return Math.sqrt(varianceSample(values));
    }

    public static double skewness(double[] values) {
        double mean = mean(values);
        double standard = standardDeviationPopulation(values);
        if (standard == 0) return 0;
        double sum = 0;
        for (double value : values) {
            double z = (value - mean) / standard;
            sum += z * z * z;
        }
        return sum / values.length;
    }

    public static double kurtosisExcess(double[] values) {
        double mean = mean(values);
        double standard = standardDeviationPopulation(values);
        if (standard == 0) return 0;
        double sum = 0;
        for (double value : values) {
            double z = (value - mean) / standard;
            sum += z * z * z * z;
        }
        return sum / values.length - 3;
    }

    public static double mode(double[] values) throws CalcEngine.CalcException {
        requireData(values);
        Map<Double, Integer> counts = new LinkedHashMap<>();
        int best = 0;
        double mode = values[0];
        Arrays.sort(values);
        for (double value : values) {
            int count = counts.containsKey(value) ? counts.get(value) + 1 : 1;
            counts.put(value, count);
            if (count > best) {
                best = count;
                mode = value;
            }
        }
        return mode;
    }

    public static String frequency(double[] values) throws CalcEngine.CalcException {
        requireData(values);
        Map<Double, Integer> counts = new java.util.TreeMap<>();
        for (double value : values) counts.put(value, counts.containsKey(value) ? counts.get(value) + 1 : 1);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Double, Integer> entry : counts.entrySet()) {
            if (builder.length() > 0) builder.append('\n');
            builder.append(CalcEngine.formatNumber(entry.getKey(), 12, "auto", false, false))
                    .append("：").append(entry.getValue());
        }
        return builder.toString();
    }

    public static double covariance(double[] x, double[] y) throws CalcEngine.CalcException {
        requirePair(x, y);
        double meanX = mean(x);
        double meanY = mean(y);
        double sum = 0;
        for (int i = 0; i < x.length; i++) sum += (x[i] - meanX) * (y[i] - meanY);
        return sum / (x.length - 1);
    }

    public static double correlation(double[] x, double[] y) throws CalcEngine.CalcException {
        requirePair(x, y);
        return covariance(x, y) / (standardDeviationSample(x) * standardDeviationSample(y));
    }

    public static Regression linear(double[] x, double[] y) throws CalcEngine.CalcException {
        requirePair(x, y);
        double meanX = mean(x);
        double meanY = mean(y);
        double numerator = 0;
        double denominator = 0;
        for (int i = 0; i < x.length; i++) {
            numerator += (x[i] - meanX) * (y[i] - meanY);
            denominator += (x[i] - meanX) * (x[i] - meanX);
        }
        if (denominator == 0) throw new CalcEngine.CalcException("X 数据不能全部相同");
        double slope = numerator / denominator;
        double intercept = meanY - slope * meanX;
        return new Regression(new double[]{slope, intercept}, "y = " + decimal(slope) + "x " + (intercept >= 0 ? "+ " : "− ") + decimal(Math.abs(intercept)));
    }

    public static Regression polynomial(double[] x, double[] y, int degree) throws CalcEngine.CalcException {
        requirePair(x, y);
        if (x.length <= degree) throw new CalcEngine.CalcException("数据点数量必须大于回归阶数");
        int size = degree + 1;
        double[][] augmented = new double[size][size + 1];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                double sum = 0;
                for (double value : x) sum += Math.pow(value, row + col);
                augmented[row][col] = sum;
            }
            double sum = 0;
            for (int i = 0; i < x.length; i++) sum += Math.pow(x[i], row) * y[i];
            augmented[row][size] = sum;
        }
        double[] coefficients = gaussian(augmented);
        StringBuilder equation = new StringBuilder("y = ");
        for (int power = degree; power >= 0; power--) {
            double coefficient = coefficients[power];
            if (Math.abs(coefficient) < 1e-12) continue;
            if (equation.length() > 4) equation.append(coefficient >= 0 ? " + " : " − ");
            equation.append(decimal(Math.abs(coefficient)));
            if (power > 0) equation.append("x");
            if (power > 1) equation.append('^').append(power);
        }
        return new Regression(coefficients, equation.toString());
    }

    public static Regression exponential(double[] x, double[] y) throws CalcEngine.CalcException {
        requirePair(x, y);
        double[] transformed = new double[y.length];
        for (int i = 0; i < y.length; i++) {
            if (y[i] <= 0) throw new CalcEngine.CalcException("指数回归要求 Y 全部为正");
            transformed[i] = Math.log(y[i]);
        }
        Regression logLinear = linear(x, transformed);
        double a = Math.exp(logLinear.coefficients[1]);
        double b = logLinear.coefficients[0];
        return new Regression(new double[]{a, b}, "y = " + decimal(a) + " × e^(" + decimal(b) + "x)");
    }

    public static Regression logarithmic(double[] x, double[] y) throws CalcEngine.CalcException {
        requirePair(x, y);
        double[] transformed = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            if (x[i] <= 0) throw new CalcEngine.CalcException("对数回归要求 X 全部为正");
            transformed[i] = Math.log(x[i]);
        }
        Regression logLinear = linear(transformed, y);
        double b = logLinear.coefficients[0];
        double a = logLinear.coefficients[1];
        return new Regression(new double[]{a, b}, "y = " + decimal(a) + " + " + decimal(b) + " × ln(x)");
    }

    private static double[] gaussian(double[][] input) throws CalcEngine.CalcException {
        int n = input.length;
        for (int col = 0; col < n; col++) {
            int pivot = col;
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(input[row][col]) > Math.abs(input[pivot][col])) pivot = row;
            }
            if (Math.abs(input[pivot][col]) < 1e-12) throw new CalcEngine.CalcException("回归方程奇异，无法求解");
            double[] temp = input[pivot];
            input[pivot] = input[col];
            input[col] = temp;
            double divisor = input[col][col];
            for (int j = col; j <= n; j++) input[col][j] /= divisor;
            for (int row = 0; row < n; row++) {
                if (row == col) continue;
                double factor = input[row][col];
                for (int j = col; j <= n; j++) input[row][j] -= factor * input[col][j];
            }
        }
        double[] result = new double[n];
        for (int i = 0; i < n; i++) result[i] = input[i][n];
        return result;
    }

    private static void requireData(double[] values) {
        if (values == null || values.length == 0) throw new IllegalArgumentException("数据不能为空");
    }

    private static void requirePair(double[] x, double[] y) throws CalcEngine.CalcException {
        if (x == null || y == null || x.length != y.length || x.length < 2) {
            throw new CalcEngine.CalcException("X/Y 数据数量必须相同且至少两组");
        }
    }

    private static String decimal(double value) {
        return CalcEngine.formatNumber(value, 12, "auto", false, false);
    }

    public static final class Regression {
        public final double[] coefficients;
        public final String equation;

        Regression(double[] coefficients, String equation) {
            this.coefficients = coefficients;
            this.equation = equation;
        }
    }
}
