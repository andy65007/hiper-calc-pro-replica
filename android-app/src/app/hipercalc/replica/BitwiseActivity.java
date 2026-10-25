package app.hipercalc.replica;

import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.Arrays;

public final class BitwiseActivity extends BaseActivity {
    private Spinner inputBase;
    private Spinner operation;
    private Spinner width;
    private EditText first;
    private EditText second;
    private Switch signed;
    private TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("整数与位运算", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 22));
        TextView intro = Ui.text(this, "支持任意长度整数文本，结果按所选位宽掩码显示。旋转和移位长度可由第二操作数指定。", 11, theme.muted, false);
        intro.setLineSpacing(0, 1.5f);
        body.addView(intro, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 52)));

        inputBase = spinner(Arrays.asList("十进制 DEC", "十六进制 HEX", "八进制 OCT", "二进制 BIN"));
        operation = spinner(Arrays.asList("AND", "OR", "XOR", "NAND", "NOR", "XNOR", "NOT",
                "SHL", "SHR", "UNSIGNED_SHR", "RL", "RR"));
        width = spinner(Arrays.asList("4 位", "8 位", "16 位", "32 位", "64 位", "128 位"));
        width.setSelection(3);
        first = input("第一个整数，例如 FF");
        first.setText("F0");
        second = input("第二个整数或移位数");
        second.setText("0F");
        signed = new Switch(this);
        signed.setText("按二进制补码显示有符号结果");
        signed.setTextColor(theme.text);

        body.addView(label("输入进制"));
        body.addView(inputBase, spinnerParams());
        body.addView(label("操作数 A"));
        body.addView(first, inputParams());
        body.addView(label("操作数 B / 移位数"));
        body.addView(second, inputParams());
        body.addView(label("运算"));
        body.addView(operation, spinnerParams());
        body.addView(label("结果位宽"));
        body.addView(width, spinnerParams());
        body.addView(signed, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44)));

        Button calculate = Ui.button(this, "计算", theme.accentInk, theme.accent, 11, 12, true);
        calculate.setOnClickListener(v -> calculate());
        LinearLayout.LayoutParams calcParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
        calcParams.topMargin = Ui.dp(this, 10);
        body.addView(calculate, calcParams);

        output = new TextView(this);
        output.setText("结果将在这里显示。");
        output.setTextColor(theme.accentStrong);
        output.setTextSize(14);
        output.setTypeface(android.graphics.Typeface.MONOSPACE);
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

    private Spinner spinner(java.util.List<String> values) {
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(Ui.adapter(this, values, theme.text));
        spinner.setBackground(AppTheme.background(this, theme.surface, 11));
        return spinner;
    }

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(theme.muted);
        input.setTextColor(theme.text);
        input.setTextSize(16);
        input.setTypeface(android.graphics.Typeface.MONOSPACE);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setBackground(AppTheme.background(this, theme.surface, 11));
        Ui.padding(input, 12, 10, 12, 10);
        return input;
    }

    private TextView label(String text) {
        TextView label = Ui.text(this, text, 10, theme.muted, true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 30));
        params.topMargin = Ui.dp(this, 6);
        label.setLayoutParams(params);
        return label;
    }

    private LinearLayout.LayoutParams inputParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 48));
    }

    private LinearLayout.LayoutParams spinnerParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 48));
    }

    private void calculate() {
        try {
            int radix = new int[]{10, 16, 8, 2}[inputBase.getSelectedItemPosition()];
            BigInteger a = parse(first.getText().toString(), radix);
            BigInteger b = parse(second.getText().toString(), radix);
            int bits = new int[]{4, 8, 16, 32, 64, 128}[width.getSelectedItemPosition()];
            BigInteger mask = BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE);
            a = a.and(mask);
            b = b.and(mask);
            int selected = operation.getSelectedItemPosition();
            BigInteger result;
            switch (selected) {
                case 0: result = a.and(b); break;
                case 1: result = a.or(b); break;
                case 2: result = a.xor(b); break;
                case 3: result = mask.xor(a.and(b)); break;
                case 4: result = mask.xor(a.or(b)); break;
                case 5: result = mask.xor(a.xor(b)); break;
                case 6: result = mask.xor(a); break;
                case 7: result = a.shiftLeft(b.intValue()).and(mask); break;
                case 8: result = b.intValue() >= bits ? BigInteger.ZERO : shiftRightSigned(a, Math.min(bits, b.intValue()), bits).and(mask); break;
                case 9: result = b.intValue() >= bits ? BigInteger.ZERO : a.shiftRight(Math.min(bits, b.intValue())).and(mask); break;
                case 10: result = rotate(a, b.intValue() % bits, bits, true); break;
                default: result = rotate(a, b.intValue() % bits, bits, false); break;
            }
            if (signed.isChecked()) {
                result = result.testBit(bits - 1) ? result.subtract(BigInteger.ONE.shiftLeft(bits)) : result;
            }
            output.setText("BIN  " + result.toString(2)
                    + "\nOCT  " + result.toString(8)
                    + "\nDEC  " + result.toString(10)
                    + "\nHEX  " + result.toString(16).toUpperCase());
        } catch (RuntimeException error) {
            output.setText("无法计算：输入不是所选进制的有效整数");
        }
    }

    private BigInteger parse(String input, int radix) {
        String value = input.trim().replace(" ", "").replace("_", "");
        boolean negative = value.startsWith("-");
        if (negative) value = value.substring(1);
        if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
            radix = 16;
        } else if (value.startsWith("0b") || value.startsWith("0B")) {
            value = value.substring(2);
            radix = 2;
        }
        BigInteger result = new BigInteger(value.isEmpty() ? "0" : value, radix);
        return negative ? result.negate() : result;
    }

    private BigInteger shiftRightSigned(BigInteger value, int shift, int bits) {
        if (shift == 0) return value;
        BigInteger sign = value.testBit(bits - 1) ? BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE) : BigInteger.ZERO;
        return value.shiftRight(shift).or(sign.shiftLeft(Math.max(0, bits - shift)));
    }

    private BigInteger rotate(BigInteger value, int shift, int bits, boolean left) {
        shift = ((shift % bits) + bits) % bits;
        if (shift == 0) return value;
        if (!left) shift = bits - shift;
        return value.shiftLeft(shift).or(value.shiftRight(bits - shift)).and(BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE));
    }
}
