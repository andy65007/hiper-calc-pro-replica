package app.hipercalc.replica;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MemoryActivity extends BaseActivity {
    private LinearLayout variableList;
    private EditText nameInput;
    private EditText valueInput;
    private double incomingValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        incomingValue = getIntent().getDoubleExtra("value", prefs.ans());
        LinearLayout root = contentColumn();
        root.addView(header("变量与内存", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 4), Ui.dp(this, 16), Ui.dp(this, 20));

        TextView desc = Ui.text(this, "A–F 与 M 是内置变量。变量可保存数值或表达式结果，并在后续计算中使用。", 11, theme.muted, false);
        desc.setLineSpacing(0, 1.5f);
        body.addView(desc, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54)));

        variableList = Ui.column(this);
        body.addView(variableList);

        LinearLayout editor = Ui.column(this);
        editor.setPadding(Ui.dp(this, 15), Ui.dp(this, 15), Ui.dp(this, 15), Ui.dp(this, 15));
        editor.setBackground(AppTheme.background(this, theme.surface, 16));
        LinearLayout.LayoutParams editorParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editorParams.topMargin = Ui.dp(this, 16);

        editor.addView(Ui.text(this, "新建或更新变量", 14, theme.text, true));
        nameInput = input("变量名，例如 G");
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        valueInput = input("数值或算式，例如 2π+3");
        valueInput.setInputType(InputType.TYPE_CLASS_TEXT);
        editor.addView(nameInput, fieldParams());
        editor.addView(valueInput, fieldParams());
        Button save = Ui.button(this, "保存变量", theme.accentInk, theme.accent, 10, 12, true);
        save.setOnClickListener(v -> saveVariable());
        Button current = Ui.button(this, "把当前结果保存为下一变量", theme.muted, theme.surfaceRaised, 10, 11, false);
        current.setOnClickListener(v -> {
            nameInput.setText(nextName());
            valueInput.setText(format(incomingValue));
        });
        editor.addView(save, new LinearLayout.LayoutParams(Ui.dp(this, 120), Ui.dp(this, 40)));
        editor.addView(current, new LinearLayout.LayoutParams(Ui.dp(this, 220), Ui.dp(this, 40)));
        body.addView(editor, editorParams);

        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        renderVariables();
    }

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(theme.muted);
        input.setTextColor(theme.text);
        input.setTextSize(13);
        input.setSingleLine(true);
        input.setBackground(AppTheme.background(this, theme.background, 10));
        Ui.padding(input, 12, 8, 12, 8);
        return input;
    }

    private LinearLayout.LayoutParams fieldParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44));
        params.topMargin = Ui.dp(this, 9);
        return params;
    }

    private void renderVariables() {
        variableList.removeAllViews();
        Map<String, Double> variables = prefs.variables();
        Set<String> names = new HashSet<>(Arrays.asList("A", "B", "C", "D", "E", "F", "M"));
        names.addAll(variables.keySet());
        for (String name : names) {
            LinearLayout row = Ui.row(this, Gravity.CENTER_VERTICAL);
            row.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
            row.setBackground(AppTheme.background(this, theme.surface, 12));
            TextView key = Ui.text(this, name, 12, theme.signal, true);
            key.setGravity(Gravity.CENTER);
            key.setBackground(AppTheme.background(this, Ui.withAlpha(theme.signal, 22), 8));
            row.addView(key, new LinearLayout.LayoutParams(Ui.dp(this, 34), Ui.dp(this, 34)));
            String value = variables.containsKey(name) ? format(variables.get(name)) : "未赋值";
            TextView valueView = Ui.text(this, value, 12, variables.containsKey(name) ? theme.text : theme.muted, false);
            valueView.setSingleLine(true);
            LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 34), 1);
            valueParams.leftMargin = Ui.dp(this, 10);
            row.addView(valueView, valueParams);
            TextView insert = Ui.text(this, "插入", 10, theme.accent, false);
            insert.setGravity(Gravity.CENTER);
            insert.setOnClickListener(v -> insertIntoCalculator(name));
            row.addView(insert, new LinearLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 34)));
            if (variables.containsKey(name)) {
                TextView delete = Ui.text(this, "×", 14, theme.danger, false);
                delete.setGravity(Gravity.CENTER);
                delete.setOnClickListener(v -> {
                    prefs.removeVariable(name);
                    renderVariables();
                });
                row.addView(delete, new LinearLayout.LayoutParams(Ui.dp(this, 34), Ui.dp(this, 34)));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = Ui.dp(this, 7);
            variableList.addView(row, params);
        }
    }

    private void saveVariable() {
        String name = nameInput.getText().toString().trim();
        String expression = valueInput.getText().toString().trim();
        if (!name.matches("[A-Za-z_][A-Za-z0-9_]{0,11}")) {
            toast("变量名需以字母或下划线开头");
            return;
        }
        if (expression.isEmpty()) {
            prefs.removeVariable(name);
            renderVariables();
            return;
        }
        try {
            CalcEngine.Result result = CalcEngine.evaluate(expression, new CalcEngine.Context(
                    prefs.angle(), 10, prefs.variables(), prefs.ans()));
            String exact = result.exact == null
                    ? java.math.BigDecimal.valueOf(result.value).toPlainString()
                    : result.exact.toPlainString();
            prefs.setVariable(name, result.value, exact);
            nameInput.setText("");
            valueInput.setText("");
            renderVariables();
            toast("变量 " + name + " 已保存");
        } catch (CalcEngine.CalcException error) {
            toast(error.getMessage());
        }
    }

    private void insertIntoCalculator(String name) {
        prefs.setExpression(name);
        Intent intent = new Intent(this, CalculatorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private String nextName() {
        for (String name : new String[]{"A", "B", "C", "D", "E", "F", "M", "G", "H"}) {
            if (!prefs.variables().containsKey(name)) return name;
        }
        return "V";
    }
}
