package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class CommunityActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community); // 연결할 XML

        // 🔙 뒤로가기 버튼 → 메인화면
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(CommunityActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // 현재 액티비티 종료
        });

        // 📝 자유 게시판 버튼 → 자유 게시판 화면
        Button btnFree = findViewById(R.id.btnFree);
        btnFree.setOnClickListener(view -> {
            Intent intent = new Intent(CommunityActivity.this, FreeBoardActivity.class);
            startActivity(intent);
        });
    }
}
