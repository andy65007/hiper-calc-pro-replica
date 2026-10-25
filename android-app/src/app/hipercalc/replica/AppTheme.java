package app.hipercalc.replica;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;

public final class AppTheme {
    public final String id;
    public final int background;
    public final int surface;
    public final int surfaceRaised;
    public final int text;
    public final int muted;
    public final int accent;
    public final int accentStrong;
    public final int accentInk;
    public final int signal;
    public final int danger;
    public final int border;

    private AppTheme(String id, int background, int surface, int surfaceRaised, int text,
                     int muted, int accent, int accentStrong, int accentInk,
                     int signal, int danger, int border) {
        this.id = id;
        this.background = background;
        this.surface = surface;
        this.surfaceRaised = surfaceRaised;
        this.text = text;
        this.muted = muted;
        this.accent = accent;
        this.accentStrong = accentStrong;
        this.accentInk = accentInk;
        this.signal = signal;
        this.danger = danger;
        this.border = border;
    }

    public static AppTheme get(String id) {
        switch (id == null ? "" : id) {
            case "modern-light":
                return light("modern-light", "#F8FBF6", "#EDF3EB", "#18221D", "#64736B", "#5B8F5F", "#376A3C", "#FFFFFF");
            case "classic":
                return light("classic", "#E8EEEA", "#D7DFDA", "#26312A", "#68766E", "#527D68", "#345A46", "#FFFFFF");
            case "brushed":
                return light("brushed", "#E4E6E2", "#D7DAD5", "#202521", "#666D68", "#4F715B", "#34543F", "#FFFFFF");
            case "shades":
                return new AppTheme("shades", 0xFF14100B, 0xFF272116, 0xFF312A1E, 0xFFFFF7E7,
                        0xFFB8A989, 0xFFFFD27C, 0xFFFFE3A5, 0xFF2C210B, 0xFFDE8668, 0xFFEF877D, 0x33F0CC91);
            case "deep":
                return new AppTheme("deep", 0xFF071824, 0xFF102A35, 0xFF173642, 0xFFE4F7F7,
                        0xFF8BB1B5, 0xFF82DFD8, 0xFFB4F4EE, 0xFF092323, 0xFFFFBE72, 0xFFEF877D, 0x2487E0E8);
            case "luxfer":
                return new AppTheme("luxfer", 0xFF111812, 0xFF1D2B21, 0xFF26372B, 0xFFE9FCE9,
                        0xFF91B498, 0xFF9CF0A5, 0xFFC6FFCB, 0xFF102513, 0xFFF2DC74, 0xFFEF877D, 0x24B4F2BA);
            case "amoled":
                return new AppTheme("amoled", 0xFF000000, 0xFF0B0B0B, 0xFF141414, 0xFFF1F1EE,
                        0xFFA1A19C, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF111111, 0xFFD8D8D8, 0xFFEF877D, 0x26FFFFFF);
            default:
                return new AppTheme("modern-dark", 0xFF0C1117, 0xFF17212B, 0xFF1C2833, 0xFFEEF5F3,
                        0xFF8CA09F, 0xFFB9DFB5, 0xFFD5F4CD, 0xFF142117, 0xFFF1A95F, 0xFFEF877D, 0x24BED9E0);
        }
    }

    private static AppTheme light(String id, String background, String surface, String text,
                                  String muted, String accent, String accentStrong, String accentInk) {
        return new AppTheme(id, Color.parseColor(background), Color.parseColor(surface),
                Color.parseColor(background), Color.parseColor(text), Color.parseColor(muted),
                Color.parseColor(accent), Color.parseColor(accentStrong), Color.parseColor(accentInk),
                Color.parseColor("#BD6E2D"), Color.parseColor("#B54D43"), Color.parseColor("#3323382D"));
    }

    public static void apply(Activity activity, AppTheme theme) {
        Window window = activity.getWindow();
        window.setStatusBarColor(theme.background);
        window.setNavigationBarColor(theme.background);
        View decor = window.getDecorView();
        int legacyFlags = 0;
        if (isLight(theme.background)) {
            legacyFlags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (android.os.Build.VERSION.SDK_INT >= 26) legacyFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        decor.setSystemUiVisibility(legacyFlags);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                boolean darkIcons = isLight(theme.background);
                controller.setSystemBarsAppearance(
                        darkIcons ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS : 0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
    }

    private static boolean isLight(int color) {
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance > 0.6;
    }

    public static GradientDrawable background(Context context, int color, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(Ui.dp(context, radiusDp));
        return drawable;
    }
}
