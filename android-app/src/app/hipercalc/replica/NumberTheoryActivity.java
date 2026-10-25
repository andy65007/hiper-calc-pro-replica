package app.hipercalc.replica;

import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.Arrays;

public final class NumberTheoryActivity extends BaseActivity {
    private EditText valueA;
    private EditText valueB;
    private Spinner operation;
    private TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("数论与组合", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 8), Ui.dp(this, 16), Ui.dp(this, 20));
        body.addView(Ui.text(this, "输入任意长度的非负整数。nCr/nPr 中 A=n、B=r。", 11, theme.muted, false),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 42)));
        valueA = input("A / n");
        valueA.setText("84");
        valueB = input("B / r");
        valueB.setText("30");
        operation = new Spinner(this);
        operation.setAdapter(Ui.adapter(this, Arrays.asList(
                "质因数分解 A", "A 是否为质数", "GCD(A,B)", "LCM(A,B)",
                "nCr", "nPr", "A!", "A!!", "数字和", "数字位数"
        ), theme.text));
        operation.setBackground(AppTheme.background(this, theme.surface, 11));
        body.addView(valueA, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 48)));
        body.addView(valueB, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 48)));
        body.addView(operation, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 48)));
        Button calculate = Ui.button(this, "计算", theme.accentInk, theme.accent, 11, 12, true);
        calculate.setOnClickListener(v -> calculate());
        LinearLayout.LayoutParams calcParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
        calcParams.topMargin = Ui.dp(this, 12);
        body.addView(calculate, calcParams);
        output = Ui.text(this, "结果将在这里显示。", 17, theme.accentStrong, false);
        output.setTextIsSelectable(true);
        output.setBackground(AppTheme.background(this, theme.surface, 14));
        Ui.padding(output, 14, 14, 14, 14);
        LinearLayout.LayoutParams outputParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        outputParams.topMargin = Ui.dp(this, 12);
        body.addView(output, outputParams);
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(theme.muted);
        input.setTextColor(theme.text);
        input.setTextSize(16);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setSingleLine(true);
        input.setBackground(AppTheme.background(this, theme.surface, 10));
        Ui.padding(input, 12, 9, 12, 9);
        return input;
    }

    private void calculate() {
        try {
            BigInteger a = new BigInteger(valueA.getText().toString());
            BigInteger b = new BigInteger(valueB.getText().toString());
            int selected = operation.getSelectedItemPosition();
            String result;
            if (selected == 0) result = factorize(a);
            else if (selected == 1) result = isPrime(a) ? "是质数" : "不是质数";
            else if (selected == 2) result = a.gcd(b).toString();
            else if (selected == 3) result = a.signum() == 0 || b.signum() == 0
                    ? "0" : a.divide(a.gcd(b)).multiply(b).abs().toString();
            else if (selected == 4) result = combination(a.intValueExact(), b.intValueExact()).toString();
            else if (selected == 5) result = permutation(a.intValueExact(), b.intValueExact()).toString();
            else if (selected == 6) result = factorial(a.intValueExact()).toString();
            else if (selected == 7) result = doubleFactorial(a.intValueExact()).toString();
            else if (selected == 8) result = sumDigits(a).toString();
            else result = a.abs().toString().length() + " 位";
            output.setText(result);
        } catch (RuntimeException error) {
            output.setText("无法计算：请输入有效非负整数");
        }
    }

    private String factorize(BigInteger value) {
        if (value.signum() <= 0) return "质因数分解要求正整数";
        BigInteger number = value;
        StringBuilder result = new StringBuilder();
        int exponent = 0;
        BigInteger two = BigInteger.valueOf(2);
        while (number.mod(two).signum() == 0) {
            number = number.divide(two);
            exponent++;
        }
        if (exponent > 0) result.append("2^").append(exponent);
        for (BigInteger factor = BigInteger.valueOf(3); factor.multiply(factor).compareTo(number) <= 0; factor = factor.add(two)) {
            exponent = 0;
            while (number.mod(factor).signum() == 0) {
                number = number.divide(factor);
                exponent++;
            }
            if (exponent > 0) {
                if (result.length() > 0) result.append(" × ");
                result.append(factor).append(exponent > 1 ? "^" + exponent : "");
            }
        }
        if (number.compareTo(BigInteger.ONE) > 0) {
            if (result.length() > 0) result.append(" × ");
            result.append(number);
        }
        return result.toString();
    }

    private boolean isPrime(BigInteger value) {
        if (value.compareTo(BigInteger.valueOf(2)) < 0) return false;
        return value.isProbablePrime(50);
    }

    private BigInteger factorial(int n) {
        if (n < 0 || n > 10000) throw new ArithmeticException("范围错误");
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) result = result.multiply(BigInteger.valueOf(i));
        return result;
    }

    private BigInteger doubleFactorial(int n) {
        if (n < -1 || n > 10000) throw new ArithmeticException("范围错误");
        BigInteger result = BigInteger.ONE;
        for (int i = n; i > 1; i -= 2) result = result.multiply(BigInteger.valueOf(i));
        return result;
    }

    private BigInteger combination(int n, int r) {
        if (n < 0 || r < 0 || r > n) throw new ArithmeticException("范围错误");
        r = Math.min(r, n - r);
        BigInteger result = BigInteger.ONE;
        for (int i = 1; i <= r; i++) result = result.multiply(BigInteger.valueOf(n - r + i)).divide(BigInteger.valueOf(i));
        return result;
    }

    private BigInteger permutation(int n, int r) {
        if (n < 0 || r < 0 || r > n) throw new ArithmeticException("范围错误");
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < r; i++) result = result.multiply(BigInteger.valueOf(n - i));
        return result;
    }

    private BigInteger sumDigits(BigInteger value) {
        BigInteger result = BigInteger.ZERO;
        for (char c : value.abs().toString().toCharArray()) result = result.add(BigInteger.valueOf(c - '0'));
        return result;
    }
}
