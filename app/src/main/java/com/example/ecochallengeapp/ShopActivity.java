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
import com.google.firebase.database.*;

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

        // 🔙 뒤로가기 (Main으로 이동)
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // ✅ 구매 버튼 설정
        setBuyButton(R.id.btnBuyHat, "hat", 100);
        setBuyButton(R.id.btnBuyShirt, "shirt", 100);
        setBuyButton(R.id.btnBuyHoney, "honey", 20);
        setBuyButton(R.id.btnBuyKey, "key", 150);

        // 📦 보관함으로 이동
        findViewById(R.id.btn_go_to_inventory).setOnClickListener(v -> {
            startActivity(new Intent(ShopActivity.this, InventoryActivity.class));
        });
    }

    // ✅ 아이템 구매 처리 (개수 누적 저장 + 코인 차감은 마지막에)
    private void setBuyButton(int buttonId, String itemKey, int price) {
        Button buyBtn = findViewById(buttonId);
        buyBtn.setOnClickListener(v -> {
            buyBtn.setEnabled(false); // 중복 클릭 방지

            DatabaseReference itemRef = userRef.child("inventory").child(itemKey);
            itemRef.get().addOnSuccessListener(itemSnap -> {
                long currentCount = itemSnap.exists() ? itemSnap.getValue(Long.class) : 0L;

                userRef.child("coin").get().addOnSuccessListener(coinSnap -> {
                    long currentCoin = coinSnap.exists() ? coinSnap.getValue(Long.class) : 0L;

                    if (currentCoin >= price) {
                        itemRef.setValue(currentCount + 1).addOnSuccessListener(aVoid -> {
                            userRef.child("coin").setValue(currentCoin - price);
                            Toast.makeText(this,
                                    getItemName(itemKey) + "을(를) 구매했습니다!",
                                    Toast.LENGTH_SHORT).show();
                            buyBtn.setEnabled(true);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "아이템 저장 실패", Toast.LENGTH_SHORT).show();
                            buyBtn.setEnabled(true);
                        });
                    } else {
                        Toast.makeText(this, "코인이 부족합니다.", Toast.LENGTH_SHORT).show();
                        buyBtn.setEnabled(true);
                    }

                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "코인 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    buyBtn.setEnabled(true);
                });

            }).addOnFailureListener(e -> {
                Toast.makeText(this, "아이템 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                buyBtn.setEnabled(true);
            });
        });
    }

    // ✅ 영어 key → 한글 이름
    private String getItemName(String key) {
        switch (key) {
            case "hat":
                return "귀여운 모자";
            case "shirt":
                return "곰 티셔츠";
            case "honey":
                return "꿀통";
            case "key":
                return "레벨업 키";
            default:
                return "알 수 없는 아이템";
        }
    }
}
