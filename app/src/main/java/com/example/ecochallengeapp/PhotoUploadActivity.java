package com.example.ecochallengeapp;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

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

        // 📌 뷰 연결
        imagePreview = findViewById(R.id.imagePreview);
        coinMessage = findViewById(R.id.coinMessage);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnGoHome = findViewById(R.id.btnGoHome);

        // 📥 인텐트 데이터 수신
        photoPath = getIntent().getStringExtra("photoPath");
        rewardCoin = getIntent().getIntExtra("rewardCoin", 10);
        missionId = getIntent().getStringExtra("missionId");

        // 📷 사진 미리보기
        if (photoPath != null) {
            File imgFile = new File(photoPath);
            if (imgFile.exists()) {
                imagePreview.setImageBitmap(BitmapFactory.decodeFile(photoPath));
            } else {
                Toast.makeText(this, "사진 파일이 존재하지 않습니다", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "사진 경로가 전달되지 않았습니다", Toast.LENGTH_SHORT).show();
        }

        // 🟢 인증하기 버튼 클릭 처리
        btnSubmit.setOnClickListener(v -> giveCoinToUser(rewardCoin));

        // 🏠 메인으로 이동 버튼
        btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(PhotoUploadActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * ✅ Firebase에 코인 지급 및 완료 미션 저장
     */
    private void giveCoinToUser(int coinAmount) {
        // 🔑 실제 로그인된 사용자 UID 가져오기
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        // 🔹 현재 코인 불러오기
        DatabaseReference coinRef = userRef.child("coin");
        coinRef.get().addOnSuccessListener(snapshot -> {
            int currentCoin = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
            int updatedCoin = currentCoin + coinAmount;

            // 🔹 코인 업데이트
            coinRef.setValue(updatedCoin).addOnSuccessListener(unused -> {
                String message = coinAmount + "코인을 획득하셨습니다!";
                coinMessage.setText(message);
                coinMessage.setVisibility(View.VISIBLE);

                // ✅ 인증 완료 UI 변경
                btnSubmit.setText("인증 완료");
                btnSubmit.setEnabled(false);
                btnGoHome.setVisibility(View.VISIBLE);

                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                // ✅ 완료된 미션 Firebase에 저장
                if (missionId != null) {
                    DatabaseReference completedRef = userRef.child("completedMissions").child(missionId);
                    completedRef.setValue(true);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getApplicationContext(), "코인 지급 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "데이터 불러오기 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
