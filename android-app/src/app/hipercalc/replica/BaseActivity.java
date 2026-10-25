package app.hipercalc.replica;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BaseActivity extends Activity {
    protected PrefsStore prefs;
    protected AppTheme theme;
    protected FrameLayout page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new PrefsStore(this);
        theme = AppTheme.get(prefs.theme());
        AppTheme.apply(this, theme);
        page = new FrameLayout(this);
        page.setBackgroundColor(theme.background);
        setContentView(page);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefs.keepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                int types = 0;
                if (prefs.hideStatusBar()) types |= WindowInsets.Type.statusBars();
                if (prefs.hideNavigationButtons()) types |= WindowInsets.Type.navigationBars();
                if (types != 0) controller.hide(types);
                else controller.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (prefs.hideStatusBar()) flags |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            else flags &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (prefs.hideNavigationButtons()) flags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            else flags &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    protected LinearLayout header(String title, boolean back) {
        LinearLayout bar = Ui.row(this, android.view.Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 10), Ui.dp(this, 8), Ui.dp(this, 10), Ui.dp(this, 8));
        if (back) {
            TextView close = Ui.text(this, "‹", 32, theme.text, false);
            close.setGravity(android.view.Gravity.CENTER);
            close.setOnClickListener(v -> finish());
            bar.addView(close, new LinearLayout.LayoutParams(Ui.dp(this, 44), Ui.dp(this, 48)));
        }
        TextView titleView = Ui.text(this, title, 18, theme.text, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1);
        titleParams.leftMargin = Ui.dp(this, back ? 6 : 12);
        bar.addView(titleView, titleParams);
        return bar;
    }

    protected LinearLayout contentColumn() {
        LinearLayout column = Ui.column(this);
        page.addView(column, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        return column;
    }

    protected void open(Class<?> activity) {
        startActivity(new Intent(this, activity));
    }

    protected String format(double value) {
        return CalcEngine.formatNumber(value, prefs.precision(), prefs.notation(), prefs.grouped(), false);
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void setPage(View view) {
        page.removeAllViews();
        page.addView(view, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    }
}
