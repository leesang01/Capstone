package com.example.ecochallengeapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WriteActivity extends AppCompatActivity {

    EditText etTitle, etContent;
    Button btnSubmit, btnCancel;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        // 🔗 뷰 연결
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent); // 작성 내용
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);
        dbRef = FirebaseDatabase.getInstance().getReference("freeboard");

        // ✅ 등록 버튼 클릭 시
        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            String date = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(new Date());

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            String author = user.getDisplayName(); // 또는 getEmail()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            String key = dbRef.push().getKey();
            Post post = new Post(title, content, author, date, uid); // 🔹 내용 포함
            dbRef.child(key).setValue(post)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "등록 완료!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "등록 실패", Toast.LENGTH_SHORT).show();
                    });
        });

        // ✅ 취소 버튼 클릭 시
        btnCancel.setOnClickListener(v -> finish());
    }
}
