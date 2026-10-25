package app.hipercalc.replica;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

final class BigDecimalMath {
    static final MathContext MC = new MathContext(180, RoundingMode.HALF_EVEN);
    static final BigDecimal PI = new BigDecimal(
            "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664709384460955058223172535940812848111745028410270193852110555964462294895493038196");
    static final BigDecimal E = new BigDecimal(
            "2.71828182845904523536028747135266249775724709369995957496696762772407663035354759457138217852516642742746639193200305992181741359662904357290033429526059563073813232862794349076323382988075319525101901");
    static final BigDecimal TWO_PI = PI.multiply(BigDecimal.valueOf(2), MC);
    private static final BigDecimal LN2 = lnSeries(new BigDecimal("0.333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333"));

    private BigDecimalMath() {
    }

    static BigDecimal sqrt(BigDecimal value) {
        if (value.signum() < 0) throw new ArithmeticException("sqrt domain");
        if (value.signum() == 0) return BigDecimal.ZERO;
        BigDecimal x = value.compareTo(BigDecimal.ONE) >= 0
                ? value.divide(BigDecimal.valueOf(2), MC)
                : BigDecimal.ONE;
        for (int i = 0; i < 12; i++) {
            x = x.add(value.divide(x, MC), MC).divide(BigDecimal.valueOf(2), MC);
        }
        return x;
    }

    static BigDecimal cbrt(BigDecimal value) {
        if (value.signum() == 0) return BigDecimal.ZERO;
        boolean negative = value.signum() < 0;
        BigDecimal target = value.abs();
        BigDecimal x = target.compareTo(BigDecimal.ONE) >= 0
                ? target.divide(BigDecimal.valueOf(3), MC) : BigDecimal.ONE;
        for (int i = 0; i < 16; i++) {
            BigDecimal x2 = x.multiply(x, MC);
            x = x2.multiply(BigDecimal.valueOf(2)).add(target.divide(x2, MC)).divide(BigDecimal.valueOf(3), MC);
        }
        return negative ? x.negate() : x;
    }

    static BigDecimal exp(BigDecimal value) {
        if (value.signum() == 0) return BigDecimal.ONE;
        if (value.signum() < 0) return BigDecimal.ONE.divide(exp(value.negate()), MC);
        int k = value.divide(LN2, 0, RoundingMode.FLOOR).intValue();
        BigDecimal reduced = value.subtract(LN2.multiply(BigDecimal.valueOf(k), MC), MC);
        BigDecimal result = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;
        for (int n = 1; n < 300; n++) {
            term = term.multiply(reduced, MC).divide(BigDecimal.valueOf(n), MC);
            result = result.add(term, MC);
            if (term.abs().compareTo(new BigDecimal("1E-175")) < 0) break;
        }
        BigDecimal two = BigDecimal.valueOf(2);
        for (int i = 0; i < k; i++) result = result.multiply(two, MC);
        return result;
    }

    static BigDecimal ln(BigDecimal value) {
        if (value.signum() <= 0) throw new ArithmeticException("ln domain");
        int power = 0;
        BigDecimal x = value;
        BigDecimal two = BigDecimal.valueOf(2);
        while (x.compareTo(new BigDecimal("1.25")) > 0) {
            x = x.divide(two, MC);
            power++;
        }
        while (x.compareTo(new BigDecimal("0.8")) < 0) {
            x = x.multiply(two, MC);
            power--;
        }
        BigDecimal result = lnSeries(x.subtract(BigDecimal.ONE, MC).divide(x.add(BigDecimal.ONE, MC), MC));
        if (power != 0) result = result.add(LN2.multiply(BigDecimal.valueOf(power), MC), MC);
        return result;
    }

    private static BigDecimal lnSeries(BigDecimal z) {
        BigDecimal z2 = z.multiply(z, MC);
        BigDecimal term = z;
        BigDecimal sum = BigDecimal.ZERO;
        for (int n = 0; n < 600; n++) {
            BigDecimal part = term.divide(BigDecimal.valueOf(2L * n + 1), MC);
            sum = sum.add(part, MC);
            if (part.abs().compareTo(new BigDecimal("1E-176")) < 0) break;
            term = term.multiply(z2, MC);
        }
        return sum.multiply(BigDecimal.valueOf(2), MC);
    }

    static BigDecimal sin(BigDecimal radians) {
        BigDecimal x = radians.remainder(TWO_PI, MC);
        BigDecimal x2 = x.multiply(x, MC);
        BigDecimal term = x;
        BigDecimal sum = x;
        for (int n = 1; n < 300; n++) {
            term = term.multiply(x2, MC)
                    .divide(BigDecimal.valueOf((2L * n) * (2L * n + 1)), MC)
                    .negate();
            sum = sum.add(term, MC);
            if (term.abs().compareTo(new BigDecimal("1E-176")) < 0) break;
        }
        return sum;
    }

    static BigDecimal cos(BigDecimal radians) {
        BigDecimal x = radians.remainder(TWO_PI, MC);
        BigDecimal x2 = x.multiply(x, MC);
        BigDecimal term = BigDecimal.ONE;
        BigDecimal sum = BigDecimal.ONE;
        for (int n = 1; n < 300; n++) {
            term = term.multiply(x2, MC)
                    .divide(BigDecimal.valueOf((2L * n - 1) * (2L * n)), MC)
                    .negate();
            sum = sum.add(term, MC);
            if (term.abs().compareTo(new BigDecimal("1E-176")) < 0) break;
        }
        return sum;
    }

    static BigDecimal tan(BigDecimal radians) {
        BigDecimal cosine = cos(radians);
        if (cosine.abs().compareTo(new BigDecimal("1E-170")) < 0) throw new ArithmeticException("tan pole");
        return sin(radians).divide(cosine, MC);
    }

    static BigDecimal atan(BigDecimal value) {
        if (value.signum() < 0) return atan(value.negate()).negate();
        if (value.compareTo(BigDecimal.ONE) > 0) {
            return PI.divide(BigDecimal.valueOf(2), MC).subtract(atan(BigDecimal.ONE.divide(value, MC)), MC);
        }
        BigDecimal x = value;
        int doublings = 0;
        BigDecimal threshold = new BigDecimal("0.2");
        while (x.compareTo(threshold) > 0) {
            x = x.divide(BigDecimal.ONE.add(sqrt(BigDecimal.ONE.add(x.multiply(x, MC), MC)), MC), MC);
            doublings++;
        }
        BigDecimal x2 = x.multiply(x, MC);
        BigDecimal term = x;
        BigDecimal sum = x;
        for (int n = 1; n < 500; n++) {
            term = term.multiply(x2, MC).negate();
            BigDecimal part = term.divide(BigDecimal.valueOf(2L * n + 1), MC);
            sum = sum.add(part, MC);
            if (part.abs().compareTo(new BigDecimal("1E-176")) < 0) break;
        }
        return sum.multiply(BigDecimal.valueOf(1L << doublings), MC);
    }

    static BigDecimal asin(BigDecimal value) {
        if (value.abs().compareTo(BigDecimal.ONE) > 0) throw new ArithmeticException("asin domain");
        if (value.abs().compareTo(BigDecimal.ONE) == 0) {
            BigDecimal halfPi = PI.divide(BigDecimal.valueOf(2), MC);
            return value.signum() < 0 ? halfPi.negate() : halfPi;
        }
        BigDecimal denominator = sqrt(BigDecimal.ONE.subtract(value.multiply(value, MC), MC));
        return atan(value.divide(denominator, MC));
    }

    static BigDecimal acos(BigDecimal value) {
        return PI.divide(BigDecimal.valueOf(2), MC).subtract(asin(value), MC);
    }

    static BigDecimal hyperbolicSin(BigDecimal value) {
        BigDecimal positive = exp(value);
        BigDecimal negative = exp(value.negate());
        return positive.subtract(negative, MC).divide(BigDecimal.valueOf(2), MC);
    }

    static BigDecimal hyperbolicCos(BigDecimal value) {
        BigDecimal positive = exp(value);
        BigDecimal negative = exp(value.negate());
        return positive.add(negative, MC).divide(BigDecimal.valueOf(2), MC);
    }

    static BigDecimal power(BigDecimal base, BigDecimal exponent) {
        if (exponent.stripTrailingZeros().scale() <= 0) {
            int n = exponent.intValueExact();
            if (n == 0) return BigDecimal.ONE;
            if (n > 0) return base.pow(n, MC);
            if (base.signum() == 0) throw new ArithmeticException("zero negative power");
            return BigDecimal.ONE.divide(base.pow(-n, MC), MC);
        }
        if (base.signum() <= 0) throw new ArithmeticException("non-integer power domain");
        return exp(exponent.multiply(ln(base), MC));
    }
}
