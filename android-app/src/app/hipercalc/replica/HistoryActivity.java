package app.hipercalc.replica;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public final class HistoryActivity extends BaseActivity {
    private LinearLayout list;
    private TextView countView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("计算历史", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));

        LinearLayout toolbar = Ui.row(this, Gravity.CENTER_VERTICAL);
        toolbar.setPadding(Ui.dp(this, 18), Ui.dp(this, 4), Ui.dp(this, 18), Ui.dp(this, 10));
        countView = Ui.text(this, "", 11, theme.muted, false);
        toolbar.addView(countView, new LinearLayout.LayoutParams(0, Ui.dp(this, 40), 1));
        Button clear = Ui.button(this, "清空历史", theme.danger, theme.surface, 11, 11, false);
        clear.setOnClickListener(v -> {
            prefs.clearHistory();
            render();
        });
        toolbar.addView(clear, new LinearLayout.LayoutParams(Ui.dp(this, 94), Ui.dp(this, 36)));
        root.addView(toolbar);

        ScrollView scroll = new ScrollView(this);
        list = Ui.column(this);
        list.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), Ui.dp(this, 18));
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        render();
        updateCount();
    }

    private void render() {
        list.removeAllViews();
        List<PrefsStore.HistoryEntry> history = prefs.history();
        updateCount();
        if (history.isEmpty()) {
            TextView empty = Ui.text(this, "尚未保存计算。\n按等号后，结果会出现在这里。", 13, theme.muted, false);
            empty.setGravity(Gravity.CENTER);
            empty.setLineSpacing(0, 1.5f);
            list.addView(empty, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 240)));
            return;
        }
        for (PrefsStore.HistoryEntry entry : history) {
            LinearLayout card = Ui.column(this);
            card.setPadding(Ui.dp(this, 15), Ui.dp(this, 12), Ui.dp(this, 15), Ui.dp(this, 12));
            card.setBackground(AppTheme.background(this, theme.surface, 15));
            TextView expression = Ui.text(this, entry.expression, 11, theme.muted, false);
            expression.setSingleLine(true);
            expression.setTypeface(android.graphics.Typeface.MONOSPACE);
            String display = entry.display == null || entry.display.isEmpty() ? format(entry.value) : entry.display;
            TextView result = Ui.math(this, display, 25, theme.accentStrong);
            result.setSingleLine(true);
            card.addView(expression);
            card.addView(result, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 42)));
            LinearLayout actions = Ui.row(this, Gravity.END);
            TextView use = action("使用表达式");
            use.setOnClickListener(v -> {
                prefs.setExpression(entry.expression);
                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
            TextView insert = action("插入结果");
            insert.setOnClickListener(v -> {
                prefs.setExpression(display);
                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
            TextView delete = action("删除");
            delete.setTextColor(theme.danger);
            delete.setOnClickListener(v -> {
                prefs.removeHistory(entry.id);
                render();
            });
            actions.addView(use);
            actions.addView(insert);
            actions.addView(delete);
            card.addView(actions);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = Ui.dp(this, 9);
            list.addView(card, params);
        }
    }

    private void updateCount() {
        if (countView != null) countView.setText("共 " + prefs.history().size() + " 条");
    }

    private TextView action(String label) {
        TextView text = Ui.text(this, label, 10, theme.muted, false);
        text.setGravity(Gravity.CENTER);
        text.setPadding(Ui.dp(this, 10), Ui.dp(this, 8), Ui.dp(this, 10), Ui.dp(this, 8));
        return text;
    }
}
