package app.hipercalc.replica;

import java.util.HashMap;
import java.util.Map;

public final class CalcEngineTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void near(double actual, double expected, double tolerance, String message) {
        check(Math.abs(actual - expected) <= tolerance, message + ": expected=" + expected + " actual=" + actual);
    }

    public static void main(String[] args) throws Exception {
        CalcEngine.Context decimal = CalcEngine.Context.defaults();
        CalcEngine.Result exact = CalcEngine.evaluate("123456789012345678901234567890+1", decimal);
        check(exact.exact != null, "high precision exact result");
        check(exact.exact.toPlainString().equals("123456789012345678901234567891"), "high precision addition");
        check(CalcEngine.evaluate("50!", decimal).exact != null, "high precision factorial");
        CalcEngine.Result highSin = CalcEngine.evaluate("sin(30)", decimal);
        check(highSin.exact != null, "high precision trig path");
        near(highSin.value, .5, 1e-14, "high precision sine");
        CalcEngine.Result highSqrt = CalcEngine.evaluate("sqrt(2)", decimal);
        check(highSqrt.exact != null, "high precision sqrt path");
        check(highSqrt.exact.toPlainString().startsWith("1.4142135623730950488"), "high precision sqrt digits");
        check(CalcEngine.evaluate("2+3*4", decimal).value == 14, "precedence");
        check(CalcEngine.evaluate("(2+3)*4", decimal).value == 20, "parentheses");
        check(CalcEngine.evaluate("-2^2", decimal).value == -4, "unary power");
        near(CalcEngine.evaluate("sin(30)", decimal).value, .5, 1e-12, "degrees");
        near(CalcEngine.evaluate("sin(pi/2)", new CalcEngine.Context("rad", 10, null, 0)).value, 1, 1e-12, "radians");
        near(CalcEngine.evaluate("log(1000)", decimal).value, 3, 1e-12, "log");
        check(CalcEngine.evaluate("5!", decimal).value == 120, "factorial");
        check(CalcEngine.evaluate("nCr(10,3)", decimal).value == 120, "combination");
        check(CalcEngine.evaluate("gcd(84,30)", decimal).value == 6, "gcd");
        check(CalcEngine.evaluate("1010", new CalcEngine.Context("deg", 2, null, 0)).value == 10, "binary");
        check(CalcEngine.evaluate("FF", new CalcEngine.Context("deg", 16, null, 0)).value == 255, "hex");
        near(CalcEngine.evaluate("derivative(\"x^2\",3)", decimal).value, 6, 1e-5, "derivative");
        near(CalcEngine.evaluate("integral(\"x^2\",0,3)", decimal).value, 9, 1e-4, "integral");

        Map<String, Double> vars = new HashMap<>();
        vars.put("a", 3d);
        vars.put("b", 4d);
        near(CalcEngine.evaluate("a*b+Ans", new CalcEngine.Context("deg", 10, vars, 5)).value, 17, 1e-12, "variables");

        UnitCatalog.Category temp = UnitCatalog.category("temperature");
        near(UnitCatalog.convert(temp, UnitCatalog.unit(temp, "c"), UnitCatalog.unit(temp, "f"), 0), 32, 1e-12, "temperature");
        UnitCatalog.Category length = UnitCatalog.category("length");
        near(UnitCatalog.convert(length, UnitCatalog.unit(length, "m"), UnitCatalog.unit(length, "ft"), 1), 3.280839895, 1e-9, "length");

        EquationSolver.Solution quadratic = EquationSolver.solve("x^2-5x+6=0", "x");
        check(quadratic.roots.size() == 2, "quadratic roots");
        near(quadratic.roots.get(0).real, 3, 1e-10, "quadratic root 1");
        near(quadratic.roots.get(1).real, 2, 1e-10, "quadratic root 2");
        EquationSolver.Solution linear = EquationSolver.solve("2x+3=7", "x");
        near(linear.roots.get(0).real, 2, 1e-10, "linear root");
        EquationSolver.Solution cubic = EquationSolver.solve("x^3-6x^2+11x-6=0", "x");
        check(cubic.roots.size() == 3, "cubic roots");
        double[] cubicRoots = cubic.roots.stream().mapToDouble(root -> root.real).sorted().toArray();
        near(cubicRoots[0], 1, 1e-6, "cubic root 1");
        near(cubicRoots[1], 2, 1e-6, "cubic root 2");
        near(cubicRoots[2], 3, 1e-6, "cubic root 3");

        double[][] matrix = MatrixMath.parse("2,1;1,3");
        near(MatrixMath.determinant(matrix), 5, 1e-12, "matrix determinant");
        double[][] inverse = MatrixMath.inverse(matrix);
        near(inverse[0][0], .6, 1e-12, "matrix inverse 00");
        near(inverse[1][1], .4, 1e-12, "matrix inverse 11");
        check(MatrixMath.rank(matrix) == 2, "matrix rank");
        near(MatrixMath.dot(new double[]{1, 2, 3}, new double[]{4, 5, 6}), 32, 1e-12, "dot product");
        MatrixMath.EigenResult eigen = MatrixMath.eigenSymmetric(new double[][]{{2, 1}, {1, 2}});
        double[] eigenValues = eigen.values.clone();
        java.util.Arrays.sort(eigenValues);
        near(eigenValues[0], 1, 1e-8, "eigenvalue 1");
        near(eigenValues[1], 3, 1e-8, "eigenvalue 3");
        MatrixMath.EigenValue[] complexEigen = MatrixMath.eigenvalues(new double[][]{{0, -1}, {1, 0}});
        check(complexEigen.length == 2, "general eigenvalues count");
        near(complexEigen[0].imaginary, -1, 1e-6, "general complex eigenvalue -i");
        near(complexEigen[1].imaginary, 1, 1e-6, "general complex eigenvalue i");

        double[] data = {1, 2, 3, 4};
        near(StatisticsMath.mean(data), 2.5, 1e-12, "mean");
        near(StatisticsMath.median(data), 2.5, 1e-12, "median");
        near(StatisticsMath.varianceSample(data), 5.0 / 3, 1e-12, "sample variance");
        StatisticsMath.Regression regression = StatisticsMath.linear(new double[]{1, 2, 3}, new double[]{3, 5, 7});
        near(regression.coefficients[0], 2, 1e-12, "linear slope");
        near(regression.coefficients[1], 1, 1e-12, "linear intercept");

        System.out.println("Native engine tests passed.");
    }
}
