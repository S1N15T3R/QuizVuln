package com.team404bnf.quizvuln.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.team404bnf.quizvuln.R;
import com.team404bnf.quizvuln.database.AppDatabase;
import com.team404bnf.quizvuln.models.Profile;

import java.util.concurrent.Executors;

public class ProfileEditActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;
    private ImageView ivProfile;
    private EditText etName;
    private Button btnSave, btnSelectImage;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        ivProfile = findViewById(R.id.ivProfileEdit);
        etName = findViewById(R.id.etNameEdit);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnSelectImage = findViewById(R.id.btnSelectImage);

        loadProfileData();

        btnSelectImage.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    /**
     * Opens system image picker using SAF (Storage Access Framework)
     * Allows long-term persisted read access.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        // Optionally: intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // ✅ Persist permission for long-term use (e.g., after reboot)
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            // Optionally: crop image before saving (uCrop hook here)
            // startCropActivity(imageUri);

            Glide.with(this).load(imageUri).into(ivProfile);
        }
    }

    private void loadProfileData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Profile profile = AppDatabase.getInstance(getApplicationContext()).profileDao().getProfile();
            runOnUiThread(() -> {
                if (profile != null) {
                    etName.setText(profile.name != null ? profile.name : "");
                    if (profile.imagePath != null) {
                        Glide.with(this).load(Uri.parse(profile.imagePath)).into(ivProfile);
                    } else {
                        ivProfile.setImageResource(R.drawable.ic_person);
                    }
                }
            });
        });
    }

    private void saveProfileChanges() {
        String name = etName.getText().toString().trim();

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            Profile profile = db.profileDao().getProfile();

            if (profile == null) profile = new Profile();

            profile.name = name.isEmpty() ? "Guest" : name;
            if (imageUri != null) {
                profile.imagePath = imageUri.toString();
            }

            if (profile.id == 0) db.profileDao().insertProfile(profile);
            else db.profileDao().updateProfile(profile);

            runOnUiThread(() -> {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                finish(); // return to DashboardActivity
            });
        });
    }

    /*
     * ✅ Notes & Gotchas:
     *
     * 1. The profile picture uses persisted URIs (via takePersistableUriPermission).
     *    This ensures the image remains accessible across app restarts as long as
     *    the original file still exists in user storage.
     *
     * 2. If you migrate to the modern Activity Result API:
     *    - Replace startActivityForResult() with registerForActivityResult()
     *      using ActivityResultContracts.OpenDocument.
     *    - You can use the same persist permission logic safely.
     *
     * 3. If you want users to crop images before saving:
     *    - Integrate uCrop (Yalantis library) or a native crop Intent.
     *    - Example:
     *      UCrop.of(sourceUri, destinationUri)
     *           .withAspectRatio(1, 1)
     *           .withMaxResultSize(512, 512)
     *           .start(this);
     *
     * 4. Dashboard auto-refreshes on return via onResume() (already implemented).
     */
}
