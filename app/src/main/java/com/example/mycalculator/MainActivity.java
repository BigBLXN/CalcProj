package com.example.mycalculator;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText tvDisplay;
    private TextView tvMode, tvPreview;
    private Button btnShift, btnAlpha;
    private boolean isShift = false;
    private boolean isAlpha = false;
    private String lastResult = "0";
    private double memoryValue = 0;
    private List<String> history = new ArrayList<>();
    private int historyIndex = 0;

    private enum AngleMode { DEG, RAD, GRA }
    private AngleMode currentAngleMode = AngleMode.DEG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvDisplay = findViewById(R.id.tvDisplay);
        tvMode = findViewById(R.id.tvMode);
        tvPreview = findViewById(R.id.tvPreview);
        btnShift = findViewById(R.id.btnShift);
        btnAlpha = findViewById(R.id.btnAlpha);

        tvDisplay.setShowSoftInputOnFocus(false);
        tvDisplay.requestFocus();

        setupClickListeners();
    }

    private void setupClickListeners() {
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot, R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply,
                R.id.btnDivide, R.id.btnEquals, R.id.btnAC, R.id.btnDelete,
                R.id.btnSin, R.id.btnCos, R.id.btnTan, R.id.btnLog, R.id.btnLn,
                R.id.btnSqrt, R.id.btnSquare, R.id.btnXY,
                R.id.btnOpenBracket, R.id.btnCloseBracket,
                R.id.btnPlusMinus, R.id.btnFraction, R.id.btnShift, R.id.btnAlpha,
                R.id.btnUp, R.id.btnDown, R.id.btnLeft, R.id.btnRight, R.id.btnMode,
                R.id.btnOn, R.id.btnAns, R.id.btnExp, R.id.btnHyp, R.id.btnDegree,
                R.id.btnMPlus, R.id.btnRcl, R.id.btnEng
        };

        for (int id : buttonIds) {
            View b = findViewById(id);
            if (b != null) b.setOnClickListener(this::onButtonClick);
        }
    }

    private void onButtonClick(View view) {
        int id = view.getId();

        if (id == R.id.btnShift) {
            isShift = !isShift;
            isAlpha = false;
            updateModeIndicator();
            return;
        } else if (id == R.id.btnAlpha) {
            isAlpha = !isAlpha;
            isShift = false;
            updateModeIndicator();
            return;
        } else if (id == R.id.btnMode) {
            toggleDRG();
            updatePreview();
            return;
        } else if (id == R.id.btnOn) {
            tvDisplay.setText("0");
            tvDisplay.setSelection(1);
            tvPreview.setText("");
            historyIndex = history.size();
            return;
        }

        if (id == R.id.btn0) appendToExpression("0");
        else if (id == R.id.btn1) appendToExpression("1");
        else if (id == R.id.btn2) appendToExpression("2");
        else if (id == R.id.btn3) appendToExpression("3");
        else if (id == R.id.btn4) appendToExpression("4");
        else if (id == R.id.btn5) appendToExpression("5");
        else if (id == R.id.btn6) appendToExpression("6");
        else if (id == R.id.btn7) appendToExpression("7");
        else if (id == R.id.btn8) appendToExpression("8");
        else if (id == R.id.btn9) appendToExpression("9");
        else if (id == R.id.btnDot) appendToExpression(".");
        else if (id == R.id.btnPlus) appendToExpression("+");
        else if (id == R.id.btnMinus) appendToExpression("-");
        else if (id == R.id.btnMultiply) appendToExpression("×");
        else if (id == R.id.btnDivide) appendToExpression("÷");
        else if (id == R.id.btnOpenBracket) appendToExpression("(");
        else if (id == R.id.btnCloseBracket) appendToExpression(")");
        else if (id == R.id.btnAns) appendToExpression(lastResult);

        else if (id == R.id.btnExp) {
            if (isShift) appendToExpression("π");
            else appendToExpression("×10^");
        }
        else if (id == R.id.btnEquals) {
            if (isShift) appendToExpression("%");
            else calculateResult();
        }

        else if (id == R.id.btnSin) {
            if (isShift) appendToExpression("asin(");
            else if (isAlpha) appendToExpression("sinh(");
            else appendToExpression("sin(");
        } else if (id == R.id.btnCos) {
            if (isShift) appendToExpression("acos(");
            else if (isAlpha) appendToExpression("cosh(");
            else appendToExpression("cos(");
        } else if (id == R.id.btnTan) {
            if (isShift) appendToExpression("atan(");
            else if (isAlpha) appendToExpression("tanh(");
            else appendToExpression("tan(");
        } else if (id == R.id.btnLog) {
            if (isShift) appendToExpression("10^(");
            else appendToExpression("log(");
        } else if (id == R.id.btnLn) {
            if (isShift) appendToExpression("exp(");
            else if (isAlpha) appendToExpression("e");
            else appendToExpression("ln(");
        } else if (id == R.id.btnSqrt) {
            if (isShift) appendToExpression("root(");
            else appendToExpression("√(");
        }
        else if (id == R.id.btnSquare) appendToExpression("^2");
        else if (id == R.id.btnXY) {
            if (isShift) appendToExpression("!");
            else appendToExpression("^");
        }

        else if (id == R.id.btnAC) {
            tvDisplay.setText("0");
            tvDisplay.setSelection(1);
            tvPreview.setText("");
            historyIndex = history.size();
        } else if (id == R.id.btnDelete) {
            deleteCharacter();
        } else if (id == R.id.btnLeft) {
            moveCursor(-1);
        } else if (id == R.id.btnRight) {
            moveCursor(1);
        } else if (id == R.id.btnUp) {
            navigateHistory(-1);
        } else if (id == R.id.btnDown) {
            navigateHistory(1);
        } else if (id == R.id.btnPlusMinus) {
            toggleSign();
        } else if (id == R.id.btnFraction) {
            if (isShift) appendToExpression("nPr");
            else appendToExpression("nCr");
        } else if (id == R.id.btnHyp) {
            isAlpha = !isAlpha;
            isShift = false;
            updateModeIndicator();
        } else if (id == R.id.btnDegree) {
            toggleDRG();
        } else if (id == R.id.btnMPlus) {
            addToMemory();
        } else if (id == R.id.btnRcl) {
            recallMemory();
        } else if (id == R.id.btnEng) {
            formatEngineering();
        }

        if (id != R.id.btnShift && id != R.id.btnAlpha && id != R.id.btnLeft && id != R.id.btnRight && id != R.id.btnUp && id != R.id.btnDown && id != R.id.btnMode) {
            isShift = false;
            isAlpha = false;
            updateModeIndicator();
        }

        // Update real-time preview
        if (id != R.id.btnEquals) {
            updatePreview();
        }
    }

    private void toggleDRG() {
        if (currentAngleMode == AngleMode.DEG) currentAngleMode = AngleMode.RAD;
        else if (currentAngleMode == AngleMode.RAD) currentAngleMode = AngleMode.GRA;
        else currentAngleMode = AngleMode.DEG;
        tvMode.setText(currentAngleMode.name());
    }

    private void appendToExpression(String str) {
        String currentText = tvDisplay.getText().toString();
        int cursorPosition = tvDisplay.getSelectionStart();

        int logicalPosition = 0;
        for (int i = 0; i < cursorPosition; i++) {
            if (currentText.charAt(i) != ',') logicalPosition++;
        }

        if (currentText.equals("0") && !str.equals(".") && !"+-×÷^!%".contains(str)) {
            currentText = "";
            logicalPosition = 0;
        }

        String rawText = currentText.replace(",", "");
        String updatedRawText = rawText.substring(0, logicalPosition) + str + rawText.substring(logicalPosition);
        String formattedText = formatInput(updatedRawText);

        tvDisplay.setText(formattedText);
        setCursorAtLogicalPosition(logicalPosition + str.length());
    }

    private void setCursorAtLogicalPosition(int logicalTarget) {
        String text = tvDisplay.getText().toString();
        int actualPos = 0;
        int logicalCount = 0;
        while (actualPos < text.length() && logicalCount < logicalTarget) {
            if (text.charAt(actualPos) != ',') logicalCount++;
            actualPos++;
        }
        tvDisplay.setSelection(actualPos);
    }

    private String formatInput(String input) {
        // Find numbers and format them with thousand separators without losing precision
        Pattern pattern = Pattern.compile("(\\d+)(\\.\\d*)?");
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String integerPart = matcher.group(1);
            String decimalPart = matcher.group(2) != null ? matcher.group(2) : "";

            // Manually add commas to avoid Double.parseDouble overflow/precision issues
            StringBuilder formattedInt = new StringBuilder();
            int len = integerPart.length();
            for (int i = 0; i < len; i++) {
                if (i > 0 && (len - i) % 3 == 0) {
                    formattedInt.append(",");
                }
                formattedInt.append(integerPart.charAt(i));
            }
            matcher.appendReplacement(sb, formattedInt.toString() + decimalPart);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private void deleteCharacter() {
        int cursorPosition = tvDisplay.getSelectionStart();
        if (cursorPosition > 0) {
            String currentText = tvDisplay.getText().toString();

            int logicalPosition = 0;
            for (int i = 0; i < cursorPosition; i++) {
                if (currentText.charAt(i) != ',') logicalPosition++;
            }

            String rawText = currentText.replace(",", "");
            if (logicalPosition > 0) {
                String updatedRawText = rawText.substring(0, logicalPosition - 1) + rawText.substring(logicalPosition);
                String formattedText = formatInput(updatedRawText);

                if (formattedText.isEmpty()) {
                    tvDisplay.setText("0");
                    tvDisplay.setSelection(1);
                } else {
                    tvDisplay.setText(formattedText);
                    setCursorAtLogicalPosition(logicalPosition - 1);
                }
            }
        }
    }

    private void moveCursor(int direction) {
        int cursorPosition = tvDisplay.getSelectionStart();
        int newPosition = cursorPosition + direction;
        if (newPosition >= 0 && newPosition <= tvDisplay.length()) {
            tvDisplay.setSelection(newPosition);
        }
    }


    private void toggleSign() {
        String currentText = tvDisplay.getText().toString();
        if (currentText.equals("0") || currentText.isEmpty()) return;

        if (currentText.startsWith("-")) {
            tvDisplay.setText(currentText.substring(1));
        } else if (currentText.matches("[\\d,]+(\\.\\d+)?")) {
            tvDisplay.setText("-" + currentText);
        } else {
            tvDisplay.setText("-(" + currentText + ")");
        }
        tvDisplay.setSelection(tvDisplay.length());
        updatePreview();
    }

    private double evaluateCurrentExpression() {
        String processed = prepareExpression(tvDisplay.getText().toString());
        Expression e = new ExpressionBuilder(processed)
                .functions(getMathFunctions())
                .build();
        return e.evaluate();
    }
    private void updateModeIndicator() {
        int activeShift = Color.parseColor("#FFD60A");
        int activeAlpha = Color.parseColor("#FF453A");
        int inactive = Color.parseColor("#5C6478");

        btnShift.setBackgroundTintList(ColorStateList.valueOf(isShift ? activeShift : inactive));
        btnShift.setTextColor(isShift ? Color.BLACK : activeShift);

        btnAlpha.setBackgroundTintList(ColorStateList.valueOf(isAlpha ? activeAlpha : inactive));
        btnAlpha.setTextColor(isAlpha ? Color.BLACK : activeAlpha);
    }

    private void updatePreview() {
        String expressionStr = tvDisplay.getText().toString();
        if (expressionStr.equals("0") || expressionStr.isEmpty()) {
            tvPreview.setText("");
            return;
        }

        // Try to evaluate the current expression for preview
        try {
            // Check if expression is just a number
            if (expressionStr.replace(",", "").matches("-?\\d+(\\.\\d*)?")) {
                tvPreview.setText("");
                return;
            }

            String processed = prepareExpression(expressionStr);
            Expression e = new ExpressionBuilder(processed)
                    .functions(getMathFunctions())
                    .build();

            if (e.validate().isValid()) {
                double result = e.evaluate();
                DecimalFormat df = new DecimalFormat("#,###.########");
                tvPreview.setText("= " + df.format(result));
            } else {
                tvPreview.setText("");
            }
        } catch (Exception e) {
            tvPreview.setText(""); // Silently ignore preview errors
        }
    }

    private String prepareExpression(String expression) {
        // Auto-close brackets
        int openBrackets = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') openBrackets++;
            else if (c == ')') openBrackets--;
        }
        while (openBrackets > 0) {
            expression += ")";
            openBrackets--;
        }

        // 1. Remove thousands-separator commas.
        // These are commas followed by exactly 3 digits that are then NOT followed by another digit.
        String processed = expression.replaceAll(",(?=\\d{3}(?!\\d))", "");

        // 2. Transform infix-style nCr/nPr to functional form.
        // Handles "12nCr5", "12nCr(5)", etc.
        processed = processed.replaceAll("(\\d+(?:\\.\\d*)?)nCr\\(?(\\d+(?:\\.\\d*)?)\\)?", "nCr($1,$2)");
        processed = processed.replaceAll("(\\d+(?:\\.\\d*)?)nPr\\(?(\\d+(?:\\.\\d*)?)\\)?", "nPr($1,$2)");

        // 3. Transform root operator: "3ⁿ√27" -> "root(3,27)"
        processed = processed.replaceAll("(\\d+(?:\\.\\d*)?)ⁿ√(\\d+(?:\\.\\d*)?)", "root($1,$2)");

        processed = processed
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", "pi")
                .replace("√", "sqrt")
                .replace("log", "log10")
                .replace("ln", "log")
                .replace("×10^", "*10^");

        processed = processed.replaceAll("(\\d+(\\.\\d*)?)%", "($1/100)");
        processed = processed.replaceAll("(\\d+(\\.\\d*)?)!", "fact($1)");

        return processed;
    }

    private Function[] getMathFunctions() {
        return new Function[]{
                new Function("fact", 1) {
                    @Override
                    public double apply(double... args) {
                        double n = args[0];
                        if (n < 0 || !isWhole(n)) return Double.NaN;
                        if (n > 170) return Double.POSITIVE_INFINITY;
                        double result = 1;
                        for (int i = 2; i <= (int)n; i++) {
                            result *= i;
                        }
                        return result;
                    }
                },
                new Function("nCr", 2) {
                    @Override
                    public double apply(double... args) {
                        double n = Math.round(args[0]);
                        double r = Math.round(args[1]);
                        if (args[0] < 0 || args[1] < 0 || args[1] > args[0] || !isWhole(args[0]) || !isWhole(args[1])) return Double.NaN;
                        if (n > 1000000 && r > 0 && r < n) return Double.POSITIVE_INFINITY; // Avoid long loops for huge n

                        r = Math.min(r, n - r);
                        double result = 1;
                        for (double i = 1; i <= r; i++) {
                            result = result * (n - r + i) / i;
                        }
                        return result;
                    }
                },
                new Function("nPr", 2) {
                    @Override
                    public double apply(double... args) {
                        double n = Math.round(args[0]);
                        double r = Math.round(args[1]);
                        if (args[0] < 0 || args[1] < 0 || args[1] > args[0] || !isWhole(args[0]) || !isWhole(args[1])) return Double.NaN;
                        if (n > 1000000 && r > 0) return Double.POSITIVE_INFINITY;

                        double result = 1;
                        for (double i = 0; i < r; i++) {
                            result *= n - i;
                            if (Double.isInfinite(result)) break;
                        }
                        return result;
                    }
                },
                new Function("root", 2) {
                    @Override
                    public double apply(double... args) {
                        double degree = args[0];
                        double value = args[1];
                        if (degree == 0) return Double.NaN;
                        if (value < 0 && Math.abs(degree % 2) == 1) {
                            return -Math.pow(-value, 1.0 / degree);
                        }
                        return Math.pow(value, 1.0 / degree);
                    }
                },
                new Function("exp", 1) {
                    @Override
                    public double apply(double... args) {
                        return Math.exp(args[0]);
                    }
                },
                new Function("sin", 1) {
                    @Override
                    public double apply(double... args) {
                        double val = args[0];
                        if (currentAngleMode == AngleMode.DEG) val = Math.toRadians(val);
                        else if (currentAngleMode == AngleMode.GRA) val = val * Math.PI / 200.0;
                        return Math.sin(val);
                    }
                },
                new Function("asin", 1) {
                    @Override
                    public double apply(double... args) {
                        double res = Math.asin(args[0]);
                        if (currentAngleMode == AngleMode.DEG) res = Math.toDegrees(res);
                        else if (currentAngleMode == AngleMode.GRA) res = res * 200.0 / Math.PI;
                        return res;
                    }
                },
                new Function("cos", 1) {
                    @Override
                    public double apply(double... args) {
                        double val = args[0];
                        if (currentAngleMode == AngleMode.DEG) val = Math.toRadians(val);
                        else if (currentAngleMode == AngleMode.GRA) val = val * Math.PI / 200.0;
                        return Math.cos(val);
                    }
                },
                new Function("acos", 1) {
                    @Override
                    public double apply(double... args) {
                        double res = Math.acos(args[0]);
                        if (currentAngleMode == AngleMode.DEG) res = Math.toDegrees(res);
                        else if (currentAngleMode == AngleMode.GRA) res = res * 200.0 / Math.PI;
                        return res;
                    }
                },
                new Function("tan", 1) {
                    @Override
                    public double apply(double... args) {
                        double val = args[0];
                        if (currentAngleMode == AngleMode.DEG) val = Math.toRadians(val);
                        else if (currentAngleMode == AngleMode.GRA) val = val * Math.PI / 200.0;
                        return Math.tan(val);
                    }
                },
                new Function("atan", 1) {
                    @Override
                    public double apply(double... args) {
                        double res = Math.atan(args[0]);
                        if (currentAngleMode == AngleMode.DEG) res = Math.toDegrees(res);
                        else if (currentAngleMode == AngleMode.GRA) res = res * 200.0 / Math.PI;
                        return res;
                    }
                },
                new Function("sinh", 1) {
                    @Override
                    public double apply(double... args) {
                        return Math.sinh(args[0]);
                    }
                },
                new Function("cosh", 1) {
                    @Override
                    public double apply(double... args) {
                        return Math.cosh(args[0]);
                    }
                },
                new Function("tanh", 1) {
                    @Override
                    public double apply(double... args) {
                        return Math.tanh(args[0]);
                    }
                }
        };
    }


    private boolean isWhole(double value) {
        return Math.abs(value - Math.rint(value)) < 1.0E-9;
    }

    private int toWholeNumber(double value) {
        if (!isWhole(value)) {
            return -1;
        }
        return (int) Math.rint(value);
    }

    private double factorial(double value) {
        int n = toWholeNumber(value);
        if (n < 0 || n > 170) return Double.NaN;
        double result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
    private void calculateResult() {
        String expressionStr = tvDisplay.getText().toString();
        if (expressionStr.isEmpty() || expressionStr.equals("0")) return;

        try {
            double result = evaluateCurrentExpression();

            // Add to history
            if (history.isEmpty() || !history.get(history.size() - 1).equals(expressionStr)) {
                history.add(expressionStr);
            }
            historyIndex = history.size();

            DecimalFormat df = new DecimalFormat("#,###.########");
            String resultStr = df.format(result);

            tvDisplay.setText(resultStr);
            tvDisplay.setSelection(tvDisplay.length());
            tvPreview.setText(""); // Clear preview on final result
            lastResult = resultStr;
        } catch (Exception e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateHistory(int direction) {
        if (history.isEmpty()) return;

        int nextIndex = historyIndex + direction;
        if (nextIndex >= 0 && nextIndex < history.size()) {
            historyIndex = nextIndex;
            tvDisplay.setText(history.get(historyIndex));
            tvDisplay.setSelection(tvDisplay.length());
            updatePreview();
        } else if (nextIndex == history.size()) {
            historyIndex = nextIndex;
            tvDisplay.setText("");
            tvPreview.setText("");
        }
    }

    private void toggleFraction() {
        String currentText = tvDisplay.getText().toString();
        if (currentText.isEmpty()) return;

        try {
            if (currentText.contains("/")) {
                String[] parts = currentText.split("/");
                double num = Double.parseDouble(parts[0].replace(",", ""));
                double den = Double.parseDouble(parts[1].replace(",", ""));
                double result = num / den;
                DecimalFormat df = new DecimalFormat("#,###.########");
                tvDisplay.setText(df.format(result));
            } else {
                double value = evaluateCurrentExpression();
                tvDisplay.setText(decimalToFraction(value));
            }
            tvDisplay.setSelection(tvDisplay.length());
        } catch (Exception e) {
            // Error
        }
    }

    private void addToMemory() {
        try {
            memoryValue += evaluateCurrentExpression();
            Toast.makeText(this, "Memory: " + formatNumber(memoryValue), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void recallMemory() {
        appendToExpression(formatNumber(memoryValue));
    }

    private void formatEngineering() {
        try {
            double value = evaluateCurrentExpression();
            String formatted = String.format("%.8E", value).replace("E", "e");
            tvDisplay.setText(formatted);
            tvDisplay.setSelection(tvDisplay.length());
            tvPreview.setText("");
        } catch (Exception e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatNumber(double value) {
        DecimalFormat df = new DecimalFormat("#,###.########");
        return df.format(value);
    }
    private String decimalToFraction(double x) {
        if (x == 0) return "0";
        double tolerance = 1.0E-6;
        double h1 = 1; double h2 = 0;
        double k1 = 0; double k2 = 1;
        double b = x;
        do {
            double a = Math.floor(b);
            double aux = h1; h1 = a * h1 + h2; h2 = aux;
            aux = k1; k1 = a * k1 + k2; k2 = aux;
            if (Math.abs(b - a) < 1.0E-12) break;
            b = 1 / (b - a);
        } while (Math.abs(x - h1 / k1) > x * tolerance);

        return (int)h1 + "/" + (int)k1;
    }
}