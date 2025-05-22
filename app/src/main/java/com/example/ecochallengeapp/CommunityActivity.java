package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class CommunityActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community); // 연결할 XML

        // 🔙 뒤로가기 버튼 클릭 시 MainActivity로 이동
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CommunityActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            }
        });
    }
}
