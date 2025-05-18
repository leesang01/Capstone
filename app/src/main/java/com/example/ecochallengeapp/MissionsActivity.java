package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class MissionsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missions_2);

        // ✅ 미션 클릭 시 카메라 화면으로 이동
        findViewById(R.id.mission1).setOnClickListener(v -> openCameraActivity());
        findViewById(R.id.mission2).setOnClickListener(v -> openCameraActivity());
        findViewById(R.id.mission3).setOnClickListener(v -> openCameraActivity());
        findViewById(R.id.mission4).setOnClickListener(v -> openCameraActivity());

        // ✅ 뒤로가기 버튼 클릭 시 메인 화면으로 이동
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MissionsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // 카메라 인증 화면으로 전환
    private void openCameraActivity() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
}
