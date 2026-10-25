package app.hipercalc.replica;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MatrixMath {
    private MatrixMath() {
    }

    public static double[][] parse(String input) throws CalcEngine.CalcException {
        String source = input == null ? "" : input.trim()
                .replace('[', ' ')
                .replace(']', ' ')
                .replace('，', ',')
                .replace('；', ';');
        if (source.isEmpty()) throw new CalcEngine.CalcException("请输入矩阵");
        String[] rowText = source.split("[;\\n]+");
        List<double[]> rows = new ArrayList<>();
        int columns = -1;
        for (String row : rowText) {
            if (row.trim().isEmpty()) continue;
            String[] cellText = row.trim().split("[,\\s]+");
            double[] cells = new double[cellText.length];
            for (int i = 0; i < cellText.length; i++) {
                try {
                    cells[i] = Double.parseDouble(cellText[i]);
                } catch (NumberFormatException error) {
                    throw new CalcEngine.CalcException("矩阵元素不是数值：" + cellText[i]);
                }
            }
            if (columns == -1) columns = cells.length;
            if (columns != cells.length) throw new CalcEngine.CalcException("矩阵每行列数必须一致");
            rows.add(cells);
        }
        if (rows.isEmpty() || columns <= 0) throw new CalcEngine.CalcException("矩阵不能为空");
        return rows.toArray(new double[0][]);
    }

    public static String format(double[][] matrix, int precision) {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < matrix.length; row++) {
            if (row > 0) builder.append('\n');
            builder.append('[');
            for (int col = 0; col < matrix[row].length; col++) {
                if (col > 0) builder.append(", ");
                builder.append(CalcEngine.formatNumber(matrix[row][col], precision, "auto", false, false));
            }
            builder.append(']');
        }
        return builder.toString();
    }

    public static double[][] identity(int size) {
        double[][] result = new double[size][size];
        for (int i = 0; i < size; i++) result[i][i] = 1;
        return result;
    }

    public static double[][] add(double[][] a, double[][] b, double sign) throws CalcEngine.CalcException {
        requireSame(a, b);
        double[][] result = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) result[i][j] = a[i][j] + sign * b[i][j];
        }
        return result;
    }

    public static double[][] multiply(double[][] a, double[][] b) throws CalcEngine.CalcException {
        if (a[0].length != b.length) throw new CalcEngine.CalcException("矩阵乘法要求 A 的列数等于 B 的行数");
        double[][] result = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                double sum = 0;
                for (int k = 0; k < b.length; k++) sum += a[i][k] * b[k][j];
                result[i][j] = sum;
            }
        }
        return result;
    }

    public static double[][] hadamard(double[][] a, double[][] b) throws CalcEngine.CalcException {
        requireSame(a, b);
        double[][] result = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) result[i][j] = a[i][j] * b[i][j];
        }
        return result;
    }

    public static double[][] kronecker(double[][] a, double[][] b) {
        double[][] result = new double[a.length * b.length][a[0].length * b[0].length];
        for (int ai = 0; ai < a.length; ai++) {
            for (int aj = 0; aj < a[0].length; aj++) {
                for (int bi = 0; bi < b.length; bi++) {
                    for (int bj = 0; bj < b[0].length; bj++) {
                        result[ai * b.length + bi][aj * b[0].length + bj] = a[ai][aj] * b[bi][bj];
                    }
                }
            }
        }
        return result;
    }

    public static double[][] transpose(double[][] a) {
        double[][] result = new double[a[0].length][a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) result[j][i] = a[i][j];
        }
        return result;
    }

    public static double trace(double[][] a) {
        double result = 0;
        for (int i = 0; i < Math.min(a.length, a[0].length); i++) result += a[i][i];
        return result;
    }

    public static double determinant(double[][] source) throws CalcEngine.CalcException {
        square(source);
        double[][] a = copy(source);
        int n = a.length;
        double result = 1;
        for (int col = 0; col < n; col++) {
            int pivot = col;
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(a[row][col]) > Math.abs(a[pivot][col])) pivot = row;
            }
            if (Math.abs(a[pivot][col]) < 1e-12) return 0;
            if (pivot != col) {
                double[] temp = a[pivot];
                a[pivot] = a[col];
                a[col] = temp;
                result = -result;
            }
            double diagonal = a[col][col];
            result *= diagonal;
            for (int row = col + 1; row < n; row++) {
                double factor = a[row][col] / diagonal;
                for (int j = col + 1; j < n; j++) a[row][j] -= factor * a[col][j];
            }
        }
        return Math.abs(result) < 1e-12 ? 0 : result;
    }

    public static int rank(double[][] source) {
        Reduced reduced = reduce(source, false);
        int rank = 0;
        for (double[] row : reduced.matrix) {
            boolean nonzero = false;
            for (double value : row) if (Math.abs(value) > 1e-10) {
                nonzero = true;
                break;
            }
            if (nonzero) rank++;
        }
        return rank;
    }

    public static double[][] rowEchelon(double[][] source) {
        return reduce(source, false).matrix;
    }

    public static double[][] rowReduced(double[][] source) {
        return reduce(source, true).matrix;
    }

    public static double[][] inverse(double[][] source) throws CalcEngine.CalcException {
        square(source);
        int n = source.length;
        double[][] augmented = new double[n][n * 2];
        for (int i = 0; i < n; i++) {
            System.arraycopy(source[i], 0, augmented[i], 0, n);
            augmented[i][n + i] = 1;
        }
        double[][] reduced = reduce(augmented, true).matrix;
        double[][] inverse = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) inverse[i][j] = reduced[i][n + j];
        }
        double[][] check = multiply(source, inverse);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (Math.abs(check[i][j] - (i == j ? 1 : 0)) > 1e-7) {
                    throw new CalcEngine.CalcException("矩阵不可逆");
                }
            }
        }
        return inverse;
    }

    public static double[][] minors(double[][] source) throws CalcEngine.CalcException {
        square(source);
        int n = source.length;
        double[][] result = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) result[i][j] = determinant(submatrix(source, i, j));
        }
        return result;
    }

    public static double[][] cofactors(double[][] source) throws CalcEngine.CalcException {
        double[][] minors = minors(source);
        int n = minors.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) minors[i][j] *= ((i + j) & 1) == 0 ? 1 : -1;
        }
        return minors;
    }

    public static double[][] adjugate(double[][] source) throws CalcEngine.CalcException {
        return transpose(cofactors(source));
    }

    public static EigenResult eigenSymmetric(double[][] source) throws CalcEngine.CalcException {
        square(source);
        int n = source.length;
        if (n == 1) return new EigenResult(new double[]{source[0][0]}, identity(1));
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (Math.abs(source[i][j] - source[j][i]) > 1e-8) {
                    throw new CalcEngine.CalcException("当前特征分解要求实对称矩阵");
                }
            }
        }
        double[][] a = copy(source);
        double[][] vectors = identity(n);
        for (int iteration = 0; iteration < 2000; iteration++) {
            int p = 0;
            int q = 1;
            double max = 0;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (Math.abs(a[i][j]) > max) {
                        max = Math.abs(a[i][j]);
                        p = i;
                        q = j;
                    }
                }
            }
            if (max < 1e-12) break;
            double angle = 0.5 * Math.atan2(2 * a[p][q], a[q][q] - a[p][p]);
            double cosine = Math.cos(angle);
            double sine = Math.sin(angle);
            for (int i = 0; i < n; i++) {
                double aip = a[i][p];
                double aiq = a[i][q];
                a[i][p] = cosine * aip - sine * aiq;
                a[i][q] = sine * aip + cosine * aiq;
            }
            for (int j = 0; j < n; j++) {
                double apj = a[p][j];
                double aqj = a[q][j];
                a[p][j] = cosine * apj - sine * aqj;
                a[q][j] = sine * apj + cosine * aqj;
            }
            for (int i = 0; i < n; i++) {
                double vip = vectors[i][p];
                double viq = vectors[i][q];
                vectors[i][p] = cosine * vip - sine * viq;
                vectors[i][q] = sine * vip + cosine * viq;
            }
        }
        double[] values = new double[n];
        for (int i = 0; i < n; i++) values[i] = Math.abs(a[i][i]) < 1e-12 ? 0 : a[i][i];
        double[][] expected = multiply(source, vectors);
        double[][] actual = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) actual[i][j] = vectors[i][j] * values[j];
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (Math.abs(expected[i][j] - actual[i][j]) > 1e-6) {
                    throw new CalcEngine.CalcException("特征向量收敛失败");
                }
            }
        }
        return new EigenResult(values, vectors);
    }

    public static double[][] submatrix(double[][] source, int removeRow, int removeColumn) {
        int rows = source.length - 1;
        int cols = source[0].length - 1;
        double[][] result = new double[rows][cols];
        int ri = 0;
        for (int i = 0; i < source.length; i++) {
            if (i == removeRow) continue;
            int ci = 0;
            for (int j = 0; j < source[0].length; j++) {
                if (j == removeColumn) continue;
                result[ri][ci++] = source[i][j];
            }
            ri++;
        }
        return result;
    }

    public static double[] characteristicPolynomial(double[][] source) throws CalcEngine.CalcException {
        square(source);
        int n = source.length;
        double[] coefficients = new double[n + 1];
        coefficients[0] = 1;
        double[][] current = identity(n);
        for (int k = 1; k <= n; k++) {
            double[][] product = multiply(source, current);
            double trace = trace(product);
            coefficients[k] = -trace / k;
            for (int i = 0; i < n; i++) product[i][i] += coefficients[k];
            current = product;
        }
        return coefficients;
    }

    public static EigenValue[] eigenvalues(double[][] source) throws CalcEngine.CalcException {
        square(source);
        if (source.length > 6) throw new CalcEngine.CalcException("特征多项式求根当前支持 6×6 以内矩阵");
        double[] ascending = characteristicPolynomial(source);
        int degree = ascending.length - 1;
        if (degree == 0) return new EigenValue[0];
        double[] descending = new double[ascending.length];
        for (int i = 0; i < ascending.length; i++) descending[i] = ascending[degree - i];
        double leading = descending[0];
        if (Math.abs(leading) < 1e-14) throw new CalcEngine.CalcException("特征多项式退化");
        for (int i = 0; i < descending.length; i++) descending[i] /= leading;
        double radius = 1;
        for (int i = 1; i < descending.length; i++) radius = Math.max(radius, 1 + Math.abs(descending[i]));
        ComplexValue[] roots = new ComplexValue[degree];
        for (int i = 0; i < degree; i++) {
            double angle = 2 * Math.PI * i / degree + 0.4;
            roots[i] = new ComplexValue(radius * Math.cos(angle), radius * Math.sin(angle));
        }
        for (int iteration = 0; iteration < 1500; iteration++) {
            boolean converged = true;
            for (int i = 0; i < degree; i++) {
                ComplexValue denominator = new ComplexValue(1, 0);
                for (int j = 0; j < degree; j++) {
                    if (i != j) denominator = denominator.multiply(roots[i].subtract(roots[j]));
                }
                if (denominator.abs() < 1e-15) denominator = new ComplexValue(1e-15, 1e-15);
                ComplexValue delta = evaluatePolynomial(descending, roots[i]).divide(denominator);
                roots[i] = roots[i].subtract(delta);
                if (delta.abs() > 1e-12) converged = false;
            }
            if (converged) break;
        }
        EigenValue[] values = new EigenValue[degree];
        for (int i = 0; i < degree; i++) {
            values[i] = new EigenValue(
                    Math.abs(roots[i].real) < 1e-9 ? 0 : roots[i].real,
                    Math.abs(roots[i].imaginary) < 1e-7 ? 0 : roots[i].imaginary
            );
        }
        java.util.Arrays.sort(values, (a, b) -> {
            int real = Double.compare(a.real, b.real);
            return real != 0 ? real : Double.compare(a.imaginary, b.imaginary);
        });
        return values;
    }

    public static String polynomialString(double[] coefficients) {
        StringBuilder builder = new StringBuilder();
        int degree = coefficients.length - 1;
        for (int i = 0; i < coefficients.length; i++) {
            double value = coefficients[i];
            if (Math.abs(value) < 1e-10) continue;
            if (builder.length() > 0) builder.append(value >= 0 ? " + " : " - ");
            else if (value < 0) builder.append('-');
            double absolute = Math.abs(value);
            if (Math.abs(absolute - 1) > 1e-10 || degree - i == 0) {
                builder.append(CalcEngine.formatNumber(absolute, 10, "auto", false, false));
            }
            int power = degree - i;
            if (power > 0) builder.append('λ');
            if (power > 1) builder.append('^').append(power);
        }
        if (builder.length() == 0) builder.append('0');
        return builder.toString();
    }

    public static double dot(double[] a, double[] b) throws CalcEngine.CalcException {
        if (a.length != b.length) throw new CalcEngine.CalcException("向量的维数必须相同");
        double result = 0;
        for (int i = 0; i < a.length; i++) result += a[i] * b[i];
        return result;
    }

    public static double[] cross(double[] a, double[] b) throws CalcEngine.CalcException {
        if (a.length != 3 || b.length != 3) throw new CalcEngine.CalcException("叉积要求两个三维向量");
        return new double[]{
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]
        };
    }

    public static double distance(double[] a, double[] b) throws CalcEngine.CalcException {
        if (a.length != b.length) throw new CalcEngine.CalcException("向量的维数必须相同");
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double delta = a[i] - b[i];
            sum += delta * delta;
        }
        return Math.sqrt(sum);
    }

    private static Reduced reduce(double[][] source, boolean reduced) {
        double[][] a = copy(source);
        int rows = a.length;
        int cols = a[0].length;
        int pivotRow = 0;
        for (int col = 0; col < cols && pivotRow < rows; col++) {
            int pivot = pivotRow;
            for (int row = pivotRow + 1; row < rows; row++) {
                if (Math.abs(a[row][col]) > Math.abs(a[pivot][col])) pivot = row;
            }
            if (Math.abs(a[pivot][col]) < 1e-12) continue;
            double[] temp = a[pivot];
            a[pivot] = a[pivotRow];
            a[pivotRow] = temp;
            double divisor = a[pivotRow][col];
            for (int j = 0; j < cols; j++) a[pivotRow][j] /= divisor;
            for (int row = 0; row < rows; row++) {
                if (row == pivotRow) continue;
                if (!reduced && row < pivotRow) continue;
                double factor = a[row][col];
                if (Math.abs(factor) < 1e-14) continue;
                for (int j = 0; j < cols; j++) a[row][j] -= factor * a[pivotRow][j];
            }
            pivotRow++;
        }
        int rank = pivotRow;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) if (Math.abs(a[i][j]) < 1e-11) a[i][j] = 0;
        }
        return new Reduced(a, rank);
    }

    private static void requireSame(double[][] a, double[][] b) throws CalcEngine.CalcException {
        if (a.length != b.length || a[0].length != b[0].length) throw new CalcEngine.CalcException("矩阵维数必须相同");
    }

    private static void square(double[][] matrix) throws CalcEngine.CalcException {
        if (matrix.length != matrix[0].length) throw new CalcEngine.CalcException("该运算要求方阵");
    }

    private static double[][] copy(double[][] source) {
        double[][] result = new double[source.length][];
        for (int i = 0; i < source.length; i++) result[i] = source[i].clone();
        return result;
    }

    private static ComplexValue evaluatePolynomial(double[] coefficients, ComplexValue x) {
        ComplexValue result = new ComplexValue(0, 0);
        for (double coefficient : coefficients) result = result.multiply(x).add(new ComplexValue(coefficient, 0));
        return result;
    }

    private static final class Reduced {
        final double[][] matrix;
        final int rank;

        Reduced(double[][] matrix, int rank) {
            this.matrix = matrix;
            this.rank = rank;
        }
    }

    public static final class EigenResult {
        public final double[] values;
        public final double[][] vectors;

        EigenResult(double[] values, double[][] vectors) {
            this.values = values;
            this.vectors = vectors;
        }

        public double[][] diagonal() {
            double[][] result = new double[values.length][values.length];
            for (int i = 0; i < values.length; i++) result[i][i] = values[i];
            return result;
        }
    }

    public static final class EigenValue {
        public final double real;
        public final double imaginary;

        EigenValue(double real, double imaginary) {
            this.real = real;
            this.imaginary = imaginary;
        }
    }

    private static final class ComplexValue {
        final double real;
        final double imaginary;

        ComplexValue(double real, double imaginary) {
            this.real = real;
            this.imaginary = imaginary;
        }

        ComplexValue add(ComplexValue other) {
            return new ComplexValue(real + other.real, imaginary + other.imaginary);
        }

        ComplexValue subtract(ComplexValue other) {
            return new ComplexValue(real - other.real, imaginary - other.imaginary);
        }

        ComplexValue multiply(ComplexValue other) {
            return new ComplexValue(real * other.real - imaginary * other.imaginary,
                    real * other.imaginary + imaginary * other.real);
        }

        ComplexValue divide(ComplexValue other) {
            double denominator = other.real * other.real + other.imaginary * other.imaginary;
            return new ComplexValue(
                    (real * other.real + imaginary * other.imaginary) / denominator,
                    (imaginary * other.real - real * other.imaginary) / denominator
            );
        }

        double abs() {
            return Math.hypot(real, imaginary);
        }
    }
}
