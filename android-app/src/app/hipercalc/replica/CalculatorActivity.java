package app.hipercalc.replica;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CalculatorActivity extends BaseActivity {
    private enum KeyKind { NORMAL, FUNCTION, OPERATOR, UTILITY, EQUALS }

    private static final class Key {
        final String label;
        String insert;
        String action;
        String sub;
        KeyKind kind = KeyKind.NORMAL;
        Key second;

        Key(String label) {
            this.label = label;
        }

        Key insert(String text) {
            insert = text;
            return this;
        }

        Key action(String value) {
            action = value;
            return this;
        }

        Key sub(String value) {
            sub = value;
            return this;
        }

        Key kind(KeyKind value) {
            kind = value;
            return this;
        }

        Key second(String label, String text) {
            second = new Key(label).insert(text).kind(kind);
            return this;
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private EditText expressionView;
    private TextView resultView;
    private TextView statusView;
    private TextView metaView;
    private TextView angleChip;
    private TextView baseChip;
    private TextView secondChip;
    private LinearLayout keypadHost;
    private final List<Runnable> pendingEvaluation = new ArrayList<>();
    private double currentResult;
    private String currentDisplay = "0";
    private boolean validResult;
    private String currentError = "";
    private String keypad = "basic";
    private boolean second;
    private boolean suppressTextChange;
    private ToneGenerator tone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentResult = prefs.ans();
        keypad = prefs.keypad();
        build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppTheme latest = AppTheme.get(prefs.theme());
        if (!latest.id.equals(theme.id)) {
            recreate();
            return;
        }
        refreshStatus();
        syncExternalExpression();
        expressionView.setCursorVisible(prefs.blinkingCursor());
        evaluate(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        syncExternalExpression();
    }

    @Override
    protected void onDestroy() {
        if (tone != null) tone.release();
        super.onDestroy();
    }

    private void build() {
        LinearLayout root = contentColumn();
        root.setBackgroundColor(theme.background);
        root.addView(buildTopBar(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        root.addView(buildDisplay(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 2.15f));
        root.addView(buildQuickTools(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 42)));
        root.addView(buildKeyboard(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 5.4f));

        expressionView.setText(prefs.expression());
        expressionView.setSelection(expressionView.length());
        evaluate(false);
    }

    private View buildTopBar() {
        LinearLayout bar = Ui.row(this, Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 16), Ui.dp(this, 5), Ui.dp(this, 8), Ui.dp(this, 5));
        bar.setBackgroundColor(theme.background);
        LinearLayout brand = Ui.column(this);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo = Ui.text(this, "HiPER", 20, theme.text, true);
        logo.setLetterSpacing(.03f);
        TextView sub = Ui.text(this, "CALC PRO", 8, theme.muted, true);
        sub.setLetterSpacing(.22f);
        brand.addView(logo);
        brand.addView(sub);
        bar.addView(brand, new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1));

        bar.addView(toolButton("◷", v -> open(HistoryActivity.class)));
        bar.addView(toolButton("M", v -> open(MemoryActivity.class)));
        bar.addView(toolButton("⇄", v -> open(ConversionsActivity.class)));
        bar.addView(toolButton("π", v -> open(ConstantsActivity.class)));
        bar.addView(toolButton("⋮", this::showMenu));
        return bar;
    }

    private View toolButton(String label, View.OnClickListener listener) {
        TextView button = Ui.text(this, label, "M".equals(label) ? 14 : 19, theme.muted, true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(AppTheme.background(this, theme.surface, 12));
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Ui.dp(this, 38), Ui.dp(this, 38));
        params.leftMargin = Ui.dp(this, 5);
        button.setLayoutParams(params);
        return button;
    }

    private void showMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, "设置");
        menu.getMenu().add(0, 2, 1, "主题");
        menu.getMenu().add(0, 3, 2, "解题");
        menu.getMenu().add(0, 4, 3, "识别表达式");
        menu.getMenu().add(0, 5, 4, "帮助");
        menu.getMenu().add(0, 6, 5, "关于");
        menu.getMenu().add(0, 7, 6, "矩阵与向量");
        menu.getMenu().add(0, 8, 7, "复数计算");
        menu.getMenu().add(0, 9, 8, "统计与回归");
        menu.getMenu().add(0, 10, 9, "整数与位运算");
        menu.getMenu().add(0, 11, 10, "极限与函数表");
        menu.getMenu().add(0, 12, 11, "数论与组合");
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: open(SettingsActivity.class); return true;
                case 2: open(ThemeActivity.class); return true;
                case 3: open(SolutionActivity.class); return true;
                case 4: open(RecognitionActivity.class); return true;
                case 5: open(HelpActivity.class); return true;
                case 6: open(AboutActivity.class); return true;
                case 7: open(MatrixActivity.class); return true;
                case 8: open(ComplexActivity.class); return true;
                case 9: open(StatisticsActivity.class); return true;
                case 10: open(BitwiseActivity.class); return true;
                case 11: open(LimitTableActivity.class); return true;
                case 12: open(NumberTheoryActivity.class); return true;
                default: return false;
            }
        });
        menu.show();
    }

    private View buildDisplay() {
        LinearLayout panel = Ui.column(this);
        panel.setPadding(Ui.dp(this, 20), Ui.dp(this, 12), Ui.dp(this, 20), Ui.dp(this, 12));
        panel.setBackground(AppTheme.background(this, theme.surface, 24));

        LinearLayout status = Ui.row(this, Gravity.CENTER_VERTICAL);
        angleChip = statusButton("DEG", v -> cycleAngle());
        baseChip = statusButton("DEC", v -> cycleBase());
        secondChip = statusButton("2ND", v -> toggleSecond());
        status.addView(angleChip);
        status.addView(baseChip);
        status.addView(secondChip);
        TextView spacer = new TextView(this);
        status.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));
        statusView = Ui.text(this, "", 9, theme.muted, false);
        status.addView(statusView);
        panel.addView(status);

        expressionView = new EditText(this);
        expressionView.setBackground(null);
        expressionView.setTextColor(theme.text);
        expressionView.setTextSize(27);
        expressionView.setGravity(Gravity.END | Gravity.BOTTOM);
        expressionView.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        expressionView.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        expressionView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        expressionView.setSingleLine(false);
        expressionView.setMaxLines(2);
        expressionView.setHint("输入算式");
        expressionView.setHintTextColor(theme.muted);
        expressionView.setPadding(0, Ui.dp(this, 8), 0, 0);
        expressionView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (suppressTextChange) return;
                prefs.setExpression(s.toString());
                if (prefs.immediateEvaluation()) evaluate(false);
            }
        });
        expressionView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                commit();
                return true;
            }
            return false;
        });
        panel.addView(expressionView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        resultView = Ui.math(this, "0", 42, theme.accentStrong);
        resultView.setGravity(Gravity.END);
        resultView.setSingleLine(true);
        resultView.setAutoSizeTextTypeUniformWithConfiguration(18, 48, 2,
                android.util.TypedValue.COMPLEX_UNIT_SP);
        resultView.setOnClickListener(v -> openDetail());
        LinearLayout.LayoutParams resultParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 64));
        resultParams.topMargin = Ui.dp(this, 2);
        panel.addView(resultView, resultParams);

        metaView = Ui.text(this, "输入算式以开始", 10, theme.muted, false);
        metaView.setGravity(Gravity.END);
        panel.addView(metaView);
        return panel;
    }

    private TextView statusButton(String text, View.OnClickListener listener) {
        TextView chip = Ui.text(this, text, 9, theme.muted, true);
        chip.setGravity(Gravity.CENTER);
        chip.setLetterSpacing(.08f);
        chip.setBackground(AppTheme.background(this, theme.surfaceRaised, 20));
        chip.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 25));
        params.rightMargin = Ui.dp(this, 6);
        chip.setLayoutParams(params);
        return chip;
    }

    private View buildQuickTools() {
        LinearLayout row = Ui.row(this, Gravity.CENTER);
        row.setBackgroundColor(theme.surface);
        addQuick(row, "±", v -> insert("-"));
        addQuick(row, "复制", v -> copyResult());
        addQuick(row, "Ans", v -> insert("Ans"));
        addQuick(row, "换算", v -> openConversions());
        addQuick(row, "精确", v -> openDetail());
        return row;
    }

    private void addQuick(LinearLayout row, String text, View.OnClickListener listener) {
        TextView button = Ui.text(this, text, 11, theme.muted, false);
        button.setGravity(Gravity.CENTER);
        button.setOnClickListener(listener);
        row.addView(button, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
    }

    private View buildKeyboard() {
        LinearLayout shell = Ui.column(this);
        shell.setPadding(Ui.dp(this, 10), Ui.dp(this, 7), Ui.dp(this, 10), Ui.dp(this, 10));
        shell.setBackgroundColor(theme.background);

        LinearLayout tabs = Ui.row(this, Gravity.CENTER_VERTICAL);
        tabs.addView(tab("基础", "basic"));
        tabs.addView(tab("科学", "scientific"));
        tabs.addView(tab("代数", "algebra"));
        tabs.addView(tab("高级", "advanced"));
        TextView spacer = new TextView(this);
        tabs.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));
        keypadHost = Ui.column(this);
        shell.addView(tabs, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 34)));
        shell.addView(keypadHost, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        renderKeypad();
        return shell;
    }

    private TextView tab(String label, String id) {
        TextView tab = Ui.text(this, label, 11, id.equals(keypad) ? theme.text : theme.muted, false);
        tab.setGravity(Gravity.CENTER);
        tab.setBackground(AppTheme.background(this, id.equals(keypad) ? theme.surfaceRaised : theme.surface, 9));
        tab.setOnClickListener(v -> {
            keypad = id;
            LinearLayout parent = (LinearLayout) v.getParent();
            String[] ids = {"basic", "scientific", "algebra", "advanced"};
            for (int i = 0; i < ids.length && i < parent.getChildCount(); i++) {
                TextView item = (TextView) parent.getChildAt(i);
                boolean active = ids[i].equals(keypad);
                item.setTextColor(active ? theme.text : theme.muted);
                item.setBackground(AppTheme.background(this, active ? theme.surfaceRaised : theme.surface, 9));
            }
            renderKeypad();
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Ui.dp(this, 58), Ui.dp(this, 29));
        params.rightMargin = Ui.dp(this, 5);
        tab.setLayoutParams(params);
        return tab;
    }

    private void renderKeypad() {
        if (keypadHost == null) return;
        keypadHost.removeAllViews();
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(5);
        grid.setRowCount((keys().size() + 4) / 5);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        grid.setUseDefaultMargins(false);
        List<Key> keys = keys();
        for (int i = 0; i < keys.size(); i++) {
            Key original = keys.get(i);
            Key key = second && original.second != null ? original.second : original;
            Button button = Ui.button(this, key.label, keyColor(key.kind), keyBackground(key.kind),
                    14, buttonSize(key), key.kind == KeyKind.EQUALS);
            button.setOnClickListener(v -> pressKey(original));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(i / 5, 1, 1f), GridLayout.spec(i % 5, 1, 1f));
            params.width = 0;
            params.height = 0;
            params.setMargins(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
            grid.addView(button, params);
        }
        keypadHost.addView(grid, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private float buttonSize(Key key) {
        int length = key.label.length();
        if (length <= 1) return 20;
        if (length <= 2) return 17;
        if (length <= 4) return 13;
        return 11;
    }

    private int keyColor(KeyKind kind) {
        switch (kind) {
            case FUNCTION: return theme.accent;
            case OPERATOR: return theme.signal;
            case UTILITY: return theme.muted;
            case EQUALS: return theme.accentInk;
            default: return theme.text;
        }
    }

    private int keyBackground(KeyKind kind) {
        return kind == KeyKind.EQUALS ? theme.accent : theme.surfaceRaised;
    }

    private List<Key> keys() {
        List<Key> list = new ArrayList<>();
        if ("advanced".equals(keypad)) {
            utilityRow(list);
            list.add(new Key("矩阵").action("open:matrix").kind(KeyKind.FUNCTION));
            list.add(new Key("复数").action("open:complex").kind(KeyKind.FUNCTION));
            list.add(new Key("统计").action("open:stats").kind(KeyKind.FUNCTION));
            list.add(new Key("回归").action("open:stats").kind(KeyKind.FUNCTION));
            list.add(new Key("位运算").action("open:bitwise").kind(KeyKind.FUNCTION));
            list.add(new Key("极限").action("open:limit").kind(KeyKind.FUNCTION));
            list.add(new Key("表格").action("open:limit").kind(KeyKind.FUNCTION));
            list.add(new Key("DMS").action("mode:DMS").kind(KeyKind.FUNCTION));
            list.add(new Key("∠").action("mode:POLAR").kind(KeyKind.FUNCTION));
            list.add(new Key("conj").action("open:complex").kind(KeyKind.FUNCTION));
            list.add(new Key("nCr").insert("nCr()").kind(KeyKind.FUNCTION));
            list.add(new Key("nPr").insert("nPr()").kind(KeyKind.FUNCTION));
            list.add(new Key("gcd").insert("gcd()").kind(KeyKind.FUNCTION));
            list.add(new Key("lcm").insert("lcm()").kind(KeyKind.FUNCTION));
            list.add(new Key("round").insert("round()").kind(KeyKind.FUNCTION));
            list.add(new Key("floor").insert("floor()").kind(KeyKind.FUNCTION));
            list.add(new Key("Σ").insert("sum(\"k^2\",1,10)").kind(KeyKind.FUNCTION));
            list.add(new Key("∫").insert("integral(\"sin(x)\",0,pi)").kind(KeyKind.FUNCTION));
            list.add(new Key("solve").insert("solve(\"x^2-2\",1)").kind(KeyKind.FUNCTION));
            list.add(new Key("M+").action("memory-add").kind(KeyKind.UTILITY));
            list.add(new Key("M−").action("memory-sub").kind(KeyKind.UTILITY));
            list.add(new Key("M").insert("M").kind(KeyKind.FUNCTION));
            list.add(new Key("LastX").insert("LastX").kind(KeyKind.FUNCTION));
            list.add(new Key("Ans").insert("Ans").kind(KeyKind.FUNCTION));
            list.add(new Key("=").action("calculate").kind(KeyKind.EQUALS));
        } else if ("scientific".equals(keypad)) {
            utilityRow(list);
            list.add(new Key("sin").insert("sin()").kind(KeyKind.FUNCTION).second("sin⁻¹", "asin()"));
            list.add(new Key("cos").insert("cos()").kind(KeyKind.FUNCTION).second("cos⁻¹", "acos()"));
            list.add(new Key("tan").insert("tan()").kind(KeyKind.FUNCTION).second("tan⁻¹", "atan()"));
            list.add(new Key("ln").insert("ln()").kind(KeyKind.FUNCTION).second("eˣ", "exp()"));
            list.add(new Key("log").insert("log()").kind(KeyKind.FUNCTION).second("10ˣ", "10^("));
            list.add(new Key("sinh").insert("sinh()").kind(KeyKind.FUNCTION));
            list.add(new Key("cosh").insert("cosh()").kind(KeyKind.FUNCTION));
            list.add(new Key("tanh").insert("tanh()").kind(KeyKind.FUNCTION));
            list.add(new Key("e").insert("e").kind(KeyKind.FUNCTION));
            list.add(new Key("×").insert("*").kind(KeyKind.OPERATOR));
            list.add(new Key("π").insert("π").kind(KeyKind.FUNCTION));
            list.add(new Key("n!").insert("!").kind(KeyKind.FUNCTION));
            list.add(new Key("nCr").insert("nCr()").kind(KeyKind.FUNCTION).sub("C(n,r)"));
            list.add(new Key("nPr").insert("nPr()").kind(KeyKind.FUNCTION).sub("P(n,r)"));
            list.add(new Key("=").action("calculate").kind(KeyKind.EQUALS));
        } else if ("algebra".equals(keypad)) {
            utilityRow(list);
            for (String variable : new String[]{"x", "y", "z", "A", "B"}) {
                list.add(new Key(variable).insert(variable).kind(KeyKind.FUNCTION));
            }
            for (String variable : new String[]{"C", "D", "M", "Ans"}) {
                list.add(new Key(variable).insert(variable).kind(KeyKind.FUNCTION));
            }
            list.add(new Key("×").insert("*").kind(KeyKind.OPERATOR));
            list.add(new Key("d/dx").insert("derivative(\"x^2\",3)").kind(KeyKind.FUNCTION));
            list.add(new Key("∫").insert("integral(\"sin(x)\",0,pi)").kind(KeyKind.FUNCTION));
            list.add(new Key("Σ").insert("sum(\"k^2\",1,10)").kind(KeyKind.FUNCTION));
            list.add(new Key("solve").insert("solve(\"x^2-2\",1)").kind(KeyKind.FUNCTION));
            list.add(new Key("+").insert("+").kind(KeyKind.OPERATOR));
            list.add(new Key("=").insert("=").kind(KeyKind.OPERATOR));
            list.add(new Key("<").insert("<").kind(KeyKind.OPERATOR));
            list.add(new Key(">").insert(">").kind(KeyKind.OPERATOR));
            list.add(new Key("=").action("calculate").kind(KeyKind.EQUALS));
        } else {
            utilityRow(list);
            for (String number : new String[]{"7", "8", "9"}) list.add(new Key(number).insert(number));
            list.add(new Key("√").insert("sqrt()").kind(KeyKind.FUNCTION).second("∛", "cbrt()"));
            list.add(new Key("×").insert("*").kind(KeyKind.OPERATOR));
            for (String number : new String[]{"4", "5", "6"}) list.add(new Key(number).insert(number));
            list.add(new Key("x²").insert("^2").kind(KeyKind.FUNCTION).second("x³", "^3"));
            list.add(new Key("−").insert("-").kind(KeyKind.OPERATOR));
            for (String number : new String[]{"1", "2", "3"}) list.add(new Key(number).insert(number));
            list.add(new Key("xʸ").insert("^").kind(KeyKind.FUNCTION));
            list.add(new Key("+").insert("+").kind(KeyKind.OPERATOR));
            list.add(new Key("±").action("sign").kind(KeyKind.UTILITY));
            list.add(new Key("0").insert("0"));
            list.add(new Key(".").insert("."));
            list.add(new Key("n!").insert("!").kind(KeyKind.FUNCTION));
            list.add(new Key("=").action("calculate").kind(KeyKind.EQUALS));
        }
        return list;
    }

    private void utilityRow(List<Key> list) {
        list.add(new Key("AC").action("clear").kind(KeyKind.UTILITY));
        list.add(new Key("(").insert("("));
        list.add(new Key(")").insert(")"));
        list.add(new Key("⌫").action("backspace").kind(KeyKind.UTILITY));
        list.add(new Key("÷").insert("/").kind(KeyKind.OPERATOR));
    }

    private void pressKey(Key original) {
        Key key = second && original.second != null ? original.second : original;
        if ("calculate".equals(key.action)) commit();
        else if ("clear".equals(key.action)) clear();
        else if ("backspace".equals(key.action)) backspace();
        else if ("sign".equals(key.action)) insert("-");
        else if (key.action != null && key.action.startsWith("open:")) openAdvanced(key.action.substring(5));
        else if (key.action != null && key.action.startsWith("mode:")) {
            prefs.setResultMode(key.action.substring(5));
            evaluate(false);
            toast("结果显示模式：" + key.action.substring(5));
        }
        else if ("memory-add".equals(key.action)) memoryAdd(1);
        else if ("memory-sub".equals(key.action)) memoryAdd(-1);
        else if (key.insert != null) {
            if (key.insert.endsWith("()")) insertWithCaretInside(key.insert);
            else insert(key.insert);
        }
        feedback();
        if (second && original.second != null) toggleSecond();
    }

    private void insert(String text) {
        int start = Math.max(0, expressionView.getSelectionStart());
        int end = Math.max(start, expressionView.getSelectionEnd());
        expressionView.getText().replace(start, end, text);
    }

    private void insertWithCaretInside(String text) {
        insert(text);
        moveCaretBack(1);
    }

    private void moveCaretBack(int count) {
        int caret = Math.max(0, expressionView.getSelectionStart() - count);
        expressionView.setSelection(caret);
    }

    private void commit() {
        CalcEngine.Result result = evaluate(true);
        if (result == null) return;
        currentResult = result.value;
        prefs.setLastXExact(prefs.ansExact().toPlainString());
        prefs.setAns(currentResult);
        prefs.setAnsExact(result.exact == null
                ? java.math.BigDecimal.valueOf(result.value).toPlainString()
                : result.exact.toPlainString());
        prefs.addHistory(expressionView.getText().toString(), currentResult, currentDisplay);
        prefs.setExpression(expressionView.getText().toString());
        feedback();
        toast("结果已保存到历史");
    }

    private CalcEngine.Result evaluate(boolean showError) {
        String expression = expressionView.getText().toString().trim();
        if (expression.isEmpty()) {
            validResult = false;
            currentError = "";
            metaView.setText("输入算式以开始");
            statusView.setText("");
            resultView.setText("0");
            return null;
        }
        try {
            Map<String, Double> vars = prefs.variables();
            CalcEngine.Result result = CalcEngine.evaluate(expression, new CalcEngine.Context(
                    prefs.angle(), prefs.base(), vars, prefs.ans(), prefs.exactVariables()));
            currentResult = result.value;
            validResult = true;
            currentError = "";
            String baseDisplay = CalcEngine.integerInBase(result, prefs.base());
            currentDisplay = baseDisplay == null
                    ? CalcEngine.displayMode(result, prefs.resultMode(), prefs.precision(), prefs.notation(), prefs.grouped(), false)
                    : baseDisplay;
            if (baseDisplay == null && prefs.showRepeatingDecimals() && "DECIMAL".equals(prefs.resultMode())) {
                String repeating = CalcEngine.repeatingDecimal(result);
                if (repeating != null) currentDisplay = repeating;
            }
            resultView.setText(currentDisplay);
            statusView.setText("已计算");
            CalcEngine.Fraction fraction = CalcEngine.closestFraction(result.value);
            metaView.setText(fraction != null && fraction.denominator <= 100000
                    ? "分数形式 " + fraction.numerator + "/" + fraction.denominator
                    : "按等号保存到历史");
            return result;
        } catch (CalcEngine.CalcException error) {
            validResult = false;
            currentError = error.getMessage();
            statusView.setText("等待输入");
            metaView.setText(error.getMessage());
            if (showError) toast(error.getMessage());
            return null;
        }
    }

    private void clear() {
        suppressTextChange = true;
        expressionView.setText("");
        suppressTextChange = false;
        prefs.setExpression("");
        validResult = false;
        currentError = "";
        resultView.setText("0");
        currentDisplay = "0";
        metaView.setText("输入算式以开始");
        statusView.setText("");
    }

    private void backspace() {
        int start = expressionView.getSelectionStart();
        int end = expressionView.getSelectionEnd();
        if (start != end) expressionView.getText().delete(Math.min(start, end), Math.max(start, end));
        else if (start > 0) expressionView.getText().delete(start - 1, start);
    }

    private void toggleSecond() {
        second = !second;
        secondChip.setTextColor(second ? theme.accent : theme.muted);
        secondChip.setBackground(AppTheme.background(this, second ? Ui.withAlpha(theme.accent, 24) : theme.surfaceRaised, 20));
        renderKeypad();
    }

    private void cycleAngle() {
        String value = prefs.angle();
        prefs.setAngle("deg".equals(value) ? "rad" : "rad".equals(value) ? "grad" : "deg");
        refreshStatus();
        evaluate(false);
    }

    private void cycleBase() {
        int value = prefs.base();
        prefs.setBase(value == 10 ? 16 : value == 16 ? 2 : value == 2 ? 8 : 10);
        refreshStatus();
        evaluate(false);
    }

    private void refreshStatus() {
        if (angleChip == null) return;
        angleChip.setText(prefs.angle().toUpperCase());
        angleChip.setTextColor(theme.accent);
        angleChip.setBackground(AppTheme.background(this, Ui.withAlpha(theme.accent, 20), 20));
        int base = prefs.base();
        baseChip.setText(base == 16 ? "HEX" : base == 2 ? "BIN" : base == 8 ? "OCT" : "DEC");
        secondChip.setTextColor(second ? theme.accent : theme.muted);
    }

    private void copyResult() {
        String value = resultView.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("HiPER result", value));
        toast("已复制结果");
    }

    private void openDetail() {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("expression", expressionView.getText().toString());
        intent.putExtra("value", currentResult);
        intent.putExtra("display", currentDisplay);
        intent.putExtra("error", currentError);
        startActivity(intent);
    }

    private void openConversions() {
        Intent intent = new Intent(this, ConversionsActivity.class);
        intent.putExtra("value", currentResult);
        startActivity(intent);
    }

    private void feedback() {
        if (prefs.haptics()) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(7, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(7);
                }
            }
        }
        if (prefs.keySound()) {
            try {
                if (tone == null) tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 18);
                tone.startTone(ToneGenerator.TONE_PROP_BEEP, 28);
            } catch (RuntimeException ignored) {
            }
        }
    }

    private void openAdvanced(String target) {
        if ("matrix".equals(target)) open(MatrixActivity.class);
        else if ("complex".equals(target)) open(ComplexActivity.class);
        else if ("stats".equals(target)) open(StatisticsActivity.class);
        else if ("bitwise".equals(target)) open(BitwiseActivity.class);
        else if ("limit".equals(target)) open(LimitTableActivity.class);
    }

    private void memoryAdd(int sign) {
        java.math.BigDecimal memory = prefs.exactVariables().containsKey("M")
                ? prefs.exactVariables().get("M") : java.math.BigDecimal.ZERO;
        java.math.BigDecimal added = prefs.ansExact().multiply(java.math.BigDecimal.valueOf(sign));
        java.math.BigDecimal value = memory.add(added);
        prefs.setVariable("M", value.doubleValue(), value.toPlainString());
        toast("M = " + CalcEngine.formatDecimal(value, prefs.precision(), prefs.notation(), prefs.grouped(), false));
    }

    private void syncExternalExpression() {
        if (expressionView == null) return;
        String saved = prefs.expression();
        if (!saved.equals(expressionView.getText().toString())) {
            suppressTextChange = true;
            expressionView.setText(saved);
            expressionView.setSelection(expressionView.length());
            suppressTextChange = false;
        }
    }
}
