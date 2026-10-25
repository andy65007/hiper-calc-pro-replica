package app.hipercalc.replica;

import java.util.ArrayList;
import java.util.List;

public final class EquationSolver {
    public static final class Root {
        public final double real;
        public final double imaginary;

        Root(double real, double imaginary) {
            this.real = real;
            this.imaginary = imaginary;
        }
    }

    public static final class Solution {
        public final String kind;
        public final List<Root> roots = new ArrayList<>();
        public final List<String> steps = new ArrayList<>();
        public Double vertexX;
        public Double vertexY;
        public Double discriminant;

        Solution(String kind) {
            this.kind = kind;
        }
    }

    private static final double EPSILON = 1e-10;
    private static final CalcEngine.Context CONTEXT = CalcEngine.Context.defaults();

    public static Solution solve(String equation, String variable) throws CalcEngine.CalcException {
        String source = equation == null ? "" : equation.trim();
        if (source.isEmpty()) throw new CalcEngine.CalcException("请输入方程");
        String[] sides = source.split("=", -1);
        if (sides.length != 2 || sides[0].trim().isEmpty() || sides[1].trim().isEmpty()) {
            throw new CalcEngine.CalcException("方程必须包含且仅包含一个等号");
        }
        Polynomial polynomial = fitPolynomial(sides, variable);
        double[] coefficients = polynomial.coefficients;
        int degree = polynomial.degree;
        if (degree == 0) {
            if (Math.abs(coefficients[0]) < EPSILON) {
                Solution solution = new Solution("identity");
                solution.steps.add("等式在实数范围内恒成立。");
                solution.steps.add("所有实数都是方程的解。");
                return solution;
            }
            throw new CalcEngine.CalcException("方程没有解");
        }
        if (degree == 1) {
            double c = coefficients[0];
            double b = coefficients[1];
            Solution solution = new Solution("linear");
            double root = clean(-c / b);
            solution.roots.add(new Root(root, 0));
            solution.steps.add("移项：x = −c / b。");
            solution.steps.add("代入系数后得到 x = " + number(root) + "。");
            return solution;
        }
        if (degree == 2) {
            double c = coefficients[0];
            double b = coefficients[1];
            double a = coefficients[2];
            double d = clean(b * b - 4 * a * c);
            double vertexX = clean(-b / (2 * a));
            double vertexY = clean(a * vertexX * vertexX + b * vertexX + c);
            Solution solution = new Solution(d < -EPSILON ? "quadratic-complex" : Math.abs(d) < EPSILON ? "quadratic-double" : "quadratic");
            solution.discriminant = d;
            solution.vertexX = vertexX;
            solution.vertexY = vertexY;
            solution.steps.add("整理为标准形式：" + polynomialString(coefficients) + " = 0。");
            solution.steps.add("判别式 Δ = b² − 4ac = " + number(d) + "。");
            if (d < -EPSILON) {
                double real = clean(-b / (2 * a));
                double imaginary = Math.sqrt(-d) / Math.abs(2 * a);
                solution.roots.add(new Root(real, imaginary));
                solution.roots.add(new Root(real, -imaginary));
                solution.steps.add("Δ < 0，因此有两个共轭复根。");
                solution.steps.add("x = " + complex(real, imaginary) + " 或 " + complex(real, -imaginary) + "。");
            } else if (Math.abs(d) < EPSILON) {
                double root = clean(-b / (2 * a));
                solution.roots.add(new Root(root, 0));
                solution.steps.add("Δ = 0，因此有一个二重实根。");
                solution.steps.add("x = −b / 2a = " + number(root) + "。");
            } else {
                double sqrt = Math.sqrt(d);
                solution.roots.add(new Root(clean((-b + sqrt) / (2 * a)), 0));
                solution.roots.add(new Root(clean((-b - sqrt) / (2 * a)), 0));
                solution.steps.add("Δ > 0，因此有两个不同实根。");
                solution.steps.add("代入求根公式 x = (−b ± √Δ) / 2a。");
            }
            return solution;
        }
        Solution solution = new Solution("polynomial");
        for (Root root : durandKerner(coefficients)) solution.roots.add(root);
        solution.steps.add("整理为 " + degree + " 次多项式：" + polynomialString(coefficients) + " = 0。");
        solution.steps.add("使用复平面 Durand–Kerner 数值迭代求根，并对接近实轴的根取实数。");
        for (Root root : solution.roots) {
            solution.steps.add("根：" + (Math.abs(root.imaginary) < 1e-8
                    ? number(root.real)
                    : complex(root.real, root.imaginary)));
        }
        return solution;
    }

    private static Polynomial fitPolynomial(String[] sides, String variable) throws CalcEngine.CalcException {
        for (int degree = 0; degree <= 6; degree++) {
            double[] coefficients = new double[degree + 1];
            double[][] augmented = new double[degree + 1][degree + 2];
            for (int row = 0; row <= degree; row++) {
                double x = row;
                for (int col = 0; col <= degree; col++) augmented[row][col] = Math.pow(x, col);
                augmented[row][degree + 1] = at(sides, variable, x);
            }
            if (degree == 0) {
                coefficients[0] = augmented[0][1];
            } else {
                coefficients = solveLinear(augmented);
            }
            boolean valid = true;
            for (double probe : new double[]{-3, -2, -1, degree + 1, degree + 2, degree + 3, 7}) {
                double expected = polynomialValue(coefficients, probe);
                double actual = at(sides, variable, probe);
                if (!Double.isFinite(actual) || !Double.isFinite(expected)
                        || Math.abs(expected - actual) > 1e-6 * Math.max(1, Math.abs(actual))) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                int actualDegree = degree;
                while (actualDegree > 0 && Math.abs(coefficients[actualDegree]) < 1e-9) actualDegree--;
                if (actualDegree != degree) {
                    double[] trimmed = new double[actualDegree + 1];
                    System.arraycopy(coefficients, 0, trimmed, 0, trimmed.length);
                    coefficients = trimmed;
                }
                return new Polynomial(coefficients);
            }
        }
        throw new CalcEngine.CalcException("未能将方程整理为 6 次以内多项式");
    }

    private static double[] solveLinear(double[][] input) throws CalcEngine.CalcException {
        int n = input.length;
        for (int col = 0; col < n; col++) {
            int pivot = col;
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(input[row][col]) > Math.abs(input[pivot][col])) pivot = row;
            }
            if (Math.abs(input[pivot][col]) < 1e-12) throw new CalcEngine.CalcException("方程拟合失败");
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

    private static Root[] durandKerner(double[] coefficients) throws CalcEngine.CalcException {
        int degree = coefficients.length - 1;
        double leading = coefficients[degree];
        double[] monic = new double[degree + 1];
        for (int i = 0; i <= degree; i++) monic[i] = coefficients[i] / leading;
        double radius = 1;
        for (int i = 0; i < degree; i++) radius = Math.max(radius, 1 + Math.abs(monic[i]));
        Complex[] roots = new Complex[degree];
        for (int i = 0; i < degree; i++) {
            double angle = 2 * Math.PI * i / degree + 0.4;
            roots[i] = new Complex(radius * Math.cos(angle), radius * Math.sin(angle));
        }
        for (int iteration = 0; iteration < 1000; iteration++) {
            boolean converged = true;
            for (int i = 0; i < degree; i++) {
                Complex denominator = new Complex(1, 0);
                for (int j = 0; j < degree; j++) {
                    if (i != j) denominator = denominator.multiply(roots[i].subtract(roots[j]));
                }
                if (denominator.abs() < 1e-15) denominator = new Complex(1e-15, 1e-15);
                Complex delta = polynomial(monic, roots[i]).divide(denominator);
                roots[i] = roots[i].subtract(delta);
                if (delta.abs() > 1e-12) converged = false;
            }
            if (converged) break;
        }
        Root[] result = new Root[degree];
        for (int i = 0; i < degree; i++) {
            result[i] = Math.abs(roots[i].imaginary) < 1e-7
                    ? new Root(clean(roots[i].real), 0)
                    : new Root(clean(roots[i].real), clean(roots[i].imaginary));
        }
        return result;
    }

    private static Complex polynomial(double[] coefficients, Complex x) {
        Complex result = new Complex(0, 0);
        for (int i = coefficients.length - 1; i >= 0; i--) {
            result = result.multiply(x).add(new Complex(coefficients[i], 0));
        }
        return result;
    }

    private static double polynomialValue(double[] coefficients, double x) {
        double result = 0;
        for (int i = coefficients.length - 1; i >= 0; i--) result = result * x + coefficients[i];
        return result;
    }

    private static String polynomialString(double[] coefficients) {
        StringBuilder builder = new StringBuilder();
        int degree = coefficients.length - 1;
        for (int power = degree; power >= 0; power--) {
            double coefficient = coefficients[power];
            if (Math.abs(coefficient) < 1e-10) continue;
            if (builder.length() > 0) builder.append(coefficient >= 0 ? " + " : " − ");
            else if (coefficient < 0) builder.append('−');
            double absolute = Math.abs(coefficient);
            if (Math.abs(absolute - 1) > 1e-10 || power == 0) builder.append(number(absolute));
            if (power > 0) builder.append("x");
            if (power > 1) builder.append('^').append(power);
        }
        return builder.length() == 0 ? "0" : builder.toString();
    }

    private static final class Polynomial {
        final double[] coefficients;
        final int degree;

        Polynomial(double[] coefficients) {
            this.coefficients = coefficients;
            this.degree = coefficients.length - 1;
        }
    }

    private static final class Complex {
        final double real;
        final double imaginary;

        Complex(double real, double imaginary) {
            this.real = real;
            this.imaginary = imaginary;
        }

        Complex add(Complex other) {
            return new Complex(real + other.real, imaginary + other.imaginary);
        }

        Complex subtract(Complex other) {
            return new Complex(real - other.real, imaginary - other.imaginary);
        }

        Complex multiply(Complex other) {
            return new Complex(real * other.real - imaginary * other.imaginary,
                    real * other.imaginary + imaginary * other.real);
        }

        Complex divide(Complex other) {
            double denominator = other.real * other.real + other.imaginary * other.imaginary;
            return new Complex(
                    (real * other.real + imaginary * other.imaginary) / denominator,
                    (imaginary * other.real - real * other.imaginary) / denominator
            );
        }

        double abs() {
            return Math.hypot(real, imaginary);
        }
    }

    private static double at(String[] sides, String variable, double value) throws CalcEngine.CalcException {
        java.util.Map<String, Double> vars = new java.util.HashMap<>();
        vars.put(variable, value);
        CalcEngine.Context context = new CalcEngine.Context("deg", 10, vars, 0);
        return CalcEngine.evaluate(sides[0], context).value - CalcEngine.evaluate(sides[1], context).value;
    }

    private static double clean(double value) {
        return Math.abs(value) < 1e-13 ? 0 : value;
    }

    private static String number(double value) {
        return CalcEngine.formatNumber(value, 10, "auto", false, false);
    }

    private static String complex(double real, double imaginary) {
        return number(real) + (imaginary >= 0 ? " + " : " − ") + number(Math.abs(imaginary)) + "i";
    }
}
