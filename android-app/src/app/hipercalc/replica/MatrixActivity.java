package app.hipercalc.replica;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;

public final class MatrixActivity extends BaseActivity {
    private EditText matrixA;
    private EditText matrixB;
    private EditText subRow;
    private EditText subColumn;
    private Spinner operation;
    private TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("矩阵与向量", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 20));

        TextView intro = Ui.text(this, "矩阵格式：行内用逗号分隔，行间用分号分隔。例如 1,2;3,4。向量可写成 1,2,3。", 11, theme.muted, false);
        intro.setLineSpacing(0, 1.5f);
        body.addView(intro, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 62)));

        matrixA = matrixInput("矩阵 A，例如 1,2;3,4");
        matrixB = matrixInput("矩阵 B，例如 5,6;7,8");
        body.addView(fieldLabel("矩阵 A / 向量 a"));
        body.addView(matrixA, matrixParams());
        body.addView(fieldLabel("矩阵 B / 向量 b"));
        body.addView(matrixB, matrixParams());

        operation = new Spinner(this);
        operation.setAdapter(Ui.adapter(this, Arrays.asList(
                "A + B", "A − B", "A × B", "Hadamard A∘B", "Kronecker A⊗B",
                "det(A)", "inverse(A)", "transpose(A)", "rank(A)", "trace(A)",
                "row echelon(A)", "RREF(A)", "minors(A)", "cofactors(A)", "adjugate(A)",
                "characteristic polynomial(A)", "eigenvalues(A)", "eigenvectors(A, symmetric)",
                "diagonalize(A, symmetric)", "dot(a,b)", "cross(a,b)", "distance(a,b)",
                "submatrix(A,row,col)", "identity(size)"
        ), theme.text));
        operation.setBackground(AppTheme.background(this, theme.surface, 11));
        body.addView(fieldLabel("运算"));
        body.addView(operation, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 48)));

        LinearLayout indices = Ui.row(this, Gravity.CENTER_VERTICAL);
        subRow = numberInput("行索引 0");
        subColumn = numberInput("列索引 0");
        indices.addView(subRow, new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 1));
        LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 1);
        columnParams.leftMargin = Ui.dp(this, 8);
        indices.addView(subColumn, columnParams);
        LinearLayout.LayoutParams indicesParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54));
        indicesParams.topMargin = Ui.dp(this, 8);
        body.addView(indices, indicesParams);

        Button calculate = Ui.button(this, "计算", theme.accentInk, theme.accent, 11, 12, true);
        calculate.setOnClickListener(v -> calculate());
        LinearLayout.LayoutParams calculateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
        calculateParams.topMargin = Ui.dp(this, 10);
        body.addView(calculate, calculateParams);

        output = new TextView(this);
        output.setText("结果将在这里显示。");
        output.setTextColor(theme.accentStrong);
        output.setTextSize(15);
        output.setTypeface(Typeface.MONOSPACE);
        output.setTextIsSelectable(true);
        output.setBackground(AppTheme.background(this, theme.surface, 14));
        Ui.padding(output, 14, 14, 14, 14);
        LinearLayout.LayoutParams outputParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        outputParams.topMargin = Ui.dp(this, 12);
        body.addView(output, outputParams);
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private EditText matrixInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(theme.muted);
        input.setTextColor(theme.text);
        input.setTextSize(15);
        input.setTypeface(Typeface.MONOSPACE);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(2);
        input.setBackground(AppTheme.background(this, theme.surface, 11));
        Ui.padding(input, 12, 10, 12, 10);
        return input;
    }

    private EditText numberInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setText("0");
        input.setHintTextColor(theme.muted);
        input.setTextColor(theme.text);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setBackground(AppTheme.background(this, theme.surface, 10));
        Ui.padding(input, 10, 8, 10, 8);
        return input;
    }

    private TextView fieldLabel(String text) {
        TextView label = Ui.text(this, text, 10, theme.muted, true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 30));
        params.topMargin = Ui.dp(this, 7);
        label.setLayoutParams(params);
        return label;
    }

    private LinearLayout.LayoutParams matrixParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 82));
    }

    private void calculate() {
        try {
            int selected = operation.getSelectedItemPosition();
            String a = matrixA.getText().toString();
            String b = matrixB.getText().toString();
            String result;
            if (selected == 0) result = matrix(MatrixMath.add(MatrixMath.parse(a), MatrixMath.parse(b), 1));
            else if (selected == 1) result = matrix(MatrixMath.add(MatrixMath.parse(a), MatrixMath.parse(b), -1));
            else if (selected == 2) result = matrix(MatrixMath.multiply(MatrixMath.parse(a), MatrixMath.parse(b)));
            else if (selected == 3) result = matrix(MatrixMath.hadamard(MatrixMath.parse(a), MatrixMath.parse(b)));
            else if (selected == 4) result = matrix(MatrixMath.kronecker(MatrixMath.parse(a), MatrixMath.parse(b)));
            else if (selected == 5) result = number(MatrixMath.determinant(MatrixMath.parse(a)));
            else if (selected == 6) result = matrix(MatrixMath.inverse(MatrixMath.parse(a)));
            else if (selected == 7) result = matrix(MatrixMath.transpose(MatrixMath.parse(a)));
            else if (selected == 8) result = Integer.toString(MatrixMath.rank(MatrixMath.parse(a)));
            else if (selected == 9) result = number(MatrixMath.trace(MatrixMath.parse(a)));
            else if (selected == 10) result = matrix(MatrixMath.rowEchelon(MatrixMath.parse(a)));
            else if (selected == 11) result = matrix(MatrixMath.rowReduced(MatrixMath.parse(a)));
            else if (selected == 12) result = matrix(MatrixMath.minors(MatrixMath.parse(a)));
            else if (selected == 13) result = matrix(MatrixMath.cofactors(MatrixMath.parse(a)));
            else if (selected == 14) result = matrix(MatrixMath.adjugate(MatrixMath.parse(a)));
            else if (selected == 15) result = MatrixMath.polynomialString(MatrixMath.characteristicPolynomial(MatrixMath.parse(a))) + " = 0";
            else if (selected == 16) {
                MatrixMath.EigenValue[] eigen = MatrixMath.eigenvalues(MatrixMath.parse(a));
                StringBuilder values = new StringBuilder();
                for (MatrixMath.EigenValue value : eigen) {
                    if (values.length() > 0) values.append(", ");
                    values.append(number(value.real));
                    if (Math.abs(value.imaginary) > 1e-8) {
                        values.append(value.imaginary >= 0 ? " + " : " − ")
                                .append(number(Math.abs(value.imaginary))).append("i");
                    }
                }
                result = "λ = [" + values + "]";
            } else if (selected == 17) result = matrix(MatrixMath.eigenSymmetric(MatrixMath.parse(a)).vectors);
            else if (selected == 18) result = matrix(MatrixMath.eigenSymmetric(MatrixMath.parse(a)).diagonal());
            else if (selected == 19) result = number(MatrixMath.dot(vector(a), vector(b)));
            else if (selected == 20) result = "[" + join(MatrixMath.cross(vector(a), vector(b))) + "]";
            else if (selected == 21) result = number(MatrixMath.distance(vector(a), vector(b)));
            else if (selected == 22) {
                int row = Integer.parseInt(subRow.getText().toString());
                int column = Integer.parseInt(subColumn.getText().toString());
                result = matrix(MatrixMath.submatrix(MatrixMath.parse(a), row, column));
            } else {
                int size = Integer.parseInt(a.trim().isEmpty() ? "3" : a.trim());
                result = matrix(MatrixMath.identity(size));
            }
            output.setText(result);
        } catch (CalcEngine.CalcException | RuntimeException error) {
            output.setText("无法计算：" + (error.getMessage() == null ? "输入格式错误" : error.getMessage()));
        }
    }

    private String matrix(double[][] matrix) {
        return MatrixMath.format(matrix, prefs.precision());
    }

    private String number(double value) {
        return CalcEngine.formatNumber(value, prefs.precision(), prefs.notation(), prefs.grouped(), false);
    }

    private double[] vector(String input) throws CalcEngine.CalcException {
        double[][] parsed = MatrixMath.parse(input);
        if (parsed.length != 1) throw new CalcEngine.CalcException("向量请输入为一行，例如 1,2,3");
        return parsed[0];
    }

    private String join(double[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) builder.append(", ");
            builder.append(number(values[i]));
        }
        return builder.toString();
    }
}
