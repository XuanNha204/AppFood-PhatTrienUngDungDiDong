package com.example.foodapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Chuẩn bị ảnh trước khi upload: thu nhỏ (tối đa 1024px), xoay đúng chiều theo EXIF,
 * nén JPEG rồi đóng gói thành MultipartBody.Part. Chạy ở background thread.
 * Server lưu ảnh vào Firestore (tối đa ~900KB/ảnh) nên giảm chất lượng dần nếu ảnh vẫn quá lớn.
 */
public final class ImageUploadHelper {
    private static final int MAX_SIZE = 1024;
    private static final long MAX_BYTES = 800 * 1024;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onReady(MultipartBody.Part part);

        void onError(String message);
    }

    private ImageUploadHelper() {}

    public static void prepare(Context context, Uri uri, String fieldName, Callback callback) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            try {
                File file = compress(app, uri);
                RequestBody body = RequestBody.create(file, MediaType.parse("image/jpeg"));
                MultipartBody.Part part = MultipartBody.Part.createFormData(fieldName, file.getName(), body);
                MAIN.post(() -> callback.onReady(part));
            } catch (Exception e) {
                MAIN.post(() -> callback.onError("Không đọc được ảnh, vui lòng chọn ảnh khác"));
            }
        });
    }

    private static File compress(Context context, Uri uri) throws Exception {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        if (bounds.outWidth <= 0) throw new IllegalArgumentException("Not an image");

        int sample = 1;
        while (bounds.outWidth / (sample * 2) >= MAX_SIZE || bounds.outHeight / (sample * 2) >= MAX_SIZE) {
            sample *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        Bitmap bitmap;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in, null, options);
        }
        if (bitmap == null) throw new IllegalArgumentException("Decode failed");

        float scale = Math.min(1f, (float) MAX_SIZE / Math.max(bitmap.getWidth(), bitmap.getHeight()));
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        int rotation = readRotation(context, uri);
        if (rotation != 0) matrix.postRotate(rotation);
        Bitmap output = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        File dir = new File(context.getCacheDir(), "upload");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "img_" + System.currentTimeMillis() + ".jpg");
        int quality = 85;
        do {
            try (FileOutputStream out = new FileOutputStream(file)) {
                output.compress(Bitmap.CompressFormat.JPEG, quality, out);
            }
            quality -= 15;
        } while (file.length() > MAX_BYTES && quality >= 40);
        if (output != bitmap) output.recycle();
        bitmap.recycle();
        return file;
    }

    private static int readRotation(Context context, Uri uri) {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) return 0;
            int orientation = new ExifInterface(in).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90: return 90;
                case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                default: return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }
}
