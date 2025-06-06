package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class ShopActivity extends AppCompatActivity {

    private String uid;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // 🔐 로그인 확인
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        uid = user.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        // 🔙 뒤로가기 버튼
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // ✅ 아이템 구매 버튼 처리
        setBuyButton(R.id.btnBuyHat, "귀여운 모자", 100);
        setBuyButton(R.id.btnBuyShirt, "곰 티셔츠", 100);
        setBuyButton(R.id.btnBuyHoney, "꿀통", 20);
        setBuyButton(R.id.btnBuyKey, "레벨업 키", 150);

        // ✅ 보관함으로 이동
        findViewById(R.id.btn_go_to_inventory).setOnClickListener(v -> {
            Intent intent = new Intent(this, InventoryActivity.class);
            startActivity(intent);
        });
    }

    private void setBuyButton(int buttonId, String itemName, int price) {
        Button buyBtn = findViewById(buttonId);
        buyBtn.setOnClickListener(v -> {
            userRef.child("coin").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Long currentCoin = snapshot.getValue(Long.class);
                    if (currentCoin == null) currentCoin = 0L;

                    if (currentCoin >= price) {
                        // 코인 차감
                        long newCoin = currentCoin - price;
                        userRef.child("coin").setValue(newCoin);

                        // 보관함에 아이템 추가
                        userRef.child("inventory").child(itemName).setValue(true);

                        Toast.makeText(ShopActivity.this, itemName + "을(를) 구매했습니다!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ShopActivity.this, "코인이 부족합니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(ShopActivity.this, "데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
