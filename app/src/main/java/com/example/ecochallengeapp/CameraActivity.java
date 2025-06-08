package com.example.ecochallengeapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 101;

    private Uri photoUri;
    private String currentPhotoPath;

    private int rewardCoin = 10;
    private String missionId = "";
    private String missionTitle = "";

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        uid = user.getUid();

        rewardCoin = getIntent().getIntExtra("rewardCoin", 10);
        missionId = getIntent().getStringExtra("missionId");
        missionTitle = getIntent().getStringExtra("missionTitle");

        Button captureButton = findViewById(R.id.btnCapture);
        captureButton.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());

        Button cancelButton = findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(v -> finish());
    }

    // ✅ 항상 권한 요청 다이얼로그 뜨게 수정
    private void checkCameraPermissionAndOpenCamera() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length >= 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "카메라 및 저장소 권한이 필요합니다", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".provider",
                            photoFile
                    );

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } else {
                    Toast.makeText(this, "사진 파일을 만들 수 없습니다", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(this, "파일 생성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "파일 접근 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "카메라 앱을 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(fileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();

        Log.d("CameraActivity", "📂 사진 저장 경로: " + currentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new File(currentPhotoPath);
            Log.d("CameraActivity", "📸 파일 존재 여부: " + imgFile.exists());
            Log.d("CameraActivity", "📸 파일 크기: " + imgFile.length());

            if (!imgFile.exists() || imgFile.length() == 0) {
                Toast.makeText(this, "사진 저장에 실패했습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(uid)
                    .child("lastMissionDate")
                    .setValue(today);

            Intent intent = new Intent(CameraActivity.this, PhotoUploadActivity.class);
            intent.putExtra("photoPath", currentPhotoPath);
            intent.putExtra("rewardCoin", rewardCoin);
            intent.putExtra("missionId", missionId);
            intent.putExtra("missionTitle", missionTitle);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "사진 촬영 취소 또는 실패", Toast.LENGTH_SHORT).show();
        }
    }
}
