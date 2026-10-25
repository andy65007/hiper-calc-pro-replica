package app.hipercalc.replica;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public final class PrefsStore {
    public static final class HistoryEntry {
        public String id;
        public String expression;
        public double value;
        public long timestamp;
        public String display;

        HistoryEntry(String id, String expression, double value, long timestamp, String display) {
            this.id = id;
            this.expression = expression;
            this.value = value;
            this.timestamp = timestamp;
            this.display = display;
        }
    }

    private static final String PREFS = "hiper_replica";
    private final SharedPreferences preferences;

    public PrefsStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String angle() {
        return preferences.getString("angle", "deg");
    }

    public void setAngle(String value) {
        preferences.edit().putString("angle", value).apply();
    }

    public int base() {
        return preferences.getInt("base", 10);
    }

    public void setBase(int value) {
        preferences.edit().putInt("base", value).apply();
    }

    public int precision() {
        return Math.max(3, Math.min(100, preferences.getInt("precision", 34)));
    }

    public void setPrecision(int value) {
        preferences.edit().putInt("precision", Math.max(3, Math.min(100, value))).apply();
    }

    public String notation() {
        return preferences.getString("notation", "auto");
    }

    public void setNotation(String value) {
        preferences.edit().putString("notation", value).apply();
    }

    public String resultMode() {
        return preferences.getString("resultMode", "DECIMAL");
    }

    public void setResultMode(String value) {
        preferences.edit().putString("resultMode", value).apply();
    }

    public String theme() {
        return preferences.getString("theme", "modern-dark");
    }

    public void setTheme(String value) {
        preferences.edit().putString("theme", value).apply();
    }

    public boolean grouped() {
        return preferences.getBoolean("grouped", false);
    }

    public void setGrouped(boolean value) {
        preferences.edit().putBoolean("grouped", value).apply();
    }

    public boolean haptics() {
        return preferences.getBoolean("haptics", true);
    }

    public void setHaptics(boolean value) {
        preferences.edit().putBoolean("haptics", value).apply();
    }

    public boolean keySound() {
        return preferences.getBoolean("keySound", false);
    }

    public void setKeySound(boolean value) {
        preferences.edit().putBoolean("keySound", value).apply();
    }

    public boolean compact() {
        return preferences.getBoolean("compact", false);
    }

    public void setCompact(boolean value) {
        preferences.edit().putBoolean("compact", value).apply();
    }

    public String keypad() {
        return preferences.getString("keypad", "basic");
    }

    public void setKeypad(String value) {
        preferences.edit().putString("keypad", value).apply();
    }

    public boolean immediateEvaluation() {
        return preferences.getBoolean("immediateEvaluation", true);
    }

    public void setImmediateEvaluation(boolean value) {
        preferences.edit().putBoolean("immediateEvaluation", value).apply();
    }

    public boolean keepScreenOn() {
        return preferences.getBoolean("keepScreenOn", false);
    }

    public void setKeepScreenOn(boolean value) {
        preferences.edit().putBoolean("keepScreenOn", value).apply();
    }

    public boolean blinkingCursor() {
        return preferences.getBoolean("blinkingCursor", true);
    }

    public void setBlinkingCursor(boolean value) {
        preferences.edit().putBoolean("blinkingCursor", value).apply();
    }

    public boolean showRepeatingDecimals() {
        return preferences.getBoolean("showRepeatingDecimals", false);
    }

    public void setShowRepeatingDecimals(boolean value) {
        preferences.edit().putBoolean("showRepeatingDecimals", value).apply();
    }

    public int historyLimit() {
        return Math.max(1, Math.min(1000, preferences.getInt("historyLimit", 100)));
    }

    public void setHistoryLimit(int value) {
        preferences.edit().putInt("historyLimit", Math.max(1, Math.min(1000, value))).apply();
    }

    public boolean removeDuplicateHistory() {
        return preferences.getBoolean("removeDuplicateHistory", true);
    }

    public void setRemoveDuplicateHistory(boolean value) {
        preferences.edit().putBoolean("removeDuplicateHistory", value).apply();
    }

    public boolean hideStatusBar() {
        return preferences.getBoolean("hideStatusBar", false);
    }

    public void setHideStatusBar(boolean value) {
        preferences.edit().putBoolean("hideStatusBar", value).apply();
    }

    public boolean hideNavigationButtons() {
        return preferences.getBoolean("hideNavigationButtons", false);
    }

    public void setHideNavigationButtons(boolean value) {
        preferences.edit().putBoolean("hideNavigationButtons", value).apply();
    }

    public String expression() {
        return preferences.getString("expression", "");
    }

    public void setExpression(String value) {
        preferences.edit().putString("expression", value).apply();
    }

    public double ans() {
        return Double.longBitsToDouble(preferences.getLong("ans", Double.doubleToRawLongBits(0)));
    }

    public void setAns(double value) {
        preferences.edit().putLong("ans", Double.doubleToRawLongBits(value)).apply();
    }

    public BigDecimal ansExact() {
        try {
            String value = preferences.getString("ansExact", "");
            return value.isEmpty() ? BigDecimal.valueOf(ans()) : new BigDecimal(value);
        } catch (NumberFormatException error) {
            return BigDecimal.valueOf(ans());
        }
    }

    public void setAnsExact(String value) {
        preferences.edit().putString("ansExact", value == null ? "" : value).apply();
    }

    public BigDecimal lastXExact() {
        try {
            String value = preferences.getString("lastXExact", "");
            return value.isEmpty() ? BigDecimal.ZERO : new BigDecimal(value);
        } catch (NumberFormatException error) {
            return BigDecimal.ZERO;
        }
    }

    public void setLastXExact(String value) {
        preferences.edit().putString("lastXExact", value == null ? "" : value).apply();
    }

    public Map<String, Double> variables() {
        Map<String, Double> result = new HashMap<>();
        String raw = preferences.getString("variables", "{}");
        try {
            JSONObject object = new JSONObject(raw);
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                result.put(key, object.optDouble(key));
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    public void setVariables(Map<String, Double> variables) {
        JSONObject object = new JSONObject();
        try {
            for (Map.Entry<String, Double> entry : variables.entrySet()) {
                object.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException ignored) {
        }
        preferences.edit().putString("variables", object.toString()).apply();
    }

    public void setVariable(String name, double value) {
        Map<String, Double> variables = variables();
        variables.put(name, value);
        setVariables(variables);
    }

    public Map<String, String> variableExpressions() {
        Map<String, String> result = new HashMap<>();
        String raw = preferences.getString("variableExpressions", "{}");
        try {
            JSONObject object = new JSONObject(raw);
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                result.put(key, object.optString(key));
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    public void setVariable(String name, double value, String expression) {
        Map<String, Double> variables = variables();
        variables.put(name, value);
        setVariables(variables);
        Map<String, String> expressions = variableExpressions();
        expressions.put(name, expression == null ? "" : expression);
        JSONObject object = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : expressions.entrySet()) object.put(entry.getKey(), entry.getValue());
        } catch (JSONException ignored) {
        }
        preferences.edit().putString("variableExpressions", object.toString()).apply();
    }

    public Map<String, BigDecimal> exactVariables() {
        Map<String, BigDecimal> result = new HashMap<>();
        for (Map.Entry<String, String> entry : variableExpressions().entrySet()) {
            try {
                result.put(entry.getKey(), new BigDecimal(entry.getValue()));
            } catch (NumberFormatException ignored) {
            }
        }
        result.put("ans", ansExact());
        result.put("lastx", lastXExact());
        return result;
    }

    public void removeVariable(String name) {
        Map<String, Double> variables = variables();
        variables.remove(name);
        setVariables(variables);
        Map<String, String> expressions = variableExpressions();
        expressions.remove(name);
        JSONObject object = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : expressions.entrySet()) object.put(entry.getKey(), entry.getValue());
        } catch (JSONException ignored) {
        }
        preferences.edit().putString("variableExpressions", object.toString()).apply();
    }

    public List<HistoryEntry> history() {
        List<HistoryEntry> history = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString("history", "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                history.add(new HistoryEntry(
                        item.optString("id"),
                        item.optString("expression"),
                        item.optDouble("value"),
                        item.optLong("timestamp"),
                        item.optString("display")
                ));
            }
        } catch (JSONException ignored) {
        }
        return history;
    }

    public void setHistory(List<HistoryEntry> history) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < Math.min(historyLimit(), history.size()); i++) {
            HistoryEntry item = history.get(i);
            try {
                JSONObject object = new JSONObject();
                object.put("id", item.id);
                object.put("expression", item.expression);
                object.put("value", item.value);
                object.put("timestamp", item.timestamp);
                object.put("display", item.display == null ? "" : item.display);
                array.put(object);
            } catch (JSONException ignored) {
            }
        }
        preferences.edit().putString("history", array.toString()).apply();
    }

    public void addHistory(String expression, double value, String display) {
        List<HistoryEntry> history = history();
        if (removeDuplicateHistory() && !history.isEmpty()) {
            HistoryEntry first = history.get(0);
            if (first.expression.equals(expression) && first.value == value) return;
        }
        history.add(0, new HistoryEntry(
                Long.toString(System.currentTimeMillis()) + "-" + Integer.toHexString(expression.hashCode()),
                expression,
                value,
                System.currentTimeMillis(),
                display
        ));
        setHistory(history);
    }

    public void removeHistory(String id) {
        List<HistoryEntry> history = history();
        for (int i = history.size() - 1; i >= 0; i--) {
            if (history.get(i).id.equals(id)) history.remove(i);
        }
        setHistory(history);
    }

    public void clearHistory() {
        preferences.edit().remove("history").apply();
    }

    public String exportJson() {
        JSONObject root = new JSONObject();
        try {
            root.put("format", "hiper-calc-pro-android-replica");
            root.put("version", 1);
            JSONObject settings = new JSONObject();
            settings.put("angle", angle());
            settings.put("base", base());
            settings.put("precision", precision());
            settings.put("notation", notation());
            settings.put("resultMode", resultMode());
            settings.put("theme", theme());
            settings.put("grouped", grouped());
            settings.put("haptics", haptics());
            settings.put("keySound", keySound());
            settings.put("compact", compact());
            settings.put("keypad", keypad());
            settings.put("immediateEvaluation", immediateEvaluation());
            settings.put("keepScreenOn", keepScreenOn());
            settings.put("blinkingCursor", blinkingCursor());
            settings.put("historyLimit", historyLimit());
            settings.put("removeDuplicateHistory", removeDuplicateHistory());
            settings.put("showRepeatingDecimals", showRepeatingDecimals());
            settings.put("hideStatusBar", hideStatusBar());
            settings.put("hideNavigationButtons", hideNavigationButtons());
            root.put("settings", settings);
            root.put("expression", expression());
            root.put("ans", ansExact().toPlainString());
            root.put("lastx", lastXExact().toPlainString());
            JSONObject vars = new JSONObject();
            for (Map.Entry<String, String> entry : variableExpressions().entrySet()) vars.put(entry.getKey(), entry.getValue());
            root.put("variables", vars);
            JSONArray items = new JSONArray();
            for (HistoryEntry entry : history()) {
                JSONObject item = new JSONObject();
                item.put("expression", entry.expression);
                item.put("value", entry.value);
                item.put("display", entry.display);
                item.put("timestamp", entry.timestamp);
                items.put(item);
            }
            root.put("history", items);
        } catch (JSONException ignored) {
        }
        try {
            return root.toString(2);
        } catch (JSONException error) {
            return root.toString();
        }
    }

    public void importJson(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        if (!"hiper-calc-pro-android-replica".equals(root.optString("format"))) {
            throw new JSONException("不是 HiPER 原生复刻备份");
        }
        JSONObject settings = root.optJSONObject("settings");
        if (settings != null) {
            setAngle(settings.optString("angle", "deg"));
            setBase(settings.optInt("base", 10));
            setPrecision(settings.optInt("precision", 34));
            setNotation(settings.optString("notation", "auto"));
            setResultMode(settings.optString("resultMode", "DECIMAL"));
            setTheme(settings.optString("theme", "modern-dark"));
            setGrouped(settings.optBoolean("grouped", false));
            setHaptics(settings.optBoolean("haptics", true));
            setKeySound(settings.optBoolean("keySound", false));
            setCompact(settings.optBoolean("compact", false));
            setKeypad(settings.optString("keypad", "basic"));
            setImmediateEvaluation(settings.optBoolean("immediateEvaluation", true));
            setKeepScreenOn(settings.optBoolean("keepScreenOn", false));
            setBlinkingCursor(settings.optBoolean("blinkingCursor", true));
            setHistoryLimit(settings.optInt("historyLimit", 100));
            setRemoveDuplicateHistory(settings.optBoolean("removeDuplicateHistory", true));
            setShowRepeatingDecimals(settings.optBoolean("showRepeatingDecimals", false));
            setHideStatusBar(settings.optBoolean("hideStatusBar", false));
            setHideNavigationButtons(settings.optBoolean("hideNavigationButtons", false));
        }
        setExpression(root.optString("expression", ""));
        setAnsExact(root.optString("ans", "0"));
        setLastXExact(root.optString("lastx", "0"));
        Map<String, Double> values = new HashMap<>();
        Map<String, String> expressions = new HashMap<>();
        JSONObject variables = root.optJSONObject("variables");
        if (variables != null) {
            Iterator<String> keys = variables.keys();
            while (keys.hasNext()) {
                String name = keys.next();
                String expression = variables.optString(name);
                expressions.put(name, expression);
                try {
                    values.put(name, new BigDecimal(expression).doubleValue());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        setVariables(values);
        JSONObject expressionObject = new JSONObject();
        for (Map.Entry<String, String> entry : expressions.entrySet()) expressionObject.put(entry.getKey(), entry.getValue());
        preferences.edit().putString("variableExpressions", expressionObject.toString()).apply();
        List<HistoryEntry> imported = new ArrayList<>();
        JSONArray array = root.optJSONArray("history");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;
                imported.add(new HistoryEntry(
                        Long.toString(System.currentTimeMillis()) + "-" + i,
                        item.optString("expression"),
                        item.optDouble("value"),
                        item.optLong("timestamp", System.currentTimeMillis()),
                        item.optString("display")
                ));
            }
        }
        setHistory(imported);
    }
}
