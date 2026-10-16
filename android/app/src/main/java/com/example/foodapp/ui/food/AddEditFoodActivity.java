package com.example.foodapp.ui.food;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.data.AppState;
import com.example.foodapp.databinding.ActivityAddEditFoodBinding;
import com.example.foodapp.model.Category;
import com.example.foodapp.model.Food;
import com.example.foodapp.ui.common.BaseActivity;
import com.example.foodapp.ui.common.ImagePickerHelper;
import com.example.foodapp.util.ImageUploadHelper;
import com.example.foodapp.util.ImageUtils;
import com.example.foodapp.util.UiUtils;
import com.example.foodapp.util.Validator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/** Thêm món mới / sửa món: ảnh món ăn có thể chụp, chọn từ thư viện hoặc dán link. */
public class AddEditFoodActivity extends BaseActivity {
    private static final String EXTRA_FOOD_JSON = "food_json";
    private static final String EXTRA_CATEGORY_ID = "category_id";
    private static final String STATE_IMAGE_URI = "image_uri";
    private static final String STATE_IMAGE_LINK = "image_link";

    private ActivityAddEditFoodBinding binding;
    private ImagePickerHelper imagePicker;
    private final List<Category> categories = new ArrayList<>();
    private Food editingFood;
    private String selectedCategoryId;
    private Uri selectedImage;
    private String selectedLink;

    public static void startCreate(Context context, @Nullable String categoryId) {
        context.startActivity(new Intent(context, AddEditFoodActivity.class)
                .putExtra(EXTRA_CATEGORY_ID, categoryId));
    }

    public static void startEdit(Context context, Food food) {
        context.startActivity(new Intent(context, AddEditFoodActivity.class)
                .putExtra(EXTRA_FOOD_JSON, new Gson().toJson(food)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imagePicker = new ImagePickerHelper(this, () -> this, this::showSelectedImage)
                .enableLink(this::showSelectedLink);

        String json = getIntent().getStringExtra(EXTRA_FOOD_JSON);
        if (json != null) editingFood = new Gson().fromJson(json, Food.class);
        selectedCategoryId = editingFood != null
                ? editingFood.getCategoryId()
                : getIntent().getStringExtra(EXTRA_CATEGORY_ID);

        setupToolbar(binding.toolbar, getString(editingFood == null
                ? R.string.add_food_title : R.string.edit_food_title));

        binding.cardImage.setOnClickListener(v -> imagePicker.show());
        binding.actCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategoryId = categories.get(position).getId();
            binding.tilCategory.setError(null);
        });
        binding.btnSave.setOnClickListener(v -> save());

        if (editingFood != null) fillForm();
        if (savedInstanceState != null && savedInstanceState.getString(STATE_IMAGE_URI) != null) {
            showSelectedImage(Uri.parse(savedInstanceState.getString(STATE_IMAGE_URI)));
        } else if (savedInstanceState != null && savedInstanceState.getString(STATE_IMAGE_LINK) != null) {
            showSelectedLink(savedInstanceState.getString(STATE_IMAGE_LINK));
        }
        loadCategories();
    }

    private void fillForm() {
        setText(binding.tilName, editingFood.getName());
        setText(binding.tilPrice, String.valueOf(editingFood.getPrice()));
        setText(binding.tilPrepTime, String.valueOf(editingFood.getPrepTime()));
        if (editingFood.getCalories() > 0) setText(binding.tilCalories, String.valueOf(editingFood.getCalories()));
        setText(binding.tilDescription, editingFood.getDescription());
        if (editingFood.getImageUrl() != null && !editingFood.getImageUrl().isEmpty()) {
            binding.imgPreview.setVisibility(View.VISIBLE);
            binding.btnChangeImage.setVisibility(View.VISIBLE);
            ImageUtils.loadFood(binding.imgPreview, editingFood.getImageUrl());
        }
    }

    private void setText(com.google.android.material.textfield.TextInputLayout layout, String value) {
        if (layout.getEditText() != null) layout.getEditText().setText(value);
    }

    private void loadCategories() {
        ApiClient.get().getCategories().enqueue(new ApiCallback<List<Category>>(this) {
            @Override
            public void onSuccess(List<Category> data) {
                categories.clear();
                categories.addAll(data);
                binding.actCategory.setAdapter(new ArrayAdapter<>(AddEditFoodActivity.this,
                        android.R.layout.simple_list_item_1, categories));
                for (Category c : categories) {
                    if (c.getId().equals(selectedCategoryId)) {
                        binding.actCategory.setText(c.getName(), false);
                        break;
                    }
                }
            }

            @Override
            public void onError(String message) {
                UiUtils.toast(AddEditFoodActivity.this, message);
            }
        });
    }

    private void showSelectedImage(Uri uri) {
        selectedImage = uri;
        selectedLink = null;
        binding.imgPreview.setVisibility(View.VISIBLE);
        binding.btnChangeImage.setVisibility(View.VISIBLE);
        Glide.with(this).load(uri).centerCrop().into(binding.imgPreview);
        UiUtils.bounce(binding.btnChangeImage);
    }

    private void showSelectedLink(String url) {
        selectedLink = url;
        selectedImage = null;
        binding.imgPreview.setVisibility(View.VISIBLE);
        binding.btnChangeImage.setVisibility(View.VISIBLE);
        ImageUtils.loadFood(binding.imgPreview, url);
        UiUtils.bounce(binding.btnChangeImage);
    }

    private void save() {
        boolean valid = Validator.required(binding.tilName, "Vui lòng nhập tên món")
                && Validator.required(binding.tilPrice, "Vui lòng nhập giá");
        if (!valid) return;
        if (selectedCategoryId == null) {
            binding.tilCategory.setError("Vui lòng chọn danh mục");
            return;
        }
        if (editingFood == null && selectedImage == null && selectedLink == null) {
            UiUtils.toast(this, "Vui lòng chọn ảnh món ăn");
            imagePicker.show();
            return;
        }

        UiUtils.hideKeyboard(this);
        UiUtils.setLoading(binding.btnSave, true, getString(R.string.action_save));

        if (selectedImage != null) {
            binding.btnSave.setText(R.string.uploading_image);
            ImageUploadHelper.prepare(this, selectedImage, "image", new ImageUploadHelper.Callback() {
                @Override
                public void onReady(MultipartBody.Part part) {
                    submit(part);
                }

                @Override
                public void onError(String message) {
                    UiUtils.setLoading(binding.btnSave, false, getString(R.string.action_save));
                    UiUtils.toast(AddEditFoodActivity.this, message);
                }
            });
        } else {
            submit(null);
        }
    }

    private void submit(@Nullable MultipartBody.Part image) {
        Map<String, RequestBody> fields = new HashMap<>();
        fields.put("name", part(Validator.text(binding.tilName)));
        fields.put("price", part(Validator.text(binding.tilPrice)));
        fields.put("category", part(selectedCategoryId));
        fields.put("description", part(Validator.text(binding.tilDescription)));
        String prep = Validator.text(binding.tilPrepTime);
        if (!prep.isEmpty()) fields.put("prepTime", part(prep));
        String calories = Validator.text(binding.tilCalories);
        if (!calories.isEmpty()) fields.put("calories", part(calories));
        if (selectedLink != null) fields.put("imageUrl", part(selectedLink));

        Call<Food> call = editingFood == null
                ? ApiClient.get().createFood(fields, image)
                : ApiClient.get().updateFood(editingFood.getId(), fields, image);

        call.enqueue(new ApiCallback<Food>(this) {
            @Override
            public void onSuccess(Food food) {
                AppState.markFoodsChanged();
                UiUtils.toast(AddEditFoodActivity.this,
                        editingFood == null ? "Đã thêm món \"" + food.getName() + "\"" : "Đã cập nhật món ăn");
                finish();
            }

            @Override
            public void onError(String message) {
                UiUtils.setLoading(binding.btnSave, false, getString(R.string.action_save));
                UiUtils.toast(AddEditFoodActivity.this, message);
            }
        });
    }

    private static RequestBody part(String value) {
        return RequestBody.create(value, MediaType.parse("text/plain"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedImage != null) outState.putString(STATE_IMAGE_URI, selectedImage.toString());
        if (selectedLink != null) outState.putString(STATE_IMAGE_LINK, selectedLink);
    }
}
