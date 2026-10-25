package app.hipercalc.replica;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class CropImageActivity extends BaseActivity {
    private CropView cropView;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bitmap = loadBitmap();
        if (bitmap == null) {
            toast("无法读取图片");
            finish();
            return;
        }
        LinearLayout root = contentColumn();
        root.addView(header("裁剪识别区域", true), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 58)));
        cropView = new CropView();
        cropView.setBitmap(bitmap);
        LinearLayout controls = Ui.row(this, android.view.Gravity.CENTER_VERTICAL);
        Button reset = Ui.button(this, "全图", theme.text, theme.surfaceRaised, 10, 11, false);
        reset.setOnClickListener(v -> cropView.resetCrop());
        Button cancel = Ui.button(this, "取消", theme.muted, theme.surfaceRaised, 10, 11, false);
        cancel.setOnClickListener(v -> finish());
        Button confirm = Ui.button(this, "使用裁剪", theme.accentInk, theme.accent, 10, 12, true);
        confirm.setOnClickListener(v -> saveCrop());
        controls.addView(reset, new LinearLayout.LayoutParams(0, Ui.dp(this, 46), .65f));
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 46), .65f);
        cancelParams.leftMargin = Ui.dp(this, 7);
        controls.addView(cancel, cancelParams);
        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1.2f);
        confirmParams.leftMargin = Ui.dp(this, 7);
        controls.addView(confirm, confirmParams);
        root.addView(cropView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        root.addView(controls, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 66)));
        Ui.padding(controls, 14, 10, 14, 10);
    }

    private Bitmap loadBitmap() {
        String path = getIntent().getStringExtra("imagePath");
        if (path != null) return BitmapFactory.decodeFile(path);
        Uri uri = getIntent().getData();
        if (uri == null) return null;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(input);
        } catch (IOException error) {
            return null;
        }
    }

    private void saveCrop() {
        RectF selection = cropView.getBitmapSelection();
        int x = Math.max(0, (int) Math.floor(selection.left));
        int y = Math.max(0, (int) Math.floor(selection.top));
        int width = Math.min(bitmap.getWidth() - x, (int) Math.ceil(selection.width()));
        int height = Math.min(bitmap.getHeight() - y, (int) Math.ceil(selection.height()));
        if (width < 10 || height < 10) {
            toast("裁剪区域过小");
            return;
        }
        Bitmap cropped = Bitmap.createBitmap(bitmap, x, y, width, height);
        try {
            File file = new File(getCacheDir(), "hiper-crop-" + System.currentTimeMillis() + ".png");
            try (FileOutputStream output = new FileOutputStream(file)) {
                cropped.compress(Bitmap.CompressFormat.PNG, 100, output);
            }
            Intent result = new Intent();
            result.putExtra("croppedPath", file.getAbsolutePath());
            setResult(RESULT_OK, result);
            finish();
        } catch (IOException error) {
            toast("保存裁剪图片失败");
        }
    }

    private final class CropView extends View {
        private final Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Paint shadePaint = new Paint();
        private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF imageRect = new RectF();
        private final RectF cropRect = new RectF();
        private Bitmap image;
        private int activeCorner = -1;
        private float lastX;
        private float lastY;

        CropView() {
            super(CropImageActivity.this);
            shadePaint.setColor(0x99000000);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(Ui.dp(getContext(), 2));
            borderPaint.setColor(0xFFFFFFFF);
            handlePaint.setColor(0xFFB9DFB5);
        }

        void setBitmap(Bitmap bitmap) {
            image = bitmap;
            invalidate();
        }

        void resetCrop() {
            cropRect.set(imageRect);
            invalidate();
        }

        RectF getBitmapSelection() {
            if (imageRect.width() <= 0) return new RectF();
            float scale = imageRect.width() / image.getWidth();
            return new RectF(
                    (cropRect.left - imageRect.left) / scale,
                    (cropRect.top - imageRect.top) / scale,
                    (cropRect.right - imageRect.left) / scale,
                    (cropRect.bottom - imageRect.top) / scale
            );
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (image == null) return;
            float inset = Ui.dp(getContext(), 12);
            float availableWidth = w - inset * 2;
            float availableHeight = h - inset * 2;
            float scale = Math.min(availableWidth / image.getWidth(), availableHeight / image.getHeight());
            float displayWidth = image.getWidth() * scale;
            float displayHeight = image.getHeight() * scale;
            imageRect.set((w - displayWidth) / 2, (h - displayHeight) / 2,
                    (w + displayWidth) / 2, (h + displayHeight) / 2);
            cropRect.set(imageRect);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (image == null) return;
            canvas.drawBitmap(image, null, imageRect, imagePaint);
            if (cropRect.isEmpty()) resetCrop();
            canvas.drawRect(0, 0, getWidth(), cropRect.top, shadePaint);
            canvas.drawRect(0, cropRect.bottom, getWidth(), getHeight(), shadePaint);
            canvas.drawRect(0, cropRect.top, cropRect.left, cropRect.bottom, shadePaint);
            canvas.drawRect(cropRect.right, cropRect.top, getWidth(), cropRect.bottom, shadePaint);
            canvas.drawRect(cropRect, borderPaint);
            float radius = Ui.dp(getContext(), 7);
            float[][] corners = {
                    {cropRect.left, cropRect.top}, {cropRect.right, cropRect.top},
                    {cropRect.left, cropRect.bottom}, {cropRect.right, cropRect.bottom}
            };
            for (float[] corner : corners) canvas.drawCircle(corner[0], corner[1], radius, handlePaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                activeCorner = nearestCorner(x, y);
                if (activeCorner < 0 && cropRect.contains(x, y)) activeCorner = 4;
                lastX = x;
                lastY = y;
                return activeCorner >= 0;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE && activeCorner >= 0) {
                float dx = x - lastX;
                float dy = y - lastY;
                if (activeCorner == 0) cropRect.left = clamp(cropRect.left + dx, imageRect.left, cropRect.right - 20);
                if (activeCorner == 0 || activeCorner == 2) cropRect.top = clamp(cropRect.top + dy, imageRect.top, cropRect.bottom - 20);
                if (activeCorner == 1) cropRect.right = clamp(cropRect.right + dx, cropRect.left + 20, imageRect.right);
                if (activeCorner == 1 || activeCorner == 3) cropRect.top = clamp(cropRect.top + dy, imageRect.top, cropRect.bottom - 20);
                if (activeCorner == 2) cropRect.left = clamp(cropRect.left + dx, imageRect.left, cropRect.right - 20);
                if (activeCorner == 2 || activeCorner == 3) cropRect.bottom = clamp(cropRect.bottom + dy, cropRect.top + 20, imageRect.bottom);
                if (activeCorner == 3) cropRect.right = clamp(cropRect.right + dx, cropRect.left + 20, imageRect.right);
                if (activeCorner == 4) {
                    dx = clamp(dx, imageRect.left - cropRect.left, imageRect.right - cropRect.right);
                    dy = clamp(dy, imageRect.top - cropRect.top, imageRect.bottom - cropRect.bottom);
                    cropRect.offset(dx, dy);
                }
                lastX = x;
                lastY = y;
                invalidate();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                activeCorner = -1;
            }
            return true;
        }

        private int nearestCorner(float x, float y) {
            float threshold = Ui.dp(getContext(), 36);
            if (Math.hypot(x - cropRect.left, y - cropRect.top) < threshold) return 0;
            if (Math.hypot(x - cropRect.right, y - cropRect.top) < threshold) return 1;
            if (Math.hypot(x - cropRect.left, y - cropRect.bottom) < threshold) return 2;
            if (Math.hypot(x - cropRect.right, y - cropRect.bottom) < threshold) return 3;
            return -1;
        }

        private float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
