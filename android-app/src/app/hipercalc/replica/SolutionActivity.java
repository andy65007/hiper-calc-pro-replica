package app.hipercalc.replica;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;

public final class SolutionActivity extends BaseActivity {
    private EditText equationInput;
    private Spinner variableSpinner;
    private LinearLayout output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("方程解题", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 20));

        TextView note = Ui.text(this, "支持可整理为 6 次以内多项式的一元方程。一/二次方程显示代数步骤，高次方程使用复平面数值求根。", 11, theme.muted, false);
        note.setLineSpacing(0, 1.5f);
        body.addView(note, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54)));

        equationInput = new EditText(this);
        equationInput.setText("x^2-5x+6=0");
        equationInput.setTextColor(theme.text);
        equationInput.setTextSize(19);
        equationInput.setSingleLine(true);
        equationInput.setInputType(InputType.TYPE_CLASS_TEXT);
        equationInput.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        equationInput.setBackground(AppTheme.background(this, theme.surface, 13));
        Ui.padding(equationInput, 14, 10, 14, 10);
        body.addView(equationInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54)));

        LinearLayout controls = Ui.row(this, Gravity.CENTER_VERTICAL);
        variableSpinner = new Spinner(this);
        variableSpinner.setAdapter(Ui.adapter(this, Arrays.asList("未知数 x", "未知数 y", "未知数 z", "未知数 a"), theme.text));
        variableSpinner.setBackground(AppTheme.background(this, theme.surface, 11));
        controls.addView(variableSpinner, new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1));
        Button solve = Ui.button(this, "开始解题", theme.accentInk, theme.accent, 11, 12, true);
        solve.setOnClickListener(v -> solve());
        LinearLayout.LayoutParams solveParams = new LinearLayout.LayoutParams(Ui.dp(this, 108), Ui.dp(this, 46));
        solveParams.leftMargin = Ui.dp(this, 9);
        controls.addView(solve, solveParams);
        LinearLayout.LayoutParams controlParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58));
        controlParams.topMargin = Ui.dp(this, 8);
        body.addView(controls, controlParams);

        output = Ui.column(this);
        output.setPadding(Ui.dp(this, 17), Ui.dp(this, 17), Ui.dp(this, 17), Ui.dp(this, 17));
        output.setBackground(AppTheme.background(this, theme.surface, 16));
        LinearLayout.LayoutParams outputParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        outputParams.topMargin = Ui.dp(this, 14);
        body.addView(output, outputParams);
        renderHint();
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private void renderHint() {
        output.removeAllViews();
        output.addView(Ui.text(this, "解题器将整理标准形式、计算判别式，并展示每个主要步骤。", 11, theme.muted, false));
    }

    private void solve() {
        output.removeAllViews();
        String variable = new String[]{"x", "y", "z", "a"}[Math.max(0, variableSpinner.getSelectedItemPosition())];
        try {
            EquationSolver.Solution solution = EquationSolver.solve(equationInput.getText().toString(), variable);
            TextView title = Ui.text(this, "解答", 9, theme.muted, true);
            output.addView(title);
            if (solution.roots.isEmpty()) {
                output.addView(Ui.math(this, "恒成立", 30, theme.accentStrong));
            } else {
                for (int i = 0; i < solution.roots.size(); i++) {
                    EquationSolver.Root root = solution.roots.get(i);
                    String suffix = solution.roots.size() > 1 ? (i == 0 ? "₁" : "₂") : "";
                    String value = Math.abs(root.imaginary) < 1e-10
                            ? format(root.real)
                            : format(root.real) + (root.imaginary >= 0 ? " + " : " − ") + format(Math.abs(root.imaginary)) + "i";
                    output.addView(Ui.math(this, variable + suffix + " = " + value, 25, theme.accentStrong));
                }
            }
            TextView stepsTitle = Ui.text(this, "步骤", 9, theme.muted, true);
            LinearLayout.LayoutParams stepsTitleParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 38));
            stepsTitleParams.topMargin = Ui.dp(this, 12);
            output.addView(stepsTitle, stepsTitleParams);
            for (int i = 0; i < solution.steps.size(); i++) {
                TextView step = Ui.text(this, (i + 1) + ". " + solution.steps.get(i), 11, theme.muted, false);
                step.setLineSpacing(0, 1.45f);
                step.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
                output.addView(step);
            }
            if (solution.vertexX != null) {
                TextView vertex = Ui.text(this, "抛物线顶点：(" + format(solution.vertexX) + ", " + format(solution.vertexY) + ")", 10, theme.muted, false);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 35));
                params.topMargin = Ui.dp(this, 8);
                output.addView(vertex, params);
            }
        } catch (CalcEngine.CalcException error) {
            TextView message = Ui.text(this, error.getMessage(), 12, theme.danger, false);
            message.setLineSpacing(0, 1.4f);
            output.addView(message);
        }
    }
}
