package com.example.foodapp.ui.common;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import com.example.foodapp.R;
import com.example.foodapp.util.UiUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.function.Supplier;

/**
 * Hiển thị bottom sheet cho phép chọn ảnh từ thư viện, chụp ảnh bằng camera
 * hoặc dán link ảnh (khi đã gọi enableLink). Phải được khởi tạo trong onCreate() của Activity/Fragment.
 */
public class ImagePickerHelper {

    public interface Listener {
        void onImagePicked(Uri uri);
    }

    public interface LinkListener {
        void onImageLink(String url);
    }

    private final Supplier<Context> contextSupplier;
    private final ActivityResultLauncher<PickVisualMediaRequest> galleryLauncher;
    private final ActivityResultLauncher<Uri> cameraLauncher;
    private Uri pendingCameraUri;
    private LinkListener linkListener;

    public ImagePickerHelper(ActivityResultCaller caller, Supplier<Context> contextSupplier, Listener listener) {
        this.contextSupplier = contextSupplier;
        galleryLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) listener.onImagePicked(uri);
                });
        cameraLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (Boolean.TRUE.equals(success) && pendingCameraUri != null) {
                        listener.onImagePicked(pendingCameraUri);
                    }
                });
    }

    public ImagePickerHelper enableLink(LinkListener listener) {
        this.linkListener = listener;
        return this;
    }

    public void show() {
        Context context = contextSupplier.get();
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_image_source, null);
        view.findViewById(R.id.optionCamera).setOnClickListener(v -> {
            dialog.dismiss();
            openCamera();
        });
        view.findViewById(R.id.optionGallery).setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });
        View optionLink = view.findViewById(R.id.optionLink);
        optionLink.setVisibility(linkListener != null ? View.VISIBLE : View.GONE);
        optionLink.setOnClickListener(v -> {
            dialog.dismiss();
            showLinkDialog();
        });
        dialog.setContentView(view);
        dialog.show();
    }

    public void openGallery() {
        galleryLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public void openCamera() {
        Context context = contextSupplier.get();
        try {
            File dir = new File(context.getCacheDir(), "images");
            if (!dir.exists()) dir.mkdirs();
            File photo = new File(dir, "camera_" + System.currentTimeMillis() + ".jpg");
            pendingCameraUri = FileProvider.getUriForFile(
                    context, context.getPackageName() + ".fileprovider", photo);
            cameraLauncher.launch(pendingCameraUri);
        } catch (ActivityNotFoundException e) {
            UiUtils.toast(context, "Thiết bị không có ứng dụng camera");
        }
    }

    private void showLinkDialog() {
        Context context = contextSupplier.get();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_image_link, null);
        TextInputLayout til = view.findViewById(R.id.tilLink);
        EditText input = view.findViewById(R.id.etLink);
        String copied = clipboardLink(context);
        if (copied != null) input.setText(copied);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.paste_image_link)
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.use_this_image, null)
                .create();
        // Tự xử lý nút OK để không đóng hộp thoại khi link sai
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = input.getText() == null ? "" : input.getText().toString().trim();
            if (!isImageLink(url)) {
                til.setError(context.getString(R.string.invalid_image_link));
                return;
            }
            dialog.dismiss();
            linkListener.onImageLink(url);
        }));
        dialog.show();
        input.requestFocus();
    }

    private static boolean isImageLink(String url) {
        return url.matches("(?i)^https?://\\S+$") && url.length() <= 2000;
    }

    /** Nếu người dùng vừa sao chép một link thì điền sẵn vào ô nhập. */
    private static String clipboardLink(Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null || !cm.hasPrimaryClip() || cm.getPrimaryClip() == null) return null;
        ClipData clip = cm.getPrimaryClip();
        if (clip.getItemCount() == 0 || clip.getItemAt(0).getText() == null) return null;
        String text = clip.getItemAt(0).getText().toString().trim();
        return isImageLink(text) ? text : null;
    }
}
