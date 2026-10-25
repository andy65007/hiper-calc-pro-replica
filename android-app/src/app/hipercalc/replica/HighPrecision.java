package app.hipercalc.replica;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Exact arithmetic path for expressions without transcendental functions.
 * The double engine remains the fallback for functions that require it.
 */
final class HighPrecision {
    static final class Unsupported extends Exception {
    }

    private enum Type {
        NUMBER, NAME, PLUS, MINUS, STAR, SLASH, POWER, LPAREN, RPAREN, COMMA, BANG, PERCENT, EOF
    }

    private static final class Token {
        final Type type;
        final String text;
        final BigDecimal number;

        Token(Type type, String text, BigDecimal number) {
            this.type = type;
            this.text = text;
            this.number = number;
        }
    }

    private static final MathContext MC = new MathContext(180, RoundingMode.HALF_EVEN);
    private static final BigDecimal PI = new BigDecimal(
            "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664709384460955058223172535940812848111745028410270193852110555964462294895493038196");
    private static final BigDecimal E = new BigDecimal(
            "2.71828182845904523536028747135266249775724709369995957496696762772407663035354759457138217852516642742746639193200305992181741359662904357290033429526059563073813232862794349076323382988075319525101901");

    private final List<Token> tokens;
    private final Map<String, BigDecimal> variables;
    private final String angle;
    private int position;

    private HighPrecision(String expression, CalcEngine.Context context) throws Unsupported {
        tokens = tokenize(expression, context.base);
        angle = context.angle;
        variables = new HashMap<>();
        for (Map.Entry<String, Double> entry : context.variables.entrySet()) {
            if (Double.isFinite(entry.getValue())) {
                variables.put(entry.getKey().toLowerCase(Locale.ROOT), BigDecimal.valueOf(entry.getValue()));
            }
        }
        variables.put("ans", BigDecimal.valueOf(context.ans));
        for (Map.Entry<String, BigDecimal> entry : context.exactVariables.entrySet()) {
            variables.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue());
        }
        variables.put("pi", PI);
        variables.put("π", PI);
        variables.put("e", E);
        variables.put("tau", PI.multiply(BigDecimal.valueOf(2), MC));
        variables.put("phi", new BigDecimal("1.6180339887498948482045868343656381177203091798057628621354486227052604628189"));
    }

    static BigDecimal evaluate(String expression, CalcEngine.Context context) throws Unsupported {
        try {
            HighPrecision parser = new HighPrecision(expression, context);
            BigDecimal value = parser.expression();
            if (parser.peek().type != Type.EOF) throw new Unsupported();
            return value;
        } catch (ArithmeticException error) {
            throw new Unsupported();
        }
    }

    private static List<Token> tokenize(String input, int base) throws Unsupported {
        String source = CalcEngine.normalize(input)
                .replace('×', '*')
                .replace('÷', '/')
                .replace('−', '-')
                .replace("²", "^2")
                .replace("³", "^3");
        List<Token> tokens = new ArrayList<>();
        int index = 0;
        while (index < source.length()) {
            char c = source.charAt(index);
            if (Character.isWhitespace(c)) {
                index++;
                continue;
            }
            if (base != 10 && (digitValue(c) >= 0 || c == '.')) {
                int start = index;
                boolean dot = false;
                while (index < source.length()) {
                    char symbol = source.charAt(index);
                    if (symbol == '.') {
                        if (dot) throw new Unsupported();
                        dot = true;
                        index++;
                        continue;
                    }
                    int digit = digitValue(symbol);
                    if (digit < 0 || digit >= base) break;
                    index++;
                }
                String raw = source.substring(start, index);
                tokens.add(new Token(Type.NUMBER, raw, parseRadix(raw, base)));
                continue;
            }
            if (Character.isDigit(c) || c == '.') {
                int start = index;
                boolean dot = false;
                while (index < source.length() && (Character.isDigit(source.charAt(index)) || source.charAt(index) == '.')) {
                    if (source.charAt(index) == '.') {
                        if (dot) throw new Unsupported();
                        dot = true;
                    }
                    index++;
                }
                if (index < source.length() && (source.charAt(index) == 'e' || source.charAt(index) == 'E')) {
                    int exponentStart = index++;
                    if (index < source.length() && (source.charAt(index) == '+' || source.charAt(index) == '-')) index++;
                    int digits = index;
                    while (index < source.length() && Character.isDigit(source.charAt(index))) index++;
                    if (digits == index) index = exponentStart;
                }
                String raw = source.substring(start, index);
                try {
                    tokens.add(new Token(Type.NUMBER, raw, new BigDecimal(raw, MC)));
                } catch (NumberFormatException error) {
                    throw new Unsupported();
                }
                continue;
            }
            if (Character.isLetter(c) || c == '_' || c == 'π' || c == 'φ') {
                int start = index++;
                while (index < source.length()) {
                    char symbol = source.charAt(index);
                    if (Character.isLetterOrDigit(symbol) || symbol == '_' || symbol == 'π' || symbol == 'φ') index++;
                    else break;
                }
                tokens.add(new Token(Type.NAME, source.substring(start, index), null));
                continue;
            }
            Type type;
            switch (c) {
                case '+': type = Type.PLUS; break;
                case '-': type = Type.MINUS; break;
                case '*': type = Type.STAR; break;
                case '/': type = Type.SLASH; break;
                case '^': type = Type.POWER; break;
                case '(': type = Type.LPAREN; break;
                case ')': type = Type.RPAREN; break;
                case ',': type = Type.COMMA; break;
                case '!': type = Type.BANG; break;
                case '%': type = Type.PERCENT; break;
                default: throw new Unsupported();
            }
            tokens.add(new Token(type, String.valueOf(c), null));
            index++;
        }
        tokens.add(new Token(Type.EOF, "", null));
        return tokens;
    }

    private static BigDecimal parseRadix(String raw, int base) throws Unsupported {
        try {
            String[] parts = raw.split("\\.", -1);
            BigDecimal value = new BigDecimal(new BigInteger(parts[0].isEmpty() ? "0" : parts[0], base));
            if (parts.length == 2) {
                BigDecimal denominator = BigDecimal.ONE;
                BigDecimal fraction = BigDecimal.ZERO;
                for (char c : parts[1].toCharArray()) {
                    denominator = denominator.multiply(BigDecimal.valueOf(base));
                    fraction = fraction.add(BigDecimal.valueOf(digitValue(c)).divide(denominator, MC));
                }
                value = value.add(fraction, MC);
            }
            return value;
        } catch (NumberFormatException error) {
            throw new Unsupported();
        }
    }

    private static int digitValue(char value) {
        if (value >= '0' && value <= '9') return value - '0';
        if (value >= 'a' && value <= 'f') return value - 'a' + 10;
        if (value >= 'A' && value <= 'F') return value - 'A' + 10;
        return -1;
    }

    private BigDecimal expression() throws Unsupported {
        BigDecimal value = term();
        while (peek().type == Type.PLUS || peek().type == Type.MINUS) {
            Type operator = next().type;
            BigDecimal right = term();
            value = operator == Type.PLUS ? value.add(right, MC) : value.subtract(right, MC);
        }
        return value;
    }

    private BigDecimal term() throws Unsupported {
        BigDecimal value = unary();
        while (true) {
            Token token = peek();
            if (token.type == Type.STAR || token.type == Type.SLASH) {
                next();
                BigDecimal right = unary();
                if (token.type == Type.STAR) value = value.multiply(right, MC);
                else {
                    if (right.signum() == 0) throw new Unsupported();
                    value = value.divide(right, MC);
                }
            } else if (token.type == Type.NUMBER || token.type == Type.NAME || token.type == Type.LPAREN) {
                value = value.multiply(unary(), MC);
            } else {
                break;
            }
        }
        return value;
    }

    private BigDecimal unary() throws Unsupported {
        if (consume(Type.PLUS)) return unary();
        if (consume(Type.MINUS)) return unary().negate();
        return power();
    }

    private BigDecimal power() throws Unsupported {
        BigDecimal base = postfix();
        if (consume(Type.POWER)) {
            BigDecimal exponent = unary();
            try {
                return BigDecimalMath.power(base, exponent);
            } catch (ArithmeticException error) {
                throw new Unsupported();
            }
        }
        return base;
    }

    private BigDecimal postfix() throws Unsupported {
        BigDecimal value = primary();
        while (true) {
            if (consume(Type.BANG)) {
                int n;
                try {
                    n = value.intValueExact();
                } catch (ArithmeticException error) {
                    throw new Unsupported();
                }
                if (n < 0 || n > 5000) throw new Unsupported();
                BigInteger factorial = BigInteger.ONE;
                for (int i = 2; i <= n; i++) factorial = factorial.multiply(BigInteger.valueOf(i));
                value = new BigDecimal(factorial, MC);
            } else if (consume(Type.PERCENT)) {
                value = value.divide(BigDecimal.valueOf(100), MC);
            } else {
                break;
            }
        }
        return value;
    }

    private BigDecimal primary() throws Unsupported {
        Token token = peek();
        if (token.type == Type.NUMBER) {
            next();
            return token.number;
        }
        if (token.type == Type.LPAREN) {
            next();
            BigDecimal value = expression();
            expect(Type.RPAREN);
            return value;
        }
        if (token.type == Type.NAME) {
            next();
            String name = token.text.toLowerCase(Locale.ROOT);
            if (consume(Type.LPAREN)) {
                List<BigDecimal> args = new ArrayList<>();
                if (peek().type != Type.RPAREN) {
                    do {
                        args.add(expression());
                    } while (consume(Type.COMMA));
                }
                expect(Type.RPAREN);
                return function(name, args);
            }
            BigDecimal value = variables.get(name);
            if (value == null) value = variables.get(token.text);
            if (value == null) throw new Unsupported();
            return value;
        }
        throw new Unsupported();
    }

    private BigDecimal function(String name, List<BigDecimal> args) throws Unsupported {
        try {
            switch (name) {
                case "abs": return arg(args, 0).abs();
                case "sqrt": return BigDecimalMath.sqrt(arg(args, 0));
                case "cbrt": return BigDecimalMath.cbrt(arg(args, 0));
                case "root": {
                    BigDecimal degree = args.size() > 1 ? args.get(1) : BigDecimal.valueOf(2);
                    BigDecimal value = arg(args, 0);
                    if (value.signum() < 0 && degree.remainder(BigDecimal.valueOf(2)).abs().compareTo(BigDecimal.ONE) == 0) {
                        return BigDecimalMath.cbrt(value);
                    }
                    return BigDecimalMath.power(value, BigDecimal.ONE.divide(degree, MC));
                }
                case "exp": return BigDecimalMath.exp(arg(args, 0));
                case "ln": return BigDecimalMath.ln(arg(args, 0));
                case "log":
                case "log10": {
                    BigDecimal value = BigDecimalMath.ln(arg(args, 0));
                    BigDecimal base = args.size() > 1 ? BigDecimalMath.ln(args.get(1)) : BigDecimalMath.ln(BigDecimal.TEN);
                    return value.divide(base, MC);
                }
                case "log2": return BigDecimalMath.ln(arg(args, 0)).divide(BigDecimalMath.ln(BigDecimal.valueOf(2)), MC);
                case "pow": return BigDecimalMath.power(arg(args, 0), arg(args, 1));
                case "sin": return BigDecimalMath.sin(toRadians(arg(args, 0)));
                case "cos": return BigDecimalMath.cos(toRadians(arg(args, 0)));
                case "tan": return BigDecimalMath.tan(toRadians(arg(args, 0)));
                case "cot": return BigDecimal.ONE.divide(BigDecimalMath.tan(toRadians(arg(args, 0))), MC);
                case "sec": return BigDecimal.ONE.divide(BigDecimalMath.cos(toRadians(arg(args, 0))), MC);
                case "csc": return BigDecimal.ONE.divide(BigDecimalMath.sin(toRadians(arg(args, 0))), MC);
                case "asin": return fromRadians(BigDecimalMath.asin(arg(args, 0)));
                case "acos": return fromRadians(BigDecimalMath.acos(arg(args, 0)));
                case "atan": return fromRadians(BigDecimalMath.atan(arg(args, 0)));
                case "acot": return fromRadians(BigDecimalMath.atan(BigDecimal.ONE.divide(arg(args, 0), MC)));
                case "asec": return fromRadians(BigDecimalMath.acos(BigDecimal.ONE.divide(arg(args, 0), MC)));
                case "acsc": return fromRadians(BigDecimalMath.asin(BigDecimal.ONE.divide(arg(args, 0), MC)));
                case "atan2": {
                    BigDecimal y = arg(args, 0);
                    BigDecimal x = arg(args, 1);
                    BigDecimal value = BigDecimalMath.atan(y.divide(x, MC));
                    if (x.signum() < 0) value = value.add(y.signum() >= 0 ? BigDecimalMath.PI : BigDecimalMath.PI.negate(), MC);
                    return fromRadians(value);
                }
                case "sinh": return BigDecimalMath.hyperbolicSin(arg(args, 0));
                case "cosh": return BigDecimalMath.hyperbolicCos(arg(args, 0));
                case "tanh": return BigDecimalMath.hyperbolicSin(arg(args, 0)).divide(BigDecimalMath.hyperbolicCos(arg(args, 0)), MC);
                case "coth": return BigDecimalMath.hyperbolicCos(arg(args, 0)).divide(BigDecimalMath.hyperbolicSin(arg(args, 0)), MC);
                case "sech": return BigDecimal.ONE.divide(BigDecimalMath.hyperbolicCos(arg(args, 0)), MC);
                case "csch": return BigDecimal.ONE.divide(BigDecimalMath.hyperbolicSin(arg(args, 0)), MC);
                case "asinh": {
                    BigDecimal x = arg(args, 0);
                    return BigDecimalMath.ln(x.add(BigDecimalMath.sqrt(x.multiply(x, MC).add(BigDecimal.ONE, MC)), MC));
                }
                case "acosh": {
                    BigDecimal x = arg(args, 0);
                    return BigDecimalMath.ln(x.add(BigDecimalMath.sqrt(x.multiply(x, MC).subtract(BigDecimal.ONE, MC)), MC));
                }
                case "atanh": {
                    BigDecimal x = arg(args, 0);
                    return BigDecimalMath.ln(BigDecimal.ONE.add(x, MC).divide(BigDecimal.ONE.subtract(x, MC), MC)).divide(BigDecimal.valueOf(2), MC);
                }
                case "acoth": {
                    BigDecimal x = arg(args, 0);
                    return BigDecimalMath.ln(x.add(BigDecimal.ONE, MC).divide(x.subtract(BigDecimal.ONE, MC), MC)).divide(BigDecimal.valueOf(2), MC);
                }
                case "asech": {
                    BigDecimal x = BigDecimal.ONE.divide(arg(args, 0), MC);
                    return BigDecimalMath.ln(x.add(BigDecimalMath.sqrt(x.multiply(x, MC).subtract(BigDecimal.ONE, MC)), MC));
                }
                case "acsch": {
                    BigDecimal x = BigDecimal.ONE.divide(arg(args, 0), MC);
                    return BigDecimalMath.ln(x.add(BigDecimalMath.sqrt(x.multiply(x, MC).add(BigDecimal.ONE, MC)), MC));
                }
                case "reciproc": return BigDecimal.ONE.divide(arg(args, 0), MC);
                case "parallel": {
                    BigDecimal a = arg(args, 0);
                    BigDecimal b = arg(args, 1);
                    return a.multiply(b, MC).divide(a.add(b, MC), MC);
                }
                case "floor": return arg(args, 0).setScale(0, RoundingMode.FLOOR);
                case "ceil": return arg(args, 0).setScale(0, RoundingMode.CEILING);
                case "round": {
                    int scale = args.size() > 1 ? integer(args.get(1)) : 0;
                    return arg(args, 0).setScale(scale, RoundingMode.HALF_UP);
                }
                case "roundup": {
                    BigDecimal value = arg(args, 0);
                    return value.signum() >= 0 ? value.setScale(0, RoundingMode.CEILING)
                            : value.setScale(0, RoundingMode.FLOOR);
                }
                case "trunc": return new BigDecimal(arg(args, 0).toBigInteger());
                case "frac": {
                    BigDecimal value = arg(args, 0);
                    return value.subtract(new BigDecimal(value.toBigInteger()), MC);
                }
                case "sign": return BigDecimal.valueOf(arg(args, 0).signum());
                case "fact":
                case "factorial": return exactFactorial(integer(arg(args, 0)));
                case "doublefactorial": {
                    int n = integer(arg(args, 0));
                    BigDecimal result = BigDecimal.ONE;
                    for (int i = n; i > 1; i -= 2) result = result.multiply(BigDecimal.valueOf(i), MC);
                    return result;
                }
                case "gcd": return new BigDecimal(BigInteger.valueOf(integer(arg(args, 0))).gcd(BigInteger.valueOf(integer(arg(args, 1)))));
                case "lcm": {
                    long a = integer(arg(args, 0));
                    long b = integer(arg(args, 1));
                    return a == 0 || b == 0 ? BigDecimal.ZERO : new BigDecimal(
                            BigInteger.valueOf(a).divide(BigInteger.valueOf(a).gcd(BigInteger.valueOf(b))).multiply(BigInteger.valueOf(b)).abs());
                }
                case "ncr":
                case "combination": return exactCombination(integer(arg(args, 0)), integer(arg(args, 1)));
                case "npr":
                case "permutation":
                case "partialpermutation": return exactPermutation(integer(arg(args, 0)), integer(arg(args, 1)));
                case "min": {
                    BigDecimal result = arg(args, 0);
                    for (BigDecimal value : args) if (value.compareTo(result) < 0) result = value;
                    return result;
                }
                case "max": {
                    BigDecimal result = arg(args, 0);
                    for (BigDecimal value : args) if (value.compareTo(result) > 0) result = value;
                    return result;
                }
                case "deg":
                case "to_deg": return arg(args, 0).multiply(BigDecimal.valueOf(180)).divide(BigDecimalMath.PI, MC);
                case "rad":
                case "to_rad": return arg(args, 0).multiply(BigDecimalMath.PI).divide(BigDecimal.valueOf(180), MC);
                case "grad":
                case "to_grad": return arg(args, 0).multiply(BigDecimal.valueOf(200)).divide(BigDecimalMath.PI, MC);
                case "dms": {
                    return arg(args, 0)
                            .add(arg(args, 1).divide(BigDecimal.valueOf(60), MC), MC)
                            .add(arg(args, 2).divide(BigDecimal.valueOf(3600), MC), MC);
                }
                case "isprime": return isPrime(integer(arg(args, 0))) ? BigDecimal.ONE : BigDecimal.ZERO;
                case "digitcount": return BigDecimal.valueOf(arg(args, 0).toBigInteger().abs().toString().length());
                default: throw new Unsupported();
            }
        } catch (ArithmeticException | IndexOutOfBoundsException error) {
            throw new Unsupported();
        }
    }

    private BigDecimal arg(List<BigDecimal> args, int index) throws Unsupported {
        if (index >= args.size()) throw new Unsupported();
        return args.get(index);
    }

    private int integer(BigDecimal value) throws Unsupported {
        try {
            return value.intValueExact();
        } catch (ArithmeticException error) {
            throw new Unsupported();
        }
    }

    private BigDecimal exactFactorial(int n) throws Unsupported {
        if (n < 0 || n > 5000) throw new Unsupported();
        BigInteger value = BigInteger.ONE;
        for (int i = 2; i <= n; i++) value = value.multiply(BigInteger.valueOf(i));
        return new BigDecimal(value, MC);
    }

    private BigDecimal exactCombination(int n, int r) throws Unsupported {
        if (n < 0 || r < 0 || r > n) throw new Unsupported();
        r = Math.min(r, n - r);
        BigInteger value = BigInteger.ONE;
        for (int i = 1; i <= r; i++) {
            value = value.multiply(BigInteger.valueOf(n - r + i)).divide(BigInteger.valueOf(i));
        }
        return new BigDecimal(value, MC);
    }

    private BigDecimal exactPermutation(int n, int r) throws Unsupported {
        if (n < 0 || r < 0 || r > n) throw new Unsupported();
        BigInteger value = BigInteger.ONE;
        for (int i = 0; i < r; i++) value = value.multiply(BigInteger.valueOf(n - i));
        return new BigDecimal(value, MC);
    }

    private boolean isPrime(long value) {
        if (value < 2) return false;
        if (value % 2 == 0) return value == 2;
        for (long divisor = 3; divisor * divisor <= value; divisor += 2) {
            if (value % divisor == 0) return false;
        }
        return true;
    }

    private BigDecimal toRadians(BigDecimal value) {
        if ("deg".equals(angle)) return value.multiply(BigDecimalMath.PI).divide(BigDecimal.valueOf(180), MC);
        if ("grad".equals(angle)) return value.multiply(BigDecimalMath.PI).divide(BigDecimal.valueOf(200), MC);
        return value;
    }

    private BigDecimal fromRadians(BigDecimal value) {
        if ("deg".equals(angle)) return value.multiply(BigDecimal.valueOf(180)).divide(BigDecimalMath.PI, MC);
        if ("grad".equals(angle)) return value.multiply(BigDecimal.valueOf(200)).divide(BigDecimalMath.PI, MC);
        return value;
    }

    private Token peek() {
        return tokens.get(position);
    }

    private Token next() {
        return tokens.get(position++);
    }

    private boolean consume(Type type) {
        if (peek().type == type) {
            position++;
            return true;
        }
        return false;
    }

    private void expect(Type type) throws Unsupported {
        if (!consume(type)) throw new Unsupported();
    }
}
