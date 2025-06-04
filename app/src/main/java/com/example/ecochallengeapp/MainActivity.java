package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private TextView coinCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 코인 텍스트뷰 연결
        coinCountText = findViewById(R.id.coinCount);

        // 🔹 현재 로그인한 사용자 정보 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            // 🔹 해당 사용자의 코인 정보 불러오기
            DatabaseReference coinRef = FirebaseDatabase.getInstance()
                    .getReference("Users").child(uid).child("coin");

            coinRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    int coin = snapshot.getValue(Integer.class);
                    coinCountText.setText(String.valueOf(coin));
                } else {
                    coinCountText.setText("0");
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "코인 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                coinCountText.setText("0");
            });

        } else {
            // 🔐 로그인 안 되어있으면 로그인 화면으로 이동 (선택사항)
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    // ✅ 미션 버튼
    public void goToMissions(View view) {
        startActivity(new Intent(this, MissionsActivity.class));
    }

    // ✅ 커뮤니티 버튼
    public void goToCommunity(View view) {
        startActivity(new Intent(this, CommunityActivity.class));
    }

    // ✅ 상점 버튼
    public void goToShop(View view) {
        startActivity(new Intent(this, ShopActivity.class));
    }
}
