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
 * Self-contained expression engine for the replica.
 * No original APK bytecode or PairIP component is used.
 */
public final class CalcEngine {
    public static final class CalcException extends Exception {
        public final int position;

        public CalcException(String message) {
            this(message, -1);
        }

        public CalcException(String message, int position) {
            super(message);
            this.position = position;
        }
    }

    public static final class Context {
        public final String angle;
        public final int base;
        public final Map<String, Double> variables;
        public final Map<String, BigDecimal> exactVariables;
        public final double ans;

        public Context(String angle, int base, Map<String, Double> variables, double ans) {
            this(angle, base, variables, ans, null);
        }

        public Context(String angle, int base, Map<String, Double> variables, double ans,
                       Map<String, BigDecimal> exactVariables) {
            this.angle = "rad".equals(angle) || "grad".equals(angle) ? angle : "deg";
            this.base = base == 2 || base == 8 || base == 16 ? base : 10;
            this.variables = variables == null ? new HashMap<>() : new HashMap<>(variables);
            this.exactVariables = exactVariables == null ? new HashMap<>() : new HashMap<>(exactVariables);
            this.ans = ans;
        }

        public static Context defaults() {
            return new Context("deg", 10, null, 0);
        }
    }

    public static final class Result {
        public final double value;
        public final String expression;
        public final BigDecimal exact;

        Result(double value, String expression) {
            this.value = value;
            this.expression = expression;
            this.exact = null;
        }

        Result(BigDecimal exact, String expression) {
            this.value = exact.doubleValue();
            this.expression = expression;
            this.exact = exact;
        }
    }

    public static final class Fraction {
        public final long numerator;
        public final long denominator;
        public final double value;

        Fraction(long numerator, long denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
            this.value = denominator == 0 ? Double.NaN : (double) numerator / denominator;
        }
    }

    private enum Type {
        NUMBER, STRING, NAME, PLUS, MINUS, STAR, SLASH, POWER, LPAREN, RPAREN,
        COMMA, BANG, PERCENT, COMPARISON, EOF
    }

    private static final class Token {
        final Type type;
        final Object value;
        final int start;

        Token(Type type, Object value, int start) {
            this.type = type;
            this.value = value;
            this.start = start;
        }
    }

    private CalcEngine() {
    }

    public static String normalize(String input) {
        if (input == null) return "";
        return input.trim()
                .replace('−', '-')
                .replace('–', '-')
                .replace('—', '-')
                .replace('×', '*')
                .replace('·', '*')
                .replace('⋅', '*')
                .replace('÷', '/')
                .replace('√', '√')
                .replace('²', '²')
                .replace('³', '³');
    }

    public static Result evaluate(String expression, Context context) throws CalcException {
        String source = normalize(expression)
                .replace("√", "sqrt")
                .replace("∛", "cbrt")
                .replace("²", "^2")
                .replace("³", "^3");
        if (source.isEmpty()) throw new CalcException("请输入表达式");
        try {
            return new Result(HighPrecision.evaluate(source, context), source);
        } catch (HighPrecision.Unsupported ignored) {
            // Transcendental functions and function arguments use the double engine.
        }
        Parser parser = new Parser(tokenize(source, context.base), context);
        Object value = parser.parse();
        double number = numeric(value, "表达式");
        if (Double.isNaN(number)) throw new CalcException("结果不是有效实数");
        return new Result(number, source);
    }

    private static List<Token> tokenize(String text, int base) throws CalcException {
        List<Token> tokens = new ArrayList<>();
        int index = 0;
        while (index < text.length()) {
            char c = text.charAt(index);
            if (Character.isWhitespace(c)) {
                index++;
                continue;
            }
            if (base != 10 && (digitValue(c) >= 0 || c == '.')) {
                int start = index;
                boolean dot = false;
                while (index < text.length()) {
                    char symbol = text.charAt(index);
                    if (symbol == '.') {
                        if (dot) throw new CalcException("数字格式错误", index);
                        dot = true;
                        index++;
                        continue;
                    }
                    int digit = digitValue(symbol);
                    if (digit < 0 || digit >= base) break;
                    index++;
                }
                String raw = text.substring(start, index);
                tokens.add(new Token(Type.NUMBER, parseBase(raw, base, start), start));
                continue;
            }
            if (Character.isDigit(c) || c == '.') {
                int start = index;
                boolean dot = false;
                while (index < text.length() && (Character.isDigit(text.charAt(index)) || text.charAt(index) == '.')) {
                    if (text.charAt(index) == '.') {
                        if (dot) throw new CalcException("数字格式错误", index);
                        dot = true;
                    }
                    index++;
                }
                if (index < text.length() && (text.charAt(index) == 'e' || text.charAt(index) == 'E')) {
                    int exponentStart = index++;
                    if (index < text.length() && (text.charAt(index) == '+' || text.charAt(index) == '-')) index++;
                    int digits = index;
                    while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
                    if (digits == index) index = exponentStart;
                }
                String raw = text.substring(start, index);
                try {
                    tokens.add(new Token(Type.NUMBER, Double.parseDouble(raw), start));
                } catch (NumberFormatException error) {
                    throw new CalcException("数字格式错误", start);
                }
                continue;
            }
            if (Character.isLetter(c) || c == '_' || c == 'π' || c == 'φ' || c == '∞') {
                int start = index++;
                while (index < text.length()) {
                    char symbol = text.charAt(index);
                    if (Character.isLetterOrDigit(symbol) || symbol == '_' || symbol == 'π' || symbol == 'φ' || symbol == '∞') index++;
                    else break;
                }
                tokens.add(new Token(Type.NAME, text.substring(start, index), start));
                continue;
            }
            if (c == '"' || c == '\'') {
                int start = index++;
                StringBuilder builder = new StringBuilder();
                boolean closed = false;
                while (index < text.length()) {
                    char symbol = text.charAt(index++);
                    if (symbol == '\\' && index < text.length()) {
                        builder.append(text.charAt(index++));
                    } else if (symbol == c) {
                        closed = true;
                        break;
                    } else {
                        builder.append(symbol);
                    }
                }
                if (!closed) throw new CalcException("表达式字符串缺少结束引号", start);
                tokens.add(new Token(Type.STRING, builder.toString(), start));
                continue;
            }
            if (c == '<' || c == '>' || c == '=' || c == '!') {
                int start = index;
                if (index + 1 < text.length()) {
                    String two = text.substring(index, index + 2);
                    if ("<=".equals(two) || ">=".equals(two) || "==".equals(two) || "!=".equals(two)) {
                        tokens.add(new Token(Type.COMPARISON, two, start));
                        index += 2;
                        continue;
                    }
                }
                if (c == '!') tokens.add(new Token(Type.BANG, "!", start));
                else tokens.add(new Token(Type.COMPARISON, String.valueOf(c), start));
                index++;
                continue;
            }
            Type type = switch (c) {
                case '+' -> Type.PLUS;
                case '-' -> Type.MINUS;
                case '*' -> Type.STAR;
                case '/' -> Type.SLASH;
                case '^' -> Type.POWER;
                case '(' -> Type.LPAREN;
                case ')' -> Type.RPAREN;
                case ',' -> Type.COMMA;
                case '%' -> Type.PERCENT;
                default -> null;
            };
            if (type == null) throw new CalcException("无法识别字符“" + c + "”", index);
            tokens.add(new Token(type, String.valueOf(c), index++));
        }
        tokens.add(new Token(Type.EOF, "", text.length()));
        return tokens;
    }

    private static final class Parser {
        private final List<Token> tokens;
        private final Context context;
        private int position;

        Parser(List<Token> tokens, Context context) {
            this.tokens = tokens;
            this.context = context;
        }

        Object parse() throws CalcException {
            Object value = comparison();
            if (peek().type != Type.EOF) {
                throw new CalcException("“" + peek().value + "”前缺少运算符", peek().start);
            }
            return value;
        }

        Object comparison() throws CalcException {
            Object left = expression();
            while (peek().type == Type.COMPARISON) {
                String operator = String.valueOf(next().value);
                double a = numeric(left, "比较");
                double b = numeric(expression(), "比较");
                boolean value = switch (operator) {
                    case "<" -> a < b;
                    case ">" -> a > b;
                    case "<=" -> a <= b;
                    case ">=" -> a >= b;
                    case "!=" -> a != b;
                    default -> a == b;
                };
                left = value ? 1.0 : 0.0;
            }
            return left;
        }

        Object expression() throws CalcException {
            double value = numeric(term(), "加法");
            while (peek().type == Type.PLUS || peek().type == Type.MINUS) {
                Type operator = next().type;
                double right = numeric(term(), "加法");
                value = operator == Type.PLUS ? value + right : value - right;
            }
            return value;
        }

        Object term() throws CalcException {
            double value = numeric(unary(), "乘法");
            while (true) {
                Token token = peek();
                if (token.type == Type.STAR || token.type == Type.SLASH) {
                    next();
                    double right = numeric(unary(), "乘法");
                    value = token.type == Type.STAR ? value * right : value / right;
                } else if (token.type == Type.NUMBER || token.type == Type.NAME
                        || token.type == Type.LPAREN || token.type == Type.STRING) {
                    value *= numeric(unary(), "隐式乘法");
                } else {
                    break;
                }
            }
            return value;
        }

        Object unary() throws CalcException {
            if (consume(Type.PLUS)) return unary();
            if (consume(Type.MINUS)) return -numeric(unary(), "负号");
            return power();
        }

        Object power() throws CalcException {
            double base = numeric(postfix(), "幂");
            if (consume(Type.POWER)) return Math.pow(base, numeric(unary(), "幂"));
            return base;
        }

        Object postfix() throws CalcException {
            double value = numeric(primary(), "后缀运算");
            while (true) {
                if (consume(Type.BANG)) {
                    value = factorial(value);
                } else if (consume(Type.PERCENT)) {
                    value /= 100;
                } else {
                    break;
                }
            }
            return value;
        }

        Object primary() throws CalcException {
            Token token = peek();
            if (token.type == Type.NUMBER || token.type == Type.STRING) {
                next();
                return token.value;
            }
            if (token.type == Type.LPAREN) {
                next();
                Object value = expression();
                expect(Type.RPAREN, "缺少右括号");
                return value;
            }
            if (token.type == Type.NAME) {
                next();
                String name = String.valueOf(token.value).toLowerCase(Locale.ROOT);
                if (consume(Type.LPAREN)) {
                    List<Object> args = new ArrayList<>();
                    if (peek().type != Type.RPAREN) {
                        do {
                            args.add(functionArgument());
                        } while (consume(Type.COMMA));
                    }
                    expect(Type.RPAREN, "函数 " + token.value + " 缺少右括号");
                    return function(name, args, token.start);
                }
                switch (name) {
                    case "pi":
                    case "π":
                        return Math.PI;
                    case "e":
                        return Math.E;
                    case "tau":
                        return Math.PI * 2;
                    case "phi":
                    case "φ":
                        return (1 + Math.sqrt(5)) / 2;
                    case "infinity":
                    case "∞":
                        return Double.POSITIVE_INFINITY;
                    case "ans":
                        return context.ans;
                    default:
                        Double variable = context.variables.get(name);
                        if (variable != null) return variable;
                        // Variable names in expressions are case-insensitive.
                        for (Map.Entry<String, Double> entry : context.variables.entrySet()) {
                            if (entry.getKey().equalsIgnoreCase(name)) return entry.getValue();
                        }
                        throw new CalcException("未知变量：" + token.value, token.start);
                }
            }
            throw new CalcException(token.type == Type.EOF ? "表达式不完整" : "此处不能使用“" + token.value + "”", token.start);
        }

        private Object functionArgument() throws CalcException {
            if (peek().type == Type.STRING) return next().value;
            return expression();
        }

        private Object function(String name, List<Object> args, int start) throws CalcException {
            Object[] values = args.toArray();
            switch (name) {
                case "sin": return Math.sin(toRadians(arg(values, 0, "sin")));
                case "cos": return Math.cos(toRadians(arg(values, 0, "cos")));
                case "tan": return Math.tan(toRadians(arg(values, 0, "tan")));
                case "asin": return fromRadians(Math.asin(arg(values, 0, "asin")));
                case "acos": return fromRadians(Math.acos(arg(values, 0, "acos")));
                case "atan": return fromRadians(Math.atan(arg(values, 0, "atan")));
                case "acot": return fromRadians(Math.atan2(1, arg(values, 0, "acot")));
                case "asec": return fromRadians(Math.acos(1 / arg(values, 0, "asec")));
                case "acsc": return fromRadians(Math.asin(1 / arg(values, 0, "acsc")));
                case "atan2": return fromRadians(Math.atan2(arg(values, 0, "atan2"), arg(values, 1, "atan2")));
                case "sinh": return Math.sinh(arg(values, 0, "sinh"));
                case "cosh": return Math.cosh(arg(values, 0, "cosh"));
                case "tanh": return Math.tanh(arg(values, 0, "tanh"));
                case "coth": return 1 / Math.tanh(arg(values, 0, "coth"));
                case "sech": return 1 / Math.cosh(arg(values, 0, "sech"));
                case "csch": return 1 / Math.sinh(arg(values, 0, "csch"));
                case "asinh": return Math.log(arg(values, 0, "asinh") + Math.sqrt(arg(values, 0, "asinh") * arg(values, 0, "asinh") + 1));
                case "acosh": return Math.log(arg(values, 0, "acosh") + Math.sqrt(arg(values, 0, "acosh") * arg(values, 0, "acosh") - 1));
                case "atanh": return 0.5 * Math.log((1 + arg(values, 0, "atanh")) / (1 - arg(values, 0, "atanh")));
                case "acoth": {
                    double x = arg(values, 0, "acoth");
                    return 0.5 * Math.log((x + 1) / (x - 1));
                }
                case "asech": return Math.log(1 / arg(values, 0, "asech") + Math.sqrt(1 / (arg(values, 0, "asech") * arg(values, 0, "asech")) - 1));
                case "acsch": {
                    double x = 1 / arg(values, 0, "acsch");
                    return Math.log(x + Math.sqrt(x * x + 1));
                }
                case "sec": return 1 / Math.cos(toRadians(arg(values, 0, "sec")));
                case "csc": return 1 / Math.sin(toRadians(arg(values, 0, "csc")));
                case "cot": return 1 / Math.tan(toRadians(arg(values, 0, "cot")));
                case "abs": return Math.abs(arg(values, 0, "abs"));
                case "sqrt": return Math.sqrt(arg(values, 0, "sqrt"));
                case "cbrt": return Math.cbrt(arg(values, 0, "cbrt"));
                case "root": {
                    double n = values.length > 1 ? arg(values, 1, "root") : 2;
                    double x = arg(values, 0, "root");
                    return x < 0 && Math.abs(n % 2) == 1 ? -Math.pow(-x, 1 / n) : Math.pow(x, 1 / n);
                }
                case "exp": return Math.exp(arg(values, 0, "exp"));
                case "ln": return Math.log(arg(values, 0, "ln"));
                case "log": return Math.log(arg(values, 0, "log")) / Math.log(values.length > 1 ? arg(values, 1, "log") : 10);
                case "log2": return Math.log(arg(values, 0, "log2")) / Math.log(2);
                case "floor": return Math.floor(arg(values, 0, "floor"));
                case "ceil": return Math.ceil(arg(values, 0, "ceil"));
                case "round": {
                    double digits = values.length > 1 ? arg(values, 1, "round") : 0;
                    double factor = Math.pow(10, digits);
                    return Math.round(arg(values, 0, "round") * factor) / factor;
                }
                case "roundup": {
                    double x = arg(values, 0, "roundup");
                    return x >= 0 ? Math.ceil(x) : Math.floor(x);
                }
                case "trunc": return (double) (long) arg(values, 0, "trunc");
                case "frac": {
                    double x = arg(values, 0, "frac");
                    return x - (long) x;
                }
                case "sign": return Math.signum(arg(values, 0, "sign"));
                case "fact":
                case "factorial": return factorial(arg(values, 0, "factorial"));
                case "doublefactorial": return doubleFactorial(arg(values, 0, "doublefactorial"));
                case "gamma": return gamma(arg(values, 0, "gamma"));
                case "mod": return arg(values, 0, "mod") % arg(values, 1, "mod");
                case "pow": return Math.pow(arg(values, 0, "pow"), arg(values, 1, "pow"));
                case "hypot": return Math.hypot(arg(values, 0, "hypot"), arg(values, 1, "hypot"));
                case "min": return min(values);
                case "max": return max(values);
                case "gcd": return (double) gcd(integer(arg(values, 0, "gcd")), integer(arg(values, 1, "gcd")));
                case "lcm": {
                    long a = integer(arg(values, 0, "lcm"));
                    long b = integer(arg(values, 1, "lcm"));
                    return a == 0 || b == 0 ? 0d : (double) Math.abs(a / gcd(a, b) * b);
                }
                case "ncr": return combinations(arg(values, 0, "nCr"), arg(values, 1, "nCr"));
                case "npr": return permutations(arg(values, 0, "nPr"), arg(values, 1, "nPr"));
                case "combination": return combinations(arg(values, 0, "combination"), arg(values, 1, "combination"));
                case "permutation": return permutations(arg(values, 0, "permutation"), arg(values, 1, "permutation"));
                case "partialpermutation": return permutations(arg(values, 0, "partialpermutation"), arg(values, 1, "partialpermutation"));
                case "parallel": {
                    double a = arg(values, 0, "parallel");
                    double b = arg(values, 1, "parallel");
                    return a * b / (a + b);
                }
                case "reciproc": return 1 / arg(values, 0, "reciproc");
                case "isprime": return isPrime(integer(arg(values, 0, "isprime"))) ? 1 : 0;
                case "digitcount": return Long.toString(Math.abs(integer(arg(values, 0, "digitcount")))).length();
                case "sumdigits": return sumDigits(integer(arg(values, 0, "sumdigits")));
                case "random":
                case "rand": return Math.random();
                case "deg": return Math.toDegrees(arg(values, 0, "deg"));
                case "rad": return Math.toRadians(arg(values, 0, "rad"));
                case "derivative": return derivative(string(values, 0, "derivative"), arg(values, 1, "derivative"));
                case "integral": return integral(string(values, 0, "integral"), arg(values, 1, "integral"), arg(values, 2, "integral"));
                case "solve": return numericRoot(string(values, 0, "solve"), values.length > 1 ? arg(values, 1, "solve") : 0);
                case "sum": return sum(string(values, 0, "sum"), integer(arg(values, 1, "sum")), integer(arg(values, 2, "sum")), false);
                case "product": return sum(string(values, 0, "product"), integer(arg(values, 1, "product")), integer(arg(values, 2, "product")), true);
                default:
                    throw new CalcException("未知函数：" + name, start);
            }
        }

        private double derivative(String expression, double point) throws CalcException {
            double step = Math.max(1e-7, Math.abs(point) * 1e-7);
            return (at(expression, "x", point + step) - at(expression, "x", point - step)) / (2 * step);
        }

        private double integral(String expression, double from, double to) throws CalcException {
            if (from == to) return 0;
            if (from > to) return -integral(expression, to, from);
            int pieces = 800;
            double step = (to - from) / pieces;
            double sum = 0;
            for (int i = 0; i < pieces; i++) {
                double a = from + i * step;
                double b = a + step;
                double middle = (a + b) / 2;
                sum += (b - a) / 6 * (at(expression, "x", a) + 4 * at(expression, "x", middle) + at(expression, "x", b));
            }
            return sum;
        }

        private double numericRoot(String expression, double guess) throws CalcException {
            double x = guess;
            for (int i = 0; i < 50; i++) {
                double value = at(expression, "x", x);
                if (Math.abs(value) < 1e-10) return x;
                double step = Math.max(1e-6, Math.abs(x) * 1e-6);
                double slope = (at(expression, "x", x + step) - at(expression, "x", x - step)) / (2 * step);
                if (!Double.isFinite(slope) || Math.abs(slope) < 1e-14) break;
                double next = x - value / slope;
                if (!Double.isFinite(next)) break;
                if (Math.abs(next - x) < 1e-11) return next;
                x = next;
            }
            throw new CalcException("未能找到方程的实数根");
        }

        private double sum(String expression, long from, long to, boolean product) throws CalcException {
            if (to - from > 100000) throw new CalcException("求和项过多");
            double result = product ? 1 : 0;
            for (long k = from; k <= to; k++) {
                Map<String, Double> vars = new HashMap<>(context.variables);
                vars.put("k", (double) k);
                vars.put("n", (double) k);
                double value = evaluate(expression, new Context(context.angle, context.base, vars, context.ans)).value;
                result = product ? result * value : result + value;
            }
            return result;
        }

        private double at(String expression, String name, double value) throws CalcException {
            Map<String, Double> vars = new HashMap<>(context.variables);
            vars.put(name, value);
            return evaluate(expression, new Context(context.angle, context.base, vars, context.ans)).value;
        }

        private double toRadians(double value) {
            if ("deg".equals(context.angle)) return Math.toRadians(value);
            if ("grad".equals(context.angle)) return value * Math.PI / 200;
            return value;
        }

        private double fromRadians(double value) {
            if ("deg".equals(context.angle)) return Math.toDegrees(value);
            if ("grad".equals(context.angle)) return value * 200 / Math.PI;
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

        private void expect(Type type, String message) throws CalcException {
            if (!consume(type)) throw new CalcException(message, peek().start);
        }
    }

    private static int digitValue(char value) {
        if (value >= '0' && value <= '9') return value - '0';
        if (value >= 'a' && value <= 'f') return value - 'a' + 10;
        if (value >= 'A' && value <= 'F') return value - 'A' + 10;
        return -1;
    }

    private static double parseBase(String raw, int base, int start) throws CalcException {
        try {
            String[] parts = raw.split("\\.", -1);
            double value = parts[0].isEmpty() ? 0 : Long.parseLong(parts[0], base);
            if (parts.length == 2) {
                double place = base;
                for (char c : parts[1].toCharArray()) {
                    value += digitValue(c) / place;
                    place *= base;
                }
            }
            return value;
        } catch (NumberFormatException error) {
            throw new CalcException(base + " 进制数字格式错误", start);
        }
    }

    private static double numeric(Object value, String label) throws CalcException {
        if (value instanceof Number) return ((Number) value).doubleValue();
        throw new CalcException(label + "需要数值", -1);
    }

    private static String string(Object[] values, int index, String label) throws CalcException {
        if (index >= values.length || !(values[index] instanceof String)) throw new CalcException(label + "的第一个参数需写成字符串");
        return (String) values[index];
    }

    private static double arg(Object[] values, int index, String label) throws CalcException {
        if (index >= values.length) throw new CalcException(label + "参数不足");
        return numeric(values[index], label);
    }

    private static long integer(double value) throws CalcException {
        if (!Double.isFinite(value) || Math.abs(value - Math.rint(value)) > 1e-12) throw new CalcException("参数必须是整数");
        return Math.round(value);
    }

    private static double factorial(double value) throws CalcException {
        long n = integer(value);
        if (n < 0) throw new CalcException("负数没有普通阶乘");
        if (n > 170) return Double.POSITIVE_INFINITY;
        double result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }

    private static double doubleFactorial(double value) throws CalcException {
        long n = integer(value);
        if (n < -1) throw new CalcException("双阶乘参数不能小于 −1");
        double result = 1;
        for (long i = n; i > 1; i -= 2) result *= i;
        return result;
    }

    private static boolean isPrime(long value) {
        if (value < 2) return false;
        if (value % 2 == 0) return value == 2;
        for (long divisor = 3; divisor * divisor <= value; divisor += 2) {
            if (value % divisor == 0) return false;
        }
        return true;
    }

    private static double sumDigits(long value) {
        value = Math.abs(value);
        double sum = 0;
        while (value > 0) {
            sum += value % 10;
            value /= 10;
        }
        return sum;
    }

    private static double gamma(double value) {
        double[] coefficients = {
                676.5203681218851, -1259.1392167224028, 771.3234287776531,
                -176.6150291621406, 12.507343278686905, -0.13857109526572012,
                0.000009984369578019572, 0.00000015056327351493116
        };
        if (value < 0.5) return Math.PI / (Math.sin(Math.PI * value) * gamma(1 - value));
        double x = 0.9999999999998099;
        double shifted = value - 1;
        for (int i = 0; i < coefficients.length; i++) x += coefficients[i] / (shifted + i + 1);
        double t = shifted + coefficients.length - 0.5;
        return Math.sqrt(2 * Math.PI) * Math.pow(t, shifted + 0.5) * Math.exp(-t) * x;
    }

    private static long gcd(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            long temp = a % b;
            a = b;
            b = temp;
        }
        return a;
    }

    private static double combinations(double nValue, double rValue) throws CalcException {
        long n = integer(nValue);
        long r = integer(rValue);
        if (n < 0 || r < 0 || r > n) throw new CalcException("组合数要求 0 ≤ r ≤ n");
        r = Math.min(r, n - r);
        double result = 1;
        for (long i = 1; i <= r; i++) result = result * (n - r + i) / i;
        return Math.round(result);
    }

    private static double permutations(double nValue, double rValue) throws CalcException {
        long n = integer(nValue);
        long r = integer(rValue);
        if (n < 0 || r < 0 || r > n) throw new CalcException("排列数要求 0 ≤ r ≤ n");
        double result = 1;
        for (int i = 0; i < r; i++) result *= n - i;
        return result;
    }

    private static double min(Object[] values) throws CalcException {
        if (values.length == 0) throw new CalcException("min 参数不足");
        double result = Double.POSITIVE_INFINITY;
        for (int i = 0; i < values.length; i++) result = Math.min(result, numeric(values[i], "min"));
        return result;
    }

    private static double max(Object[] values) throws CalcException {
        if (values.length == 0) throw new CalcException("max 参数不足");
        double result = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < values.length; i++) result = Math.max(result, numeric(values[i], "max"));
        return result;
    }

    public static Fraction closestFraction(double value) {
        if (!Double.isFinite(value)) return null;
        double sign = value < 0 ? -1 : 1;
        double target = Math.abs(value);
        long h1 = 1, h0 = 0, k1 = 0, k0 = 1;
        double x = target;
        for (int i = 0; i < 48; i++) {
            long a = (long) Math.floor(x);
            if (a != 0 && (Long.MAX_VALUE - h0) / a < h1) break;
            if (a != 0 && (Long.MAX_VALUE - k0) / a < k1) break;
            long h2 = a * h1 + h0;
            long k2 = a * k1 + k0;
            if (k2 > 1_000_000L || k2 <= 0) break;
            h0 = h1;
            h1 = h2;
            k0 = k1;
            k1 = k2;
            if (Math.abs(target - (double) h1 / k1) < 1e-13) break;
            double remainder = x - a;
            if (remainder < 1e-15) break;
            x = 1 / remainder;
        }
        return k1 == 0 ? null : new Fraction(sign < 0 ? -h1 : h1, k1);
    }

    public static String formatNumber(double value, int precision, String notation, boolean grouping, boolean comma) {
        if (Double.isInfinite(value)) return value > 0 ? "∞" : "−∞";
        if (Double.isNaN(value)) return "未定义";
        if (value == 0) return "0";
        precision = Math.max(1, Math.min(15, precision));
        notation = notation == null ? "auto" : notation;
        double absolute = Math.abs(value);
        String text;
        if ("scientific".equals(notation) || ("auto".equals(notation) && (absolute >= 1e12 || absolute < 1e-8))) {
            text = trim(String.format(Locale.US, "%." + (precision - 1) + "e", value));
            String[] pieces = text.split("e");
            text = trim(pieces[0]) + " × 10" + superscript(Integer.parseInt(pieces[1]));
        } else if ("engineering".equals(notation)) {
            int exponent = (int) Math.floor(Math.log10(absolute) / 3) * 3;
            double mantissa = value / Math.pow(10, exponent);
            text = trim(new BigDecimal(mantissa).round(new MathContext(precision, RoundingMode.HALF_UP)).stripTrailingZeros().toPlainString())
                    + " × 10" + superscript(exponent);
        } else if ("fixed".equals(notation)) {
            text = trim(BigDecimal.valueOf(value).setScale(Math.min(10, Math.max(0, precision - 1)), RoundingMode.HALF_UP).toPlainString());
        } else {
            text = trim(new BigDecimal(value).round(new MathContext(precision, RoundingMode.HALF_UP)).stripTrailingZeros().toPlainString());
        }
        if (grouping && !text.contains(" × ")) {
            boolean negative = text.startsWith("-");
            String unsigned = negative ? text.substring(1) : text;
            String[] parts = unsigned.split("\\.", 2);
            StringBuilder grouped = new StringBuilder();
            for (int i = 0; i < parts[0].length(); i++) {
                if (i > 0 && (parts[0].length() - i) % 3 == 0) grouped.append(' ');
                grouped.append(parts[0].charAt(i));
            }
            text = (negative ? "-" : "") + grouped + (parts.length == 2 ? "." + parts[1] : "");
        }
        text = text.replace('-', '−');
        if (comma) text = text.replace('.', ',');
        return text;
    }

    public static String formatDecimal(BigDecimal input, int precision, String notation, boolean grouping, boolean comma) {
        if (input == null) return "—";
        if (input.signum() == 0) return "0";
        precision = Math.max(1, Math.min(100, precision));
        notation = notation == null ? "auto" : notation;
        BigDecimal value = input.round(new MathContext(precision, RoundingMode.HALF_UP));
        BigDecimal absolute = value.abs();
        String text;
        if ("fixed".equals(notation)) {
            text = trim(value.setScale(Math.max(0, Math.min(100, precision - 1)), RoundingMode.HALF_UP).toPlainString());
        } else if ("scientific".equals(notation) || "auto".equals(notation)
                && (absolute.compareTo(new BigDecimal("1E12")) >= 0 || absolute.compareTo(new BigDecimal("1E-8")) < 0)) {
            int exponent = absolute.precision() - absolute.scale() - 1;
            BigDecimal mantissa = value.movePointLeft(exponent).round(new MathContext(precision, RoundingMode.HALF_UP));
            text = trim(mantissa.stripTrailingZeros().toPlainString()) + " × 10" + superscript(exponent);
        } else if ("engineering".equals(notation) || "eng_si".equals(notation)) {
            int exponent = Math.floorDiv(absolute.precision() - absolute.scale() - 1, 3) * 3;
            BigDecimal mantissa = value.movePointRight(-exponent).round(new MathContext(precision, RoundingMode.HALF_UP));
            String compact = trim(mantissa.stripTrailingZeros().toPlainString());
            if ("eng_si".equals(notation) && exponent >= -30 && exponent <= 30) {
                text = compact + " " + siPrefix(exponent);
            } else {
                text = compact + " × 10" + superscript(exponent);
            }
        } else {
            text = trim(value.stripTrailingZeros().toPlainString());
        }
        if (grouping && !text.contains(" × ") && !text.endsWith(" ")) {
            boolean negative = text.startsWith("-");
            String unsigned = negative ? text.substring(1) : text;
            String[] parts = unsigned.split("\\.", 2);
            StringBuilder grouped = new StringBuilder();
            for (int i = 0; i < parts[0].length(); i++) {
                if (i > 0 && (parts[0].length() - i) % 3 == 0) grouped.append(' ');
                grouped.append(parts[0].charAt(i));
            }
            text = (negative ? "-" : "") + grouped + (parts.length == 2 ? "." + parts[1] : "");
        }
        text = text.replace('-', '−');
        if (comma) text = text.replace('.', ',');
        return text;
    }

    public static String display(Result result, int precision, String notation, boolean grouping, boolean comma) {
        if (result == null) return "—";
        if (result.exact != null) return formatDecimal(result.exact, precision, notation, grouping, comma);
        return formatNumber(result.value, precision, notation, grouping, comma);
    }

    public static String displayMode(Result result, String mode, int precision, String notation,
                                     boolean grouping, boolean comma) {
        if (result == null) return "—";
        if ("EXPRESSION".equals(mode)) return result.expression;
        String fraction = exactFraction(result);
        if ("MIXED_NUMBER".equals(mode)) {
            String mixed = mixedNumber(fraction);
            if (mixed != null) return mixed;
        }
        if ("DMS".equals(mode)) return dms(result.value, true);
        if ("DM".equals(mode)) return dms(result.value, false);
        if ("POLAR".equals(mode)) {
            double radius = Math.abs(result.value);
            double angle = result.value < 0 ? 180 : 0;
            return formatNumber(radius, precision, notation, grouping, comma)
                    + " ∠ " + formatNumber(angle, 10, "auto", false, comma) + "°";
        }
        return display(result, precision, notation, grouping, comma);
    }

    public static String exactFraction(Result result) {
        if (result == null) return null;
        if (result.exact == null) {
            Fraction fraction = closestFraction(result.value);
            if (fraction == null || fraction.denominator > 1000000) return null;
            return fraction.numerator + "/" + fraction.denominator;
        }
        BigDecimal value = result.exact.stripTrailingZeros();
        if (value.scale() <= 0) return value.toBigIntegerExact() + "/1";
        BigInteger numerator = value.unscaledValue();
        BigInteger denominator = BigInteger.TEN.pow(value.scale());
        BigInteger gcd = numerator.abs().gcd(denominator);
        return numerator.divide(gcd) + "/" + denominator.divide(gcd);
    }

    public static String mixedNumber(String fraction) {
        if (fraction == null) return null;
        String[] parts = fraction.split("/");
        if (parts.length != 2) return null;
        try {
            BigInteger numerator = new BigInteger(parts[0]);
            BigInteger denominator = new BigInteger(parts[1]);
            if (denominator.abs().compareTo(BigInteger.ONE) <= 0) return numerator.toString();
            BigInteger[] division = numerator.divideAndRemainder(denominator);
            if (division[1].signum() == 0) return division[0].toString();
            String sign = numerator.signum() < 0 ? "−" : "";
            return sign + division[0].abs() + " " + division[1].abs() + "/" + denominator.abs();
        } catch (NumberFormatException | ArithmeticException error) {
            return null;
        }
    }

    public static String repeatingDecimal(Result result) {
        if (result == null || result.exact == null) return null;
        BigDecimal value = result.exact.stripTrailingZeros();
        if (value.scale() <= 0) return null;
        BigInteger numerator = value.unscaledValue();
        BigInteger denominator = BigInteger.TEN.pow(value.scale());
        BigInteger gcd = numerator.abs().gcd(denominator);
        numerator = numerator.divide(gcd);
        denominator = denominator.divide(gcd);
        boolean negative = numerator.signum() < 0;
        numerator = numerator.abs();
        BigInteger[] division = numerator.divideAndRemainder(denominator);
        BigInteger remainder = division[1];
        if (remainder.signum() == 0) return null;
        Map<BigInteger, Integer> seen = new HashMap<>();
        StringBuilder digits = new StringBuilder();
        int cycleStart = -1;
        for (int i = 0; i < 100 && remainder.signum() != 0; i++) {
            Integer previous = seen.put(remainder, i);
            if (previous != null) {
                cycleStart = previous;
                break;
            }
            remainder = remainder.multiply(BigInteger.TEN);
            BigInteger[] chunk = remainder.divideAndRemainder(denominator);
            digits.append(chunk[0]);
            remainder = chunk[1];
        }
        if (cycleStart < 0) return null;
        String prefix = digits.substring(0, cycleStart);
        String cycle = digits.substring(cycleStart);
        return (negative ? "−" : "") + division[0] + "." + prefix + "(" + cycle + ")";
    }

    public static String dms(double degrees, boolean includeSeconds) {
        if (!Double.isFinite(degrees)) return formatNumber(degrees, 12, "auto", false, false);
        String sign = degrees < 0 ? "−" : "";
        double absolute = Math.abs(degrees);
        int wholeDegrees = (int) Math.floor(absolute);
        double minuteValue = (absolute - wholeDegrees) * 60;
        if (!includeSeconds) {
            return sign + wholeDegrees + "° " + formatNumber(minuteValue, 10, "auto", false, false) + "′";
        }
        int wholeMinutes = (int) Math.floor(minuteValue);
        double seconds = (minuteValue - wholeMinutes) * 60;
        return sign + wholeDegrees + "° " + wholeMinutes + "′ "
                + formatNumber(seconds, 10, "auto", false, false) + "″";
    }

    private static String siPrefix(int exponent) {
        switch (exponent) {
            case -30: return "q";
            case -27: return "r";
            case -24: return "y";
            case -21: return "z";
            case -18: return "a";
            case -15: return "f";
            case -12: return "p";
            case -9: return "n";
            case -6: return "µ";
            case -3: return "m";
            case 0: return "";
            case 3: return "k";
            case 6: return "M";
            case 9: return "G";
            case 12: return "T";
            case 15: return "P";
            case 18: return "E";
            case 21: return "Z";
            case 24: return "Y";
            case 27: return "R";
            case 30: return "Q";
            default: return "";
        }
    }

    private static String trim(String value) {
        if (!value.contains(".")) return value;
        value = value.replaceAll("0+$", "");
        return value.endsWith(".") ? value.substring(0, value.length() - 1) : value;
    }

    private static String superscript(int exponent) {
        String source = Integer.toString(exponent);
        String[][] map = {{"-", "⁻"}, {"0", "⁰"}, {"1", "¹"}, {"2", "²"}, {"3", "³"},
                {"4", "⁴"}, {"5", "⁵"}, {"6", "⁶"}, {"7", "⁷"}, {"8", "⁸"}, {"9", "⁹"}};
        for (String[] entry : map) source = source.replace(entry[0], entry[1]);
        return source;
    }

    public static String integerInBase(double value, int base) {
        if (!Double.isFinite(value) || Math.abs(value - Math.rint(value)) > 1e-12) return null;
        return Long.toString(Math.round(value), base).toUpperCase(Locale.ROOT);
    }

    public static String integerInBase(Result result, int base) {
        if (result == null) return null;
        if (result.exact != null) {
            try {
                return result.exact.toBigIntegerExact().toString(base).toUpperCase(Locale.ROOT);
            } catch (ArithmeticException ignored) {
                return null;
            }
        }
        return integerInBase(result.value, base);
    }
}
