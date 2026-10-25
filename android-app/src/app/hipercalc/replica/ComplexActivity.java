package app.hipercalc.replica;

import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;

public final class ComplexActivity extends BaseActivity {
    private EditText realA;
    private EditText imagA;
    private EditText realB;
    private EditText imagB;
    private Spinner operation;
    private TextView output;
    private TextView polar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("复数计算", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 20));
        body.addView(Ui.text(this, "输入两个复数 a+bi。极坐标按当前 DEG/RAD/GRAD 设置显示。", 11, theme.muted, false),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 42)));
        body.addView(complexRow("复数 A", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54)));
        body.addView(complexRow("复数 B", false), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54)));

        operation = new Spinner(this);
        operation.setAdapter(Ui.adapter(this, Arrays.asList(
                "A + B", "A − B", "A × B", "A ÷ B", "A^n", "|A|", "arg(A)",
                "conj(A)", "polar(A)", "exp(A)", "ln(A)", "sqrt(A)", "A^B",
                "sin(A)", "cos(A)", "tan(A)", "sinh(A)", "cosh(A)", "tanh(A)"
        ), theme.text));
        operation.setBackground(AppTheme.background(this, theme.surface, 11));
        body.addView(operation, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 48)));

        Button calculate = Ui.button(this, "计算", theme.accentInk, theme.accent, 11, 12, true);
        calculate.setOnClickListener(v -> calculate());
        LinearLayout.LayoutParams calcParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
        calcParams.topMargin = Ui.dp(this, 12);
        body.addView(calculate, calcParams);

        LinearLayout card = Ui.column(this);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        card.setBackground(AppTheme.background(this, theme.surface, 16));
        output = Ui.math(this, "0", 30, theme.accentStrong);
        output.setTextIsSelectable(true);
        polar = Ui.text(this, "", 11, theme.muted, false);
        card.addView(output);
        card.addView(polar);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = Ui.dp(this, 14);
        body.addView(card, cardParams);
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private LinearLayout complexRow(String label, boolean first) {
        LinearLayout row = Ui.row(this, Gravity.CENTER_VERTICAL);
        row.addView(Ui.text(this, label, 11, theme.muted, true),
                new LinearLayout.LayoutParams(Ui.dp(this, 66), Ui.dp(this, 46)));
        EditText real = number(first ? "1" : "0");
        EditText imag = number(first ? "2" : "1");
        if (first) {
            realA = real;
            imagA = imag;
        } else {
            realB = real;
            imagB = imag;
        }
        row.addView(real, new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 1));
        TextView plus = Ui.text(this, "+", 16, theme.muted, false);
        plus.setGravity(Gravity.CENTER);
        row.addView(plus, new LinearLayout.LayoutParams(Ui.dp(this, 25), Ui.dp(this, 44)));
        row.addView(imag, new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 1));
        row.addView(Ui.text(this, "i", 12, theme.muted, false),
                new LinearLayout.LayoutParams(Ui.dp(this, 22), Ui.dp(this, 44)));
        return row;
    }

    private EditText number(String value) {
        EditText input = new EditText(this);
        input.setText(value);
        input.setTextColor(theme.text);
        input.setTextSize(14);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.setBackground(AppTheme.background(this, theme.surfaceRaised, 10));
        Ui.padding(input, 10, 7, 10, 7);
        return input;
    }

    private void calculate() {
        try {
            ComplexMath a = new ComplexMath(value(realA), value(imagA));
            ComplexMath b = new ComplexMath(value(realB), value(imagB));
            int selected = operation.getSelectedItemPosition();
            ComplexMath result = null;
            String scalar = null;
            if (selected == 0) result = a.add(b);
            else if (selected == 1) result = a.subtract(b);
            else if (selected == 2) result = a.multiply(b);
            else if (selected == 3) result = a.divide(b);
            else if (selected == 4) result = a.power((int) Math.round(b.real));
            else if (selected == 5) scalar = format(a.abs());
            else if (selected == 6) scalar = format(angle(a.argument()));
            else if (selected == 7) result = a.conjugate();
            else if (selected == 8) scalar = polar(a);
            else if (selected == 9) result = a.exp();
            else if (selected == 10) result = a.log();
            else if (selected == 11) result = a.sqrt();
            else if (selected == 12) result = a.power(b);
            else if (selected == 13) result = a.sin();
            else if (selected == 14) result = a.cos();
            else if (selected == 15) result = a.tan();
            else if (selected == 16) result = a.sinh();
            else if (selected == 17) result = a.cosh();
            else result = a.tanh();
            if (result != null) {
                output.setText(rectangular(result));
                polar.setText(polar(result));
            } else {
                output.setText(scalar);
                polar.setText("");
            }
        } catch (RuntimeException error) {
            output.setText("输入格式错误");
            polar.setText("");
        }
    }

    private double value(EditText input) {
        return Double.parseDouble(input.getText().toString().replace(',', '.'));
    }

    private String rectangular(ComplexMath value) {
        if (Math.abs(value.imaginary) < 1e-12) return format(value.real);
        if (Math.abs(value.real) < 1e-12) return format(value.imaginary) + "i";
        return format(value.real) + (value.imaginary >= 0 ? " + " : " − ")
                + format(Math.abs(value.imaginary)) + "i";
    }

    private String polar(ComplexMath value) {
        return format(value.abs()) + " ∠ " + format(angle(value.argument())) + " " + prefs.angle().toUpperCase();
    }

    private double angle(double radians) {
        if ("deg".equals(prefs.angle())) return Math.toDegrees(radians);
        if ("grad".equals(prefs.angle())) return radians * 200 / Math.PI;
        return radians;
    }

    protected String format(double value) {
        return CalcEngine.formatNumber(value, prefs.precision(), prefs.notation(), prefs.grouped(), false);
    }
}
