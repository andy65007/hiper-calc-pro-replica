package app.hipercalc.replica;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class AboutActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("关于此复刻", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 18), Ui.dp(this, 12), Ui.dp(this, 18), Ui.dp(this, 22));
        body.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView logo = Ui.text(this, "╱", 42, theme.accent, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(AppTheme.background(this, theme.surfaceRaised, 22));
        body.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 78), Ui.dp(this, 78)));
        TextView title = Ui.text(this, "HiPER Calc Pro", 23, theme.text, true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
        titleParams.topMargin = Ui.dp(this, 12);
        body.addView(title, titleParams);
        TextView version = Ui.text(this, "12.1.4 · Native Replica", 10, theme.muted, false);
        version.setGravity(Gravity.CENTER);
        body.addView(version, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 30)));

        String text = "这是根据当前目录 APK 的静态结构重新编写的独立 Android 应用。\n\n"
                + "原始应用使用 PairIP 包装与混淆的任意精度代数核心。本复刻没有加载原 APK、没有运行连接的手机应用，也不包含 Google 许可验证、广告或签名校验。\n\n"
                + "功能覆盖科学表达式、单位换算、变量、历史、常量、主题、设置、数值详情和方程步骤。高阶方程、矩阵、原应用专有 OCR 模型与任意精度代数尚未做到逐字节等价。\n\n"
                + "包名与签名均已独立，不会覆盖或修改原始 HiPER Calc Pro。";
        TextView content = Ui.text(this, text, 12, theme.muted, false);
        content.setLineSpacing(0, 1.7f);
        LinearLayout card = Ui.column(this);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        card.setBackground(AppTheme.background(this, theme.surface, 16));
        card.addView(content);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = Ui.dp(this, 18);
        body.addView(card, cardParams);
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }
}
