package app.hipercalc.replica;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class ThemeActivity extends BaseActivity {
    private static final String[][] THEMES = {
            {"modern-dark", "现代深色"}, {"modern-light", "现代浅色"},
            {"classic", "经典"}, {"shades", "渐变"},
            {"brushed", "拉丝"}, {"deep", "深蓝"},
            {"luxfer", "荧光"}, {"amoled", "AMOLED"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("主题", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 15), Ui.dp(this, 6), Ui.dp(this, 15), Ui.dp(this, 20));
        TextView note = Ui.text(this, "主题名称来自本地 APK 的资源清单；配色为根据资源用途重建的近似方案。", 11, theme.muted, false);
        note.setLineSpacing(0, 1.5f);
        body.addView(note, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));

        for (int i = 0; i < THEMES.length; i += 2) {
            LinearLayout row = Ui.row(this, Gravity.CENTER_VERTICAL);
            row.addView(themeCard(THEMES[i][0], THEMES[i][1]),
                    new LinearLayout.LayoutParams(0, Ui.dp(this, 120), 1));
            LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(0, Ui.dp(this, 120), 1);
            right.leftMargin = Ui.dp(this, 10);
            row.addView(themeCard(THEMES[i + 1][0], THEMES[i + 1][1]), right);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 130));
            body.addView(row, params);
        }
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private LinearLayout themeCard(String id, String label) {
        AppTheme preview = AppTheme.get(id);
        LinearLayout card = Ui.column(this);
        card.setGravity(Gravity.CENTER);
        card.setPadding(Ui.dp(this, 10), Ui.dp(this, 15), Ui.dp(this, 10), Ui.dp(this, 10));
        card.setBackground(AppTheme.background(this, preview.surface, 16));
        TextView dot = Ui.text(this, "●", 30, preview.accent, false);
        dot.setGravity(Gravity.CENTER);
        TextView title = Ui.text(this, label, 12, preview.text, false);
        title.setGravity(Gravity.CENTER);
        TextView state = Ui.text(this, id.equals(prefs.theme()) ? "当前主题" : "点击应用", 9, preview.muted, false);
        state.setGravity(Gravity.CENTER);
        card.addView(dot);
        card.addView(title);
        card.addView(state);
        card.setOnClickListener(v -> {
            prefs.setTheme(id);
            recreate();
        });
        return card;
    }
}
