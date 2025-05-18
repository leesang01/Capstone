package com.example.ecochallengeapp;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera); // XML 파일 연결

        // ✅ 취소 버튼 클릭 시 카메라 화면 종료
        Button cancelButton = findViewById(R.id.btnCancel); // XML의 취소 버튼 ID 확인
        cancelButton.setOnClickListener(v -> finish());
    }
}

