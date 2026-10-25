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

public final class StatisticsActivity extends BaseActivity {
    private EditText dataX;
    private EditText dataY;
    private Spinner operation;
    private TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("统计与回归", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 20));
        TextView intro = Ui.text(this, "数据用逗号或空格分隔。描述统计只使用 X；协方差、相关和回归要求 X/Y 数量相同。", 11, theme.muted, false);
        intro.setLineSpacing(0, 1.5f);
        body.addView(intro, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        dataX = input("X 数据，例如 1,2,3,4");
        dataY = input("Y 数据，例如 2,4,5,8");
        body.addView(label("X 数据"));
        body.addView(dataX, inputParams());
        body.addView(label("Y 数据"));
        body.addView(dataY, inputParams());

        operation = new Spinner(this);
        operation.setAdapter(Ui.adapter(this, Arrays.asList(
                "汇总统计", "最小值", "最大值", "中位数", "百分位 90%",
                "算术平均", "几何平均", "调和平均", "二次平均", "众数", "频数",
                "总体方差", "样本方差", "总体标准差", "样本标准差",
                "偏度", "超额峰度", "协方差", "Pearson 相关系数",
                "线性回归", "二次回归", "三次回归", "指数回归", "对数回归"
        ), theme.text));
        operation.setBackground(AppTheme.background(this, theme.surface, 11));
        body.addView(label("运算"));
        body.addView(operation, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 48)));

        Button calculate = Ui.button(this, "计算", theme.accentInk, theme.accent, 11, 12, true);
        calculate.setOnClickListener(v -> calculate());
        LinearLayout.LayoutParams calcParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
        calcParams.topMargin = Ui.dp(this, 12);
        body.addView(calculate, calcParams);

        output = new TextView(this);
        output.setText("结果将在这里显示。");
        output.setTextColor(theme.accentStrong);
        output.setTextSize(14);
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

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(theme.muted);
        input.setTextColor(theme.text);
        input.setTextSize(14);
        input.setTypeface(Typeface.MONOSPACE);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSingleLine(true);
        input.setBackground(AppTheme.background(this, theme.surface, 11));
        Ui.padding(input, 12, 10, 12, 10);
        return input;
    }

    private TextView label(String text) {
        TextView label = Ui.text(this, text, 10, theme.muted, true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 30));
        params.topMargin = Ui.dp(this, 7);
        label.setLayoutParams(params);
        return label;
    }

    private LinearLayout.LayoutParams inputParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 48));
    }

    private void calculate() {
        try {
            double[] x = StatisticsMath.parse(dataX.getText().toString());
            double[] y = dataY.getText().toString().trim().isEmpty() ? null : StatisticsMath.parse(dataY.getText().toString());
            int selected = operation.getSelectedItemPosition();
            if (selected == 0) {
                String result = "N = " + x.length
                        + "\nΣx = " + number(StatisticsMath.sum(x))
                        + "\nΣx² = " + number(sumSquares(x))
                        + "\nx̄ = " + number(StatisticsMath.mean(x))
                        + "\nmin = " + number(StatisticsMath.minimum(x))
                        + "\nQ1 = " + number(StatisticsMath.percentile(x, 25))
                        + "\nmedian = " + number(StatisticsMath.median(x))
                        + "\nQ3 = " + number(StatisticsMath.percentile(x, 75))
                        + "\nmax = " + number(StatisticsMath.maximum(x))
                        + "\nσ = " + number(StatisticsMath.standardDeviationPopulation(x));
                output.setText(result);
                return;
            }
            if (selected == 1) output.setText(number(StatisticsMath.minimum(x)));
            else if (selected == 2) output.setText(number(StatisticsMath.maximum(x)));
            else if (selected == 3) output.setText(number(StatisticsMath.median(x)));
            else if (selected == 4) output.setText(number(StatisticsMath.percentile(x, 90)));
            else if (selected == 5) output.setText(number(StatisticsMath.mean(x)));
            else if (selected == 6) output.setText(number(StatisticsMath.geometricMean(x)));
            else if (selected == 7) output.setText(number(StatisticsMath.harmonicMean(x)));
            else if (selected == 8) output.setText(number(StatisticsMath.quadraticMean(x)));
            else if (selected == 9) output.setText(number(StatisticsMath.mode(x)));
            else if (selected == 10) output.setText(StatisticsMath.frequency(x));
            else if (selected == 11) output.setText(number(StatisticsMath.variancePopulation(x)));
            else if (selected == 12) output.setText(number(StatisticsMath.varianceSample(x)));
            else if (selected == 13) output.setText(number(StatisticsMath.standardDeviationPopulation(x)));
            else if (selected == 14) output.setText(number(StatisticsMath.standardDeviationSample(x)));
            else if (selected == 15) output.setText(number(StatisticsMath.skewness(x)));
            else if (selected == 16) output.setText(number(StatisticsMath.kurtosisExcess(x)));
            else {
                if (y == null) throw new CalcEngine.CalcException("该运算需要 Y 数据");
                if (selected == 17) output.setText(number(StatisticsMath.covariance(x, y)));
                else if (selected == 18) output.setText(number(StatisticsMath.correlation(x, y)));
                else {
                    StatisticsMath.Regression regression;
                    if (selected == 19) regression = StatisticsMath.linear(x, y);
                    else if (selected == 20) regression = StatisticsMath.polynomial(x, y, 2);
                    else if (selected == 21) regression = StatisticsMath.polynomial(x, y, 3);
                    else if (selected == 22) regression = StatisticsMath.exponential(x, y);
                    else regression = StatisticsMath.logarithmic(x, y);
                    output.setText(regression.equation + "\nR² = " + number(rSquared(x, y, regression)));
                }
            }
        } catch (CalcEngine.CalcException | RuntimeException error) {
            output.setText("无法计算：" + (error.getMessage() == null ? "输入格式错误" : error.getMessage()));
        }
    }

    private double sumSquares(double[] values) {
        double sum = 0;
        for (double value : values) sum += value * value;
        return sum;
    }

    private double rSquared(double[] x, double[] y, StatisticsMath.Regression regression) {
        double mean = StatisticsMath.mean(y);
        double total = 0;
        double residual = 0;
        for (int i = 0; i < x.length; i++) {
            double predicted = predict(regression, x[i]);
            total += (y[i] - mean) * (y[i] - mean);
            residual += (y[i] - predicted) * (y[i] - predicted);
        }
        return total == 0 ? 1 : 1 - residual / total;
    }

    private double predict(StatisticsMath.Regression regression, double x) {
        if (regression.equation.contains("e^(")) return regression.coefficients[0] * Math.exp(regression.coefficients[1] * x);
        if (regression.equation.contains("ln(x)")) return regression.coefficients[0] + regression.coefficients[1] * Math.log(x);
        double result = 0;
        for (int i = 0; i < regression.coefficients.length; i++) result += regression.coefficients[i] * Math.pow(x, i);
        return result;
    }

    private String number(double value) {
        return CalcEngine.formatNumber(value, prefs.precision(), prefs.notation(), prefs.grouped(), false);
    }
}
