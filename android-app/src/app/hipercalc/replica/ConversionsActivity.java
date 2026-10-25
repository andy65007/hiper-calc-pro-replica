package app.hipercalc.replica;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public final class ConversionsActivity extends BaseActivity {
    private UnitCatalog.Category category;
    private Spinner categorySpinner;
    private Spinner fromSpinner;
    private Spinner toSpinner;
    private EditText valueInput;
    private TextView output;
    private TextView outputUnit;
    private LinearLayout alternatives;
    private double converted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        category = UnitCatalog.CATEGORIES.get(0);
        LinearLayout root = contentColumn();
        root.addView(header("单位换算", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 4), Ui.dp(this, 16), Ui.dp(this, 20));

        categorySpinner = spinner();
        List<String> categories = new ArrayList<>();
        for (UnitCatalog.Category item : UnitCatalog.CATEGORIES) {
            categories.add(item.icon + "  " + item.label);
        }
        categorySpinner.setAdapter(stringAdapter(categories));
        body.addView(label("换算类别"));
        body.addView(categorySpinner, spinnerParams());

        LinearLayout pickers = Ui.row(this, Gravity.BOTTOM);
        LinearLayout fromColumn = Ui.column(this);
        fromColumn.addView(label("从"));
        fromSpinner = spinner();
        fromColumn.addView(fromSpinner, spinnerParams());
        LinearLayout toColumn = Ui.column(this);
        toColumn.addView(label("到"));
        toSpinner = spinner();
        toColumn.addView(toSpinner, spinnerParams());
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout.LayoutParams halfRight = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        halfRight.leftMargin = Ui.dp(this, 10);
        pickers.addView(fromColumn, half);
        pickers.addView(toColumn, halfRight);
        body.addView(pickers);

        body.addView(label("输入数值"));
        valueInput = new EditText(this);
        valueInput.setTextColor(theme.text);
        valueInput.setTextSize(19);
        valueInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        valueInput.setSingleLine(true);
        valueInput.setBackground(AppTheme.background(this, theme.surface, 13));
        Ui.padding(valueInput, 14, 10, 14, 10);
        double initial = getIntent().getDoubleExtra("value", 1);
        valueInput.setText(CalcEngine.formatNumber(initial, 12, "auto", false, false));
        body.addView(valueInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 50)));

        LinearLayout resultCard = Ui.column(this);
        resultCard.setPadding(Ui.dp(this, 18), Ui.dp(this, 17), Ui.dp(this, 18), Ui.dp(this, 17));
        resultCard.setBackground(AppTheme.background(this, theme.surface, 18));
        LinearLayout.LayoutParams resultParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resultParams.topMargin = Ui.dp(this, 16);
        output = Ui.math(this, "0", 34, theme.accentStrong);
        output.setSingleLine(true);
        outputUnit = Ui.text(this, "", 11, theme.muted, false);
        resultCard.addView(output);
        resultCard.addView(outputUnit);
        body.addView(resultCard, resultParams);

        LinearLayout actions = Ui.row(this, Gravity.CENTER_VERTICAL);
        Button use = Ui.button(this, "带回计算器", theme.accentInk, theme.accent, 11, 12, true);
        use.setOnClickListener(v -> useResult());
        Button swap = Ui.button(this, "交换单位", theme.muted, theme.surfaceRaised, 11, 11, false);
        swap.setOnClickListener(v -> swap());
        actions.addView(use, new LinearLayout.LayoutParams(0, Ui.dp(this, 42), 1));
        LinearLayout.LayoutParams swapParams = new LinearLayout.LayoutParams(Ui.dp(this, 105), Ui.dp(this, 42));
        swapParams.leftMargin = Ui.dp(this, 8);
        actions.addView(swap, swapParams);
        body.addView(actions, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54)));

        body.addView(label("其他常见单位"));
        alternatives = Ui.column(this);
        body.addView(alternatives);

        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        categorySpinner.setOnItemSelectedListener(new SimpleSelect() {
            @Override
            public void selected(int position) {
                category = UnitCatalog.CATEGORIES.get(position);
                updateUnitSpinners(0, Math.min(1, category.units.size() - 1));
            }
        });
        fromSpinner.setOnItemSelectedListener(new SimpleSelect() {
            @Override public void selected(int position) { update(); }
        });
        toSpinner.setOnItemSelectedListener(new SimpleSelect() {
            @Override public void selected(int position) { update(); }
        });
        valueInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { update(); }
        });
        updateUnitSpinners(0, Math.min(1, category.units.size() - 1));
    }

    private TextView label(String text) {
        TextView label = Ui.text(this, text, 10, theme.muted, true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 30));
        params.topMargin = Ui.dp(this, 10);
        label.setLayoutParams(params);
        return label;
    }

    private Spinner spinner() {
        Spinner spinner = new Spinner(this, Spinner.MODE_DROPDOWN);
        spinner.setBackground(AppTheme.background(this, theme.surface, 11));
        return spinner;
    }

    private LinearLayout.LayoutParams spinnerParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
    }

    private ArrayAdapter<String> stringAdapter(List<String> values) {
        return Ui.adapter(this, values, theme.text);
    }

    private void updateUnitSpinners(int from, int to) {
        List<String> values = new ArrayList<>();
        for (UnitCatalog.Unit unit : category.units) values.add(unit.label + " · " + unit.symbol);
        fromSpinner.setAdapter(stringAdapter(values));
        toSpinner.setAdapter(stringAdapter(values));
        fromSpinner.setSelection(from);
        toSpinner.setSelection(to);
        update();
    }

    private void update() {
        if (category == null || fromSpinner == null || fromSpinner.getSelectedItemPosition() < 0) return;
        UnitCatalog.Unit from = category.units.get(fromSpinner.getSelectedItemPosition());
        UnitCatalog.Unit to = category.units.get(Math.max(0, toSpinner.getSelectedItemPosition()));
        double value;
        try {
            value = Double.parseDouble(valueInput.getText().toString().replace(',', '.'));
        } catch (NumberFormatException error) {
            value = 0;
        }
        converted = UnitCatalog.convert(category, from, to, value);
        output.setText(format(converted));
        outputUnit.setText(from.label + " → " + to.label + "  ·  " + to.symbol);
        alternatives.removeAllViews();
        int count = 0;
        for (UnitCatalog.Unit unit : UnitCatalog.alternatives(category, from)) {
            if (count++ >= 6) break;
            double result = UnitCatalog.convert(category, from, unit, value);
            LinearLayout row = Ui.row(this, Gravity.CENTER_VERTICAL);
            row.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
            row.setBackground(AppTheme.background(this, theme.surface, 10));
            row.addView(Ui.text(this, unit.label + " · " + unit.symbol, 10, theme.muted, false),
                    new LinearLayout.LayoutParams(0, Ui.dp(this, 30), 1));
            TextView valueView = Ui.text(this, format(result), 11, theme.text, false);
            valueView.setGravity(Gravity.END);
            valueView.setTypeface(Typeface.MONOSPACE);
            row.addView(valueView, new LinearLayout.LayoutParams(Ui.dp(this, 145), Ui.dp(this, 30)));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 43));
            params.bottomMargin = Ui.dp(this, 5);
            alternatives.addView(row, params);
        }
    }

    private void swap() {
        int from = fromSpinner.getSelectedItemPosition();
        int to = toSpinner.getSelectedItemPosition();
        fromSpinner.setSelection(to);
        toSpinner.setSelection(from);
        update();
    }

    private void useResult() {
        prefs.setExpression(format(converted));
        Intent intent = new Intent(this, CalculatorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private abstract static class SimpleSelect implements AdapterView.OnItemSelectedListener {
        abstract void selected(int position);

        @Override
        public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
            selected(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
}
