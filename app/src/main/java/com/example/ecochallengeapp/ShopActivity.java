package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView; // ✅ ImageView로 수정

import androidx.appcompat.app.AppCompatActivity;

public class ShopActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop); // activity_shop.xml과 연결

        // 🔙 뒤로가기 버튼 클릭 시 MainActivity로 이동
        ImageView btnBack = findViewById(R.id.btn_back); // ✅ 타입을 ImageView로 변경
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 현재 화면 종료
            }
        });
    }
}
