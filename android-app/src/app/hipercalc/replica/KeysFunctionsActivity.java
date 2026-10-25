package app.hipercalc.replica;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class KeysFunctionsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("按键与函数", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 20));
        String[][] rows = {
                {"sin / cos / tan", "sin(x)、cos(x)、tan(x)"},
                {"2ND + sin / cos / tan", "asin(x)、acos(x)、atan(x)"},
                {"ln / log", "ln(x)、log(x)；log(x,b) 可指定底数"},
                {"√ / ∛", "sqrt(x)、cbrt(x)"},
                {"x² / xʸ", "^2、^（幂运算）"},
                {"n! / nCr / nPr", "阶乘、组合、排列"},
                {"π / e", "圆周率与自然常数"},
                {"d/dx", "derivative(\"表达式\", 点)"},
                {"∫", "integral(\"表达式\", 下限, 上限)"},
                {"Σ", "sum(\"表达式\", 起始, 结束)"},
                {"solve", "solve(\"表达式\", 初始猜测)"},
                {"Ans / A–F / M", "上一次结果与保存变量"}
        };
        for (String[] row : rows) {
            LinearLayout card = Ui.column(this);
            card.setPadding(Ui.dp(this, 14), Ui.dp(this, 11), Ui.dp(this, 14), Ui.dp(this, 11));
            card.setBackground(AppTheme.background(this, theme.surface, 12));
            card.addView(Ui.text(this, row[0], 12, theme.text, true));
            card.addView(Ui.text(this, row[1], 10, theme.muted, false));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = Ui.dp(this, 7);
            body.addView(card, params);
        }
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }
}
