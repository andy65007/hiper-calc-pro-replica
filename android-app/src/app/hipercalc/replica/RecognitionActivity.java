package app.hipercalc.replica;

import android.content.Intent;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;

public final class RecognitionActivity extends BaseActivity {
    private static final int REQUEST_CAMERA = 30;
    private static final int REQUEST_IMAGE = 31;
    private static final int REQUEST_CROP = 32;
    private ImageView preview;
    private EditText expressionInput;
    private Uri captureUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = contentColumn();
        root.addView(header("识别图片表达式", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 16), Ui.dp(this, 6), Ui.dp(this, 16), Ui.dp(this, 20));
        TextView note = Ui.text(this, "可选择图片或调用系统相机。为避免加入 Google 视觉服务，复刻版不调用原始 OCR 模型；图片仅用于本地预览，识别文本需核对或手工输入。", 11, theme.muted, false);
        note.setLineSpacing(0, 1.55f);
        body.addView(note, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 82)));

        LinearLayout buttons = Ui.row(this, Gravity.CENTER_VERTICAL);
        Button camera = Ui.button(this, "拍照", theme.accentInk, theme.accent, 11, 12, true);
        camera.setOnClickListener(v -> capture());
        Button gallery = Ui.button(this, "选择图片", theme.text, theme.surfaceRaised, 11, 12, false);
        gallery.setOnClickListener(v -> chooseImage());
        buttons.addView(camera, new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 1));
        LinearLayout.LayoutParams galleryParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 1);
        galleryParams.leftMargin = Ui.dp(this, 8);
        buttons.addView(gallery, galleryParams);
        body.addView(buttons);

        preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        preview.setBackgroundColor(theme.surface);
        body.addView(preview, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 220)));

        body.addView(Ui.text(this, "识别/校正后的表达式", 10, theme.muted, true),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 38)));
        expressionInput = new EditText(this);
        expressionInput.setHint("例如 2+3×4");
        expressionInput.setHintTextColor(theme.muted);
        expressionInput.setTextColor(theme.text);
        expressionInput.setTextSize(18);
        expressionInput.setSingleLine(true);
        expressionInput.setBackground(AppTheme.background(this, theme.surface, 12));
        Ui.padding(expressionInput, 13, 9, 13, 9);
        body.addView(expressionInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 52)));
        Button insert = Ui.button(this, "带回计算器", theme.accentInk, theme.accent, 11, 12, true);
        insert.setOnClickListener(v -> {
            String text = expressionInput.getText().toString().trim();
            if (text.isEmpty()) {
                toast("请输入或校正表达式");
                return;
            }
            prefs.setExpression(text.replace('×', '*').replace('÷', '/'));
            Intent intent = new Intent(this, CalculatorActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        LinearLayout.LayoutParams insertParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
        insertParams.topMargin = Ui.dp(this, 12);
        body.addView(insert, insertParams);
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private void capture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) {
            toast("未找到系统相机");
            return;
        }
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "hiper_capture_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/HiPER Calc");
            captureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (captureUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivityForResult(intent, REQUEST_CAMERA);
        } catch (RuntimeException error) {
            captureUri = null;
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == REQUEST_CAMERA && captureUri != null) {
            Intent crop = new Intent(this, CropImageActivity.class);
            crop.setData(captureUri);
            crop.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(crop, REQUEST_CROP);
            captureUri = null;
            return;
        }
        if (data == null) return;
        if (requestCode == REQUEST_CAMERA && data.getExtras() != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if (bitmap != null) openCrop(bitmap);
        }
        if (requestCode == REQUEST_IMAGE) {
            Uri uri = data.getData();
            if (uri == null) return;
            Intent crop = new Intent(this, CropImageActivity.class);
            crop.setData(uri);
            crop.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(crop, REQUEST_CROP);
        }
        if (requestCode == REQUEST_CROP) {
            String path = data.getStringExtra("croppedPath");
            if (path != null) {
                Bitmap cropped = android.graphics.BitmapFactory.decodeFile(path);
                if (cropped != null) preview.setImageBitmap(cropped);
            }
        }
    }

    private void openCrop(Bitmap bitmap) {
        try {
            java.io.File file = new java.io.File(getCacheDir(), "hiper-camera-" + System.currentTimeMillis() + ".png");
            try (java.io.FileOutputStream output = new java.io.FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            }
            Intent crop = new Intent(this, CropImageActivity.class);
            crop.putExtra("imagePath", file.getAbsolutePath());
            startActivityForResult(crop, REQUEST_CROP);
        } catch (IOException error) {
            toast("保存拍照图像失败");
        }
    }
}
