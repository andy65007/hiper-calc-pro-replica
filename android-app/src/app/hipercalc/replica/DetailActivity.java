package app.hipercalc.replica;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public final class DetailActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        double value = getIntent().getDoubleExtra("value", prefs.ans());
        String suppliedDisplay = getIntent().getStringExtra("display");
        String expression = getIntent().getStringExtra("expression");
        String error = getIntent().getStringExtra("error");
        CalcEngine.Result evaluated = null;
        if (expression != null && !expression.isEmpty()) {
            try {
                evaluated = CalcEngine.evaluate(expression, new CalcEngine.Context(
                        prefs.angle(), prefs.base(), prefs.variables(), prefs.ans(), prefs.exactVariables()));
            } catch (CalcEngine.CalcException ignored) {
            }
        }
        LinearLayout root = contentColumn();
        root.addView(header("完整数值信息", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 20));

        LinearLayout hero = Ui.column(this);
        hero.setPadding(Ui.dp(this, 18), Ui.dp(this, 18), Ui.dp(this, 18), Ui.dp(this, 18));
        hero.setBackground(AppTheme.background(this, theme.surface, 18));
        hero.addView(Ui.text(this, "当前结果", 9, theme.muted, true));
        String display = suppliedDisplay == null || suppliedDisplay.isEmpty() ? format(value) : suppliedDisplay;
        TextView primary = Ui.math(this, display, 38, theme.accentStrong);
        primary.setSingleLine(true);
        hero.addView(primary);
        if (expression != null && !expression.isEmpty()) {
            TextView source = Ui.text(this, expression, 11, theme.muted, false);
            source.setTypeface(android.graphics.Typeface.MONOSPACE);
            source.setSingleLine(true);
            hero.addView(source);
        }
        if (error != null && !error.isEmpty()) {
            hero.addView(Ui.text(this, error, 10, theme.danger, false));
        }
        body.addView(hero);

        CalcEngine.Fraction fraction = CalcEngine.closestFraction(value);
        String exactFraction = CalcEngine.exactFraction(evaluated);
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"十进制 DEC", CalcEngine.formatNumber(value, 15, "auto", false, false)});
        rows.add(new String[]{"精确分数", exactFraction == null
                ? (fraction == null ? "—" : fraction.numerator + "/" + fraction.denominator)
                : exactFraction});
        rows.add(new String[]{"带分数", CalcEngine.mixedNumber(exactFraction) == null ? "—" : CalcEngine.mixedNumber(exactFraction)});
        rows.add(new String[]{"科学计数", CalcEngine.formatNumber(value, prefs.precision(), "scientific", false, false)});
        rows.add(new String[]{"工程计数", CalcEngine.formatNumber(value, prefs.precision(), "engineering", false, false)});
        rows.add(new String[]{"工程 SI", CalcEngine.formatNumber(value, prefs.precision(), "eng_si", false, false)});
        rows.add(new String[]{"度-分-秒", CalcEngine.dms(value, true)});
        rows.add(new String[]{"度-分", CalcEngine.dms(value, false)});
        rows.add(new String[]{"极坐标", CalcEngine.displayMode(evaluated, "POLAR", prefs.precision(), prefs.notation(), prefs.grouped(), false)});
        rows.add(new String[]{"百分比", CalcEngine.formatNumber(value * 100, prefs.precision(), "auto", false, false) + "%"});
        if (Double.isFinite(value) && Math.abs(value) < 9007199254740991.0) {
            long integer = Math.round(value);
            rows.add(new String[]{"十六进制 HEX", Long.toString(integer, 16).toUpperCase()});
            rows.add(new String[]{"八进制 OCT", Long.toString(integer, 8)});
            rows.add(new String[]{"二进制 BIN", Long.toString(integer, 2)});
        }

        LinearLayout info = Ui.column(this);
        info.setBackground(AppTheme.background(this, theme.surface, 14));
        for (String[] row : rows) info.addView(infoRow(row[0], row[1]));
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        infoParams.topMargin = Ui.dp(this, 12);
        body.addView(info, infoParams);

        Button copy = Ui.button(this, "复制结果", theme.accentInk, theme.accent, 11, 12, true);
        copy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("HiPER result", display));
            toast("已复制结果");
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44));
        buttonParams.topMargin = Ui.dp(this, 14);
        body.addView(copy, buttonParams);
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private LinearLayout infoRow(String label, String value) {
        LinearLayout row = Ui.row(this, Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.dp(this, 13), Ui.dp(this, 10), Ui.dp(this, 13), Ui.dp(this, 10));
        TextView title = Ui.text(this, label, 10, theme.muted, false);
        row.addView(title, new LinearLayout.LayoutParams(0, Ui.dp(this, 34), .8f));
        TextView content = Ui.text(this, value, 10, theme.text, false);
        content.setGravity(Gravity.END);
        content.setTypeface(android.graphics.Typeface.MONOSPACE);
        content.setSingleLine(true);
        row.addView(content, new LinearLayout.LayoutParams(0, Ui.dp(this, 34), 1.7f));
        return row;
    }
}
