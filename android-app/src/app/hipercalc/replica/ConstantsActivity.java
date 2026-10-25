package app.hipercalc.replica;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public final class ConstantsActivity extends BaseActivity {
    private LinearLayout list;
    private LinearLayout tabs;
    private String group = "数学";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("科学常量", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        HorizontalScrollView tabScroll = new HorizontalScrollView(this);
        tabScroll.setHorizontalScrollBarEnabled(false);
        tabs = Ui.row(this, Gravity.CENTER_VERTICAL);
        tabs.setPadding(Ui.dp(this, 14), Ui.dp(this, 4), Ui.dp(this, 14), Ui.dp(this, 10));
        tabScroll.addView(tabs);
        root.addView(tabScroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54)));

        ScrollView scroll = new ScrollView(this);
        list = Ui.column(this);
        list.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), Ui.dp(this, 18));
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        renderTabs();
        renderList();
    }

    private void renderTabs() {
        tabs.removeAllViews();
        for (String name : new String[]{"数学", "物理", "天文", "化学"}) {
            TextView tab = Ui.text(this, name, 11, name.equals(group) ? theme.text : theme.muted, false);
            tab.setGravity(Gravity.CENTER);
            tab.setBackground(AppTheme.background(this, name.equals(group) ? theme.surfaceRaised : theme.surface, 10));
            tab.setOnClickListener(v -> {
                group = name;
                renderTabs();
                renderList();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Ui.dp(this, 74), Ui.dp(this, 36));
            params.rightMargin = Ui.dp(this, 7);
            tabs.addView(tab, params);
        }
    }

    private void renderList() {
        list.removeAllViews();
        List<ConstantCatalog.Entry> entries = ConstantCatalog.group(group);
        for (ConstantCatalog.Entry entry : entries) {
            LinearLayout card = Ui.row(this, Gravity.CENTER_VERTICAL);
            card.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
            card.setBackground(AppTheme.background(this, theme.surface, 14));
            TextView symbol = Ui.math(this, entry.symbol, 19, theme.signal);
            symbol.setGravity(Gravity.CENTER);
            symbol.setBackground(AppTheme.background(this, Ui.withAlpha(theme.signal, 22), 10));
            card.addView(symbol, new LinearLayout.LayoutParams(Ui.dp(this, 46), Ui.dp(this, 46)));
            LinearLayout copy = Ui.column(this);
            copy.addView(Ui.text(this, entry.name, 12, theme.text, false));
            copy.addView(Ui.text(this, entry.display, 10, theme.muted, false));
            LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            copyParams.leftMargin = Ui.dp(this, 12);
            card.addView(copy, copyParams);
            TextView insert = Ui.text(this, "插入", 10, theme.accent, false);
            insert.setGravity(Gravity.CENTER);
            insert.setOnClickListener(v -> insert(entry.expression));
            card.addView(insert, new LinearLayout.LayoutParams(Ui.dp(this, 54), Ui.dp(this, 38)));
            card.setOnClickListener(v -> insert(entry.expression));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = Ui.dp(this, 8);
            list.addView(card, params);
        }
    }

    private void insert(String expression) {
        prefs.setExpression(expression);
        Intent intent = new Intent(this, CalculatorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
