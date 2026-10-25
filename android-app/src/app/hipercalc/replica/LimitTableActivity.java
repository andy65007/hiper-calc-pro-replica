package app.hipercalc.replica;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class LimitTableActivity extends BaseActivity {
    private Spinner mode;
    private EditText expression;
    private EditText variable;
    private EditText start;
    private EditText end;
    private EditText step;
    private Spinner direction;
    private TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("极限与函数表", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 22));
        TextView intro = Ui.text(this, "极限使用双侧/单侧数值逼近；函数表在指定区间内采样。表达式可使用 x 或自定义变量。", 11, theme.muted, false);
        intro.setLineSpacing(0, 1.5f);
        body.addView(intro, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 52)));

        mode = spinner(Arrays.asList("数值极限", "函数表"));
        expression = input("表达式，例如 sin(x)/x");
        expression.setText("sin(x)/x");
        variable = input("变量");
        variable.setText("x");
        start = input("起点/极限点");
        start.setText("0");
        end = input("终点");
        end.setText("10");
        step = input("步长");
        step.setText("1");
        direction = spinner(Arrays.asList("双侧", "左侧", "右侧"));

        body.addView(label("模式"));
        body.addView(mode, spinnerParams());
        body.addView(label("表达式"));
        body.addView(expression, inputParams());
        body.addView(label("变量"));
        body.addView(variable, inputParams());
        body.addView(label("起点 / 极限点"));
        body.addView(start, inputParams());
        body.addView(label("终点（函数表）"));
        body.addView(end, inputParams());
        body.addView(label("步长（函数表）"));
        body.addView(step, inputParams());
        body.addView(label("极限方向"));
        body.addView(direction, spinnerParams());

        Button calculate = Ui.button(this, "运行", theme.accentInk, theme.accent, 11, 12, true);
        calculate.setOnClickListener(v -> calculate());
        LinearLayout.LayoutParams calcParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
        calcParams.topMargin = Ui.dp(this, 12);
        body.addView(calculate, calcParams);

        output = new TextView(this);
        output.setText("结果将在这里显示。");
        output.setTextColor(theme.accentStrong);
        output.setTextSize(13);
        output.setTypeface(Typeface.MONOSPACE);
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
        input.setTextSize(14);
        input.setTypeface(Typeface.MONOSPACE);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setBackground(AppTheme.background(this, theme.surface, 11));
        Ui.padding(input, 12, 9, 12, 9);
        return input;
    }

    private TextView label(String text) {
        TextView label = Ui.text(this, text, 10, theme.muted, true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 29));
        params.topMargin = Ui.dp(this, 5);
        label.setLayoutParams(params);
        return label;
    }

    private LinearLayout.LayoutParams inputParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
    }

    private LinearLayout.LayoutParams spinnerParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
    }

    private void calculate() {
        try {
            if (mode.getSelectedItemPosition() == 0) calculateLimit();
            else calculateTable();
        } catch (CalcEngine.CalcException | RuntimeException error) {
            output.setText("无法计算：" + (error.getMessage() == null ? "输入格式错误" : error.getMessage()));
        }
    }

    private void calculateLimit() throws CalcEngine.CalcException {
        double point = Double.parseDouble(start.getText().toString());
        StringBuilder builder = new StringBuilder("数值极限逼近\n");
        double left = Double.NaN;
        double right = Double.NaN;
        if (direction.getSelectedItemPosition() != 2) {
            builder.append("\n左侧：");
            for (int i = 1; i <= 8; i++) {
                double h = Math.pow(10, -i);
                double value = at(point - h);
                left = value;
                builder.append('\n').append("x = ").append(CalcEngine.formatNumber(point - h, 12, "auto", false, false))
                        .append("  f(x) = ").append(CalcEngine.formatNumber(value, 12, "auto", false, false));
            }
        }
        if (direction.getSelectedItemPosition() != 1) {
            builder.append("\n\n右侧：");
            for (int i = 1; i <= 8; i++) {
                double h = Math.pow(10, -i);
                double value = at(point + h);
                right = value;
                builder.append('\n').append("x = ").append(CalcEngine.formatNumber(point + h, 12, "auto", false, false))
                        .append("  f(x) = ").append(CalcEngine.formatNumber(value, 12, "auto", false, false));
            }
        }
        double estimate = Double.isNaN(left) ? right : Double.isNaN(right) ? left : (left + right) / 2;
        builder.append("\n\n估计极限 ").append(CalcEngine.formatNumber(estimate, 12, "auto", false, false));
        if (!Double.isNaN(left) && !Double.isNaN(right)) {
            builder.append(Math.abs(left - right) < 1e-5 ? "\n双侧结果一致。" : "\n双侧结果不一致，极限可能不存在。");
        }
        output.setText(builder.toString());
    }

    private void calculateTable() throws CalcEngine.CalcException {
        double from = Double.parseDouble(start.getText().toString());
        double to = Double.parseDouble(end.getText().toString());
        double delta = Double.parseDouble(step.getText().toString());
        if (delta == 0 || (to - from) / delta < 0) throw new CalcEngine.CalcException("步长方向或大小错误");
        if (Math.abs((to - from) / delta) > 500) throw new CalcEngine.CalcException("函数表最多生成 500 行");
        StringBuilder builder = new StringBuilder("x\tf(x)");
        for (double x = from; delta > 0 ? x <= to + delta * 1e-9 : x >= to - delta * 1e-9; x += delta) {
            builder.append('\n')
                    .append(CalcEngine.formatNumber(x, 10, "auto", false, false))
                    .append('\t')
                    .append(CalcEngine.formatNumber(at(x), 10, "auto", false, false));
        }
        output.setText(builder.toString());
    }

    private double at(double value) throws CalcEngine.CalcException {
        String name = variable.getText().toString().trim();
        if (name.isEmpty()) name = "x";
        Map<String, Double> vars = new HashMap<>(prefs.variables());
        vars.put(name, value);
        return CalcEngine.evaluate(expression.getText().toString(), new CalcEngine.Context(
                prefs.angle(), 10, vars, prefs.ans())).value;
    }
}
