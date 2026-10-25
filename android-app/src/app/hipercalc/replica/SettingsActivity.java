package app.hipercalc.replica;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Arrays;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public final class SettingsActivity extends BaseActivity {
    private static final int REQUEST_EXPORT = 71;
    private static final int REQUEST_IMPORT = 72;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("设置", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 4), Ui.dp(this, 16), Ui.dp(this, 24));

        body.addView(sectionTitle("计算"));
        LinearLayout calculation = card();
        calculation.addView(settingTitle("角度单位", "控制三角函数与反三角函数"));
        Spinner angle = spinner(Arrays.asList("度 DEG", "弧度 RAD", "百分度 GRAD"));
        angle.setSelection("rad".equals(prefs.angle()) ? 1 : "grad".equals(prefs.angle()) ? 2 : 0);
        angle.setOnItemSelectedListener(new Select(position -> {
            prefs.setAngle(position == 0 ? "deg" : position == 1 ? "rad" : "grad");
        }));
        calculation.addView(angle, spinnerParams());

        calculation.addView(settingTitle("有效数字", "显示 3–100 位有效数字；无函数表达式使用 BigDecimal 精确计算"));
        TextView precisionValue = Ui.text(this, String.valueOf(prefs.precision()), 11, theme.accent, true);
        SeekBar precision = new SeekBar(this);
        precision.setMax(97);
        precision.setProgress(prefs.precision() - 3);
        precision.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = progress + 3;
                precisionValue.setText(String.valueOf(value));
                if (fromUser) prefs.setPrecision(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        calculation.addView(precisionValue);
        calculation.addView(precision);

        calculation.addView(settingTitle("数字格式", "极大数和极小数显示方式"));
        Spinner notation = spinner(Arrays.asList("自动 NORM", "固定小数 FIX", "科学计数 SCI", "工程计数 ENG", "工程 SI ENG_SI"));
        notation.setSelection("fixed".equals(prefs.notation()) ? 1 : "scientific".equals(prefs.notation()) ? 2
                : "engineering".equals(prefs.notation()) ? 3 : "eng_si".equals(prefs.notation()) ? 4 : 0);
        notation.setOnItemSelectedListener(new Select(position -> {
            prefs.setNotation(position == 1 ? "fixed" : position == 2 ? "scientific"
                    : position == 3 ? "engineering" : position == 4 ? "eng_si" : "auto");
        }));
        calculation.addView(notation, spinnerParams());

        calculation.addView(settingTitle("结果显示", "十进制 / 表达式 / 带分数 / DMS / DM / 极坐标"));
        Spinner resultMode = spinner(Arrays.asList("十进制", "表达式", "带分数", "度-分-秒 DMS", "度-分 DM", "极坐标 POLAR"));
        resultMode.setSelection(resultModeIndex(prefs.resultMode()));
        resultMode.setOnItemSelectedListener(new Select(position -> {
            String[] modes = {"DECIMAL", "EXPRESSION", "MIXED_NUMBER", "DMS", "DM", "POLAR"};
            prefs.setResultMode(modes[Math.max(0, Math.min(modes.length - 1, position))]);
        }));
        calculation.addView(resultMode, spinnerParams());
        calculation.addView(toggle("立即计算预览", prefs.immediateEvaluation(),
                prefs::setImmediateEvaluation));
        calculation.addView(toggle("显示循环小数", prefs.showRepeatingDecimals(),
                prefs::setShowRepeatingDecimals));
        body.addView(calculation);

        body.addView(sectionTitle("外观"));
        LinearLayout appearance = card();
        appearance.addView(settingTitle("主题", "现代、经典、拉丝、深色与 AMOLED"));
        Button themeButton = Ui.button(this, "选择主题", theme.text, theme.surfaceRaised, 10, 12, false);
        themeButton.setOnClickListener(v -> open(ThemeActivity.class));
        appearance.addView(themeButton, new LinearLayout.LayoutParams(Ui.dp(this, 130), Ui.dp(this, 42)));
        appearance.addView(toggle("千位分隔", prefs.grouped(), value -> prefs.setGrouped(value)));
        appearance.addView(toggle("隐藏状态栏", prefs.hideStatusBar(), value -> {
            prefs.setHideStatusBar(value);
            recreate();
        }));
        appearance.addView(toggle("隐藏导航按钮", prefs.hideNavigationButtons(), value -> {
            prefs.setHideNavigationButtons(value);
            recreate();
        }));
        body.addView(appearance);

        body.addView(sectionTitle("键盘与反馈"));
        LinearLayout keyboard = card();
        Button keyboardLayout = Ui.button(this, "键盘布局与按键功能", theme.text, theme.surfaceRaised, 10, 12, false);
        keyboardLayout.setOnClickListener(v -> open(KeyboardParamsActivity.class));
        keyboard.addView(keyboardLayout, new LinearLayout.LayoutParams(Ui.dp(this, 190), Ui.dp(this, 42)));
        keyboard.addView(toggle("紧凑键盘", prefs.compact(), value -> prefs.setCompact(value)));
        keyboard.addView(toggle("触感反馈", prefs.haptics(), value -> prefs.setHaptics(value)));
        keyboard.addView(toggle("按键音", prefs.keySound(), value -> prefs.setKeySound(value)));
        keyboard.addView(toggle("保持屏幕常亮", prefs.keepScreenOn(), value -> prefs.setKeepScreenOn(value)));
        keyboard.addView(toggle("显示闪烁光标", prefs.blinkingCursor(), value -> prefs.setBlinkingCursor(value)));
        body.addView(keyboard);

        body.addView(sectionTitle("本地数据"));
        LinearLayout data = card();
        data.addView(Ui.text(this, "历史与变量仅保存在本机应用数据中，不需要 Google 登录、许可验证或网络连接。", 11, theme.muted, false));
        LinearLayout historyLimitRow = Ui.row(this, Gravity.CENTER_VERTICAL);
        historyLimitRow.addView(Ui.text(this, "历史最大条数", 12, theme.text, false),
                new LinearLayout.LayoutParams(0, Ui.dp(this, 42), 1));
        EditText historyLimit = new EditText(this);
        historyLimit.setText(String.valueOf(prefs.historyLimit()));
        historyLimit.setTextColor(theme.text);
        historyLimit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        historyLimit.setSingleLine(true);
        historyLimit.setBackground(AppTheme.background(this, theme.surfaceRaised, 9));
        historyLimitRow.addView(historyLimit, new LinearLayout.LayoutParams(Ui.dp(this, 80), Ui.dp(this, 40)));
        Button applyLimit = Ui.button(this, "应用", theme.text, theme.surfaceRaised, 9, 10, false);
        applyLimit.setOnClickListener(v -> {
            try {
                prefs.setHistoryLimit(Integer.parseInt(historyLimit.getText().toString()));
                toast("历史容量已更新");
            } catch (NumberFormatException error) {
                toast("请输入有效条数");
            }
        });
        LinearLayout.LayoutParams applyParams = new LinearLayout.LayoutParams(Ui.dp(this, 60), Ui.dp(this, 40));
        applyParams.leftMargin = Ui.dp(this, 6);
        historyLimitRow.addView(applyLimit, applyParams);
        data.addView(historyLimitRow);
        data.addView(toggle("移除连续重复历史", prefs.removeDuplicateHistory(),
                prefs::setRemoveDuplicateHistory));
        Button clearHistory = Ui.button(this, "清空计算历史", theme.danger, theme.surfaceRaised, 10, 11, false);
        clearHistory.setOnClickListener(v -> {
            prefs.clearHistory();
            toast("历史已清空");
        });
        Button clearVariables = Ui.button(this, "清空全部变量", theme.danger, theme.surfaceRaised, 10, 11, false);
        clearVariables.setOnClickListener(v -> {
            prefs.setVariables(new java.util.HashMap<>());
            toast("变量已清空");
        });
        LinearLayout buttons = Ui.row(this, Gravity.CENTER_VERTICAL);
        buttons.addView(clearHistory, new LinearLayout.LayoutParams(0, Ui.dp(this, 42), 1));
        LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(0, Ui.dp(this, 42), 1);
        right.leftMargin = Ui.dp(this, 8);
        buttons.addView(clearVariables, right);
        data.addView(buttons);
        Button export = Ui.button(this, "导出备份", theme.text, theme.surfaceRaised, 10, 11, false);
        export.setOnClickListener(v -> exportBackup());
        Button importButton = Ui.button(this, "导入备份", theme.text, theme.surfaceRaised, 10, 11, false);
        importButton.setOnClickListener(v -> importBackup());
        LinearLayout transfer = Ui.row(this, Gravity.CENTER_VERTICAL);
        transfer.addView(export, new LinearLayout.LayoutParams(0, Ui.dp(this, 42), 1));
        LinearLayout.LayoutParams importParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 42), 1);
        importParams.leftMargin = Ui.dp(this, 8);
        transfer.addView(importButton, importParams);
        data.addView(transfer);
        body.addView(data);

        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private TextView sectionTitle(String text) {
        TextView title = Ui.text(this, text, 13, theme.text, true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 42));
        params.topMargin = Ui.dp(this, 10);
        title.setLayoutParams(params);
        return title;
    }

    private LinearLayout card() {
        LinearLayout card = Ui.column(this);
        card.setPadding(Ui.dp(this, 15), Ui.dp(this, 12), Ui.dp(this, 15), Ui.dp(this, 14));
        card.setBackground(AppTheme.background(this, theme.surface, 16));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = Ui.dp(this, 8);
        card.setLayoutParams(params);
        return card;
    }

    private TextView settingTitle(String title, String subtitle) {
        TextView text = Ui.text(this, title + "\n" + subtitle, 11, theme.muted, false);
        text.setLineSpacing(0, 1.4f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54));
        params.topMargin = Ui.dp(this, 5);
        text.setLayoutParams(params);
        return text;
    }

    private Spinner spinner(java.util.List<String> values) {
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(Ui.adapter(this, values, theme.text));
        spinner.setBackground(AppTheme.background(this, theme.surfaceRaised, 10));
        return spinner;
    }

    private LinearLayout.LayoutParams spinnerParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
    }

    private LinearLayout toggle(String title, boolean value, ToggleListener listener) {
        LinearLayout row = Ui.row(this, Gravity.CENTER_VERTICAL);
        row.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 4));
        row.addView(Ui.text(this, title, 12, theme.text, false),
                new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 1));
        Switch toggle = new Switch(this);
        toggle.setChecked(value);
        toggle.setTextColor(theme.text);
        toggle.setOnCheckedChangeListener((buttonView, checked) -> listener.changed(checked));
        row.addView(toggle, new LinearLayout.LayoutParams(Ui.dp(this, 62), Ui.dp(this, 44)));
        return row;
    }

    private interface ToggleListener {
        void changed(boolean value);
    }

    private static final class Select implements AdapterView.OnItemSelectedListener {
        private final PositionListener listener;

        Select(PositionListener listener) {
            this.listener = listener;
        }

        @Override public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
            listener.selected(position);
        }
        @Override public void onNothingSelected(AdapterView<?> parent) {}
    }

    private interface PositionListener {
        void selected(int position);
    }

    private int resultModeIndex(String mode) {
        String[] modes = {"DECIMAL", "EXPRESSION", "MIXED_NUMBER", "DMS", "DM", "POLAR"};
        for (int i = 0; i < modes.length; i++) if (modes[i].equals(mode)) return i;
        return 0;
    }

    private void exportBackup() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "hiper-calc-backup.json");
        startActivityForResult(intent, REQUEST_EXPORT);
    }

    private void importBackup() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQUEST_IMPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == REQUEST_EXPORT) {
            try (OutputStream output = getContentResolver().openOutputStream(uri, "wt")) {
                if (output == null) throw new IOException("无法打开输出文件");
                output.write(prefs.exportJson().getBytes(StandardCharsets.UTF_8));
                toast("备份已导出");
            } catch (IOException error) {
                toast("导出失败：" + error.getMessage());
            }
        } else if (requestCode == REQUEST_IMPORT) {
            try (InputStream input = getContentResolver().openInputStream(uri)) {
                if (input == null) throw new IOException("无法打开备份文件");
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int count;
                while ((count = input.read(chunk)) != -1) buffer.write(chunk, 0, count);
                String json = buffer.toString(StandardCharsets.UTF_8.name());
                prefs.importJson(json);
                toast("备份已导入");
                recreate();
            } catch (IOException | org.json.JSONException error) {
                toast("导入失败：" + error.getMessage());
            }
        }
    }
}
