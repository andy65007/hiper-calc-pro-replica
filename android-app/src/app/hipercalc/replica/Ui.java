package app.hipercalc.replica;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import java.util.List;

public final class Ui {
    private Ui() {
    }

    public static int dp(Context context, float value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics()));
    }

    public static TextView text(Context context, String value, float size, int color, boolean bold) {
        TextView text = new TextView(context);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setTypeface(Typeface.create(Typeface.SANS_SERIF, bold ? Typeface.BOLD : Typeface.NORMAL));
        text.setIncludeFontPadding(false);
        return text;
    }

    public static TextView math(Context context, String value, float size, int color) {
        TextView text = text(context, value, size, color, false);
        text.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        return text;
    }

    public static Button button(Context context, String label, int textColor, int background,
                                float radius, float size, boolean bold) {
        Button button = new Button(context);
        button.setText(label);
        button.setTextSize(size);
        button.setTextColor(textColor);
        button.setAllCaps(false);
        button.setTypeface(Typeface.create(Typeface.SANS_SERIF, bold ? Typeface.BOLD : Typeface.NORMAL));
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(0);
        button.setMinWidth(0);
        button.setPadding(dp(context, 4), dp(context, 4), dp(context, 4), dp(context, 4));
        GradientDrawable normal = AppTheme.background(context, background, radius);
        RippleDrawable ripple = new RippleDrawable(ColorStateList.valueOf(withAlpha(textColor, 35)), normal, null);
        button.setBackground(ripple);
        button.setStateListAnimator(null);
        return button;
    }

    public static LinearLayout row(Context context, int gravity) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(gravity);
        return layout;
    }

    public static LinearLayout column(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    public static LinearLayout.LayoutParams lp(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    public static LinearLayout.LayoutParams weighted(float weight) {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight);
    }

    public static void margin(View view, int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams raw = view.getLayoutParams();
        if (raw instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) raw;
            params.setMargins(dp(view.getContext(), left), dp(view.getContext(), top),
                    dp(view.getContext(), right), dp(view.getContext(), bottom));
            view.setLayoutParams(params);
        }
    }

    public static void padding(View view, int left, int top, int right, int bottom) {
        view.setPadding(dp(view.getContext(), left), dp(view.getContext(), top),
                dp(view.getContext(), right), dp(view.getContext(), bottom));
    }

    public static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static ArrayAdapter<String> adapter(Context context, List<String> values, int textColor) {
        return new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, values) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(textColor);
                view.setTextSize(13);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.parseColor("#202020"));
                view.setTextSize(14);
                view.setPadding(dp(context, 16), dp(context, 10), dp(context, 16), dp(context, 10));
                view.setBackgroundColor(Color.WHITE);
                return view;
            }
        };
    }
}
