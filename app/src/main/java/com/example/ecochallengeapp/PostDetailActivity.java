package com.example.ecochallengeapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class PostDetailActivity extends AppCompatActivity {

    TextView tvTitle, tvAuthor, tvDate, tvContent;
    Button btnDelete;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // 1. 뷰 연결
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvDate = findViewById(R.id.tvDate);
        tvContent = findViewById(R.id.tvContent);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);

        // 2. 뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish());

        // 3. 전달받은 데이터 가져오기
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        String author = getIntent().getStringExtra("author");
        String date = getIntent().getStringExtra("date");
        String uid = getIntent().getStringExtra("uid");
        String key = getIntent().getStringExtra("key");
        String boardType = getIntent().getStringExtra("boardType"); // 🔥 게시판 종류

        // 4. 화면에 표시
        tvTitle.setText(title);
        tvAuthor.setText(author);
        tvDate.setText(date);
        tvContent.setText(content);

        // 5. 로그인한 사용자와 게시글 작성자 비교 → 삭제 허용 여부
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid().equals(uid)) {
            btnDelete.setVisibility(View.VISIBLE);

            btnDelete.setOnClickListener(v -> {
                if (boardType == null || key == null) {
                    Toast.makeText(this, "게시판 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 🔥 boardType에 따라 정확한 경로로 삭제 수행
                FirebaseDatabase.getInstance().getReference(boardType)
                        .child(key)
                        .removeValue()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "삭제 완료", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        } else {
            btnDelete.setVisibility(View.GONE);
        }
    }
}
