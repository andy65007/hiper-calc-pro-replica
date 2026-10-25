package app.hipercalc.replica;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public final class KeyboardParamsActivity extends BaseActivity {
    private LinearLayout choices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("键盘参数", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 20));
        TextView intro = Ui.text(this, "选择启动计算器时默认展示的键盘页，并调整按键尺寸和反馈。", 11, theme.muted, false);
        intro.setLineSpacing(0, 1.5f);
        body.addView(intro, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));

        body.addView(Ui.text(this, "默认键盘页", 12, theme.text, true),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 38)));
        choices = Ui.row(this, Gravity.CENTER_VERTICAL);
        body.addView(choices, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 48)));

        LinearLayout controls = card();
        controls.addView(toggle("紧凑键盘", prefs.compact(), value -> prefs.setCompact(value)));
        controls.addView(toggle("触感反馈", prefs.haptics(), value -> prefs.setHaptics(value)));
        controls.addView(toggle("按键音", prefs.keySound(), value -> prefs.setKeySound(value)));
        LinearLayout.LayoutParams controlsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        controlsParams.topMargin = Ui.dp(this, 14);
        body.addView(controls, controlsParams);

        Button functions = Ui.button(this, "查看按键与函数映射", theme.text, theme.surfaceRaised, 11, 12, false);
        functions.setOnClickListener(v -> open(KeysFunctionsActivity.class));
        LinearLayout.LayoutParams functionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44));
        functionParams.topMargin = Ui.dp(this, 14);
        body.addView(functions, functionParams);
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        renderChoices();
    }

    private void renderChoices() {
        choices.removeAllViews();
        String[][] values = {{"basic", "基础"}, {"scientific", "科学"}, {"algebra", "代数"}};
        for (String[] value : values) {
            Button button = Ui.button(this, value[1], value[0].equals(prefs.keypad()) ? theme.accentInk : theme.muted,
                    value[0].equals(prefs.keypad()) ? theme.accent : theme.surfaceRaised, 10, 11, false);
            button.setOnClickListener(v -> {
                prefs.setKeypad(value[0]);
                renderChoices();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, Ui.dp(this, 38), 1);
            params.rightMargin = Ui.dp(this, 6);
            choices.addView(button, params);
        }
    }

    private LinearLayout card() {
        LinearLayout card = Ui.column(this);
        card.setPadding(Ui.dp(this, 15), Ui.dp(this, 8), Ui.dp(this, 15), Ui.dp(this, 10));
        card.setBackground(AppTheme.background(this, theme.surface, 16));
        return card;
    }

    private LinearLayout toggle(String title, boolean checked, ToggleListener listener) {
        LinearLayout row = Ui.row(this, Gravity.CENTER_VERTICAL);
        row.addView(Ui.text(this, title, 12, theme.text, false),
                new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 1));
        Switch toggle = new Switch(this);
        toggle.setChecked(checked);
        toggle.setOnCheckedChangeListener((view, value) -> listener.changed(value));
        row.addView(toggle, new LinearLayout.LayoutParams(Ui.dp(this, 62), Ui.dp(this, 44)));
        return row;
    }

    private interface ToggleListener {
        void changed(boolean value);
    }
}
