package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class CommunityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // 🔙 뒤로가기 버튼 → 메인화면 이동
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // 현재 액티비티 종료
        });

        // 📝 자유 게시판 버튼 → FreeBoardActivity
        Button btnFree = findViewById(R.id.btnFree);
        btnFree.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, FreeBoardActivity.class);
            startActivity(intent);
        });

        // ⭐ 리뷰 게시판 버튼 → ReviewBoardActivity
        Button btnReview = findViewById(R.id.btnReview);
        btnReview.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, ReviewBoardActivity.class);
            startActivity(intent);
        });

        // ❓ 질문/답변 게시판 버튼 → QnaBoardActivity
        Button btnQna = findViewById(R.id.btnQna);
        btnQna.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, QnaBoardActivity.class);
            startActivity(intent);
        });

        // 📢 공지사항 버튼 → NoticeBoardActivity
        Button btnNotice = findViewById(R.id.btnNotice);
        btnNotice.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, NoticeBoardActivity.class);
            startActivity(intent);
        });
    }
}
