package com.example.ecochallengeapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhotoUploadActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private TextView coinMessage;
    private Button btnSubmit, btnGoHome;
    private String photoPath;
    private int rewardCoin;
    private String missionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_upload);

        imagePreview = findViewById(R.id.imagePreview);
        coinMessage = findViewById(R.id.coinMessage);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnGoHome = findViewById(R.id.btnGoHome);

        photoPath = getIntent().getStringExtra("photoPath");
        rewardCoin = getIntent().getIntExtra("rewardCoin", 10);
        missionId = getIntent().getStringExtra("missionId");

        if (photoPath != null) {
            File imgFile = new File(photoPath);
            if (imgFile.exists()) {
                Bitmap rotatedBitmap = rotateImageIfRequired(photoPath);
                imagePreview.setImageBitmap(rotatedBitmap);
            } else {
                Toast.makeText(this, "사진 파일이 존재하지 않습니다", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "사진 경로가 전달되지 않았습니다", Toast.LENGTH_SHORT).show();
        }

        btnSubmit.setOnClickListener(v -> giveCoinToUser(rewardCoin));

        btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(PhotoUploadActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private Bitmap rotateImageIfRequired(String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateBitmap(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateBitmap(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateBitmap(bitmap, 270);
                default:
                    return bitmap;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return BitmapFactory.decodeFile(imagePath);
        }
    }

    private Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void giveCoinToUser(int coinAmount) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        DatabaseReference coinRef = userRef.child("coin");

        coinRef.get().addOnSuccessListener(snapshot -> {
            int currentCoin = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
            int updatedCoin = currentCoin + coinAmount;

            coinRef.setValue(updatedCoin).addOnSuccessListener(unused -> {
                String message = coinAmount + "코인을 획득하셨습니다!";
                coinMessage.setText(message);
                coinMessage.setVisibility(View.VISIBLE);

                btnSubmit.setText("인증 완료");
                btnSubmit.setEnabled(false);
                btnGoHome.setVisibility(View.VISIBLE);

                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                if (missionId != null) {
                    DatabaseReference completedRef = userRef.child("completedMissions").child(missionId);
                    completedRef.setValue(true);
                }

                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                userRef.child("lastMissionDate").setValue(today);

            }).addOnFailureListener(e -> {
                Toast.makeText(getApplicationContext(), "코인 지급 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });

        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "데이터 불러오기 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
