package app.hipercalc.replica;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class HelpActivity extends BaseActivity {
    private static final String[][] TOPICS = {
            {"输入表达式", "点击按键或直接在显示区输入。支持隐式乘法，例如 2π、3(4+5) 与 2sin(30)。按 Enter 或等号计算并保存到历史。"},
            {"角度与数制", "顶部 DEG/RAD/GRAD 控制三角函数。DEC/HEX/BIN/OCT 控制输入进制和整数结果显示。"},
            {"函数", "支持三角、反三角、双曲、对数、指数、幂、根、阶乘、组合、排列、GCD、LCM、最大值和最小值。"},
            {"微积分与求根", "使用字符串表达式：derivative(\"x^2\", 3)、integral(\"sin(x)\", 0, pi)、sum(\"k^2\", 1, n)、solve(\"x^2-2\", 1)。"},
            {"变量与历史", "Ans 保存上次结果。变量页可建立 A–F、M 及自定义变量。历史页可重新载入表达式或插入结果。"},
            {"单位换算", "换算页提供长度、面积、体积、质量、温度、时间、速度、压力、能量、功率、数据和角度。"},
            {"方程解题", "解题页目前支持一元一次和一元二次方程，包括判别式、实根、复根和主要步骤。"},
            {"隐私", "这个复刻不包含 Google 许可验证、广告、网络权限或远程服务；历史和变量只保存在应用私有数据目录。"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("使用帮助", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 22));
        for (String[] topic : TOPICS) {
            LinearLayout card = Ui.column(this);
            card.setPadding(Ui.dp(this, 15), Ui.dp(this, 14), Ui.dp(this, 15), Ui.dp(this, 14));
            card.setBackground(AppTheme.background(this, theme.surface, 14));
            TextView title = Ui.text(this, topic[0], 13, theme.text, true);
            TextView text = Ui.text(this, topic[1], 11, theme.muted, false);
            text.setLineSpacing(0, 1.6f);
            card.addView(title);
            card.addView(text);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = Ui.dp(this, 9);
            body.addView(card, params);
        }
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }
}
