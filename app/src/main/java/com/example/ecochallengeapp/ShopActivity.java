package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

        // ✅ 구매 버튼 설정 (XML의 실제 ID 사용)
        setBuyButton(R.id.btnBuyHat, "hat", 100, true);      // 한번만 구매 가능
        setBuyButton(R.id.btnBuyShirt, "shirt", 100, true);  // 한번만 구매 가능
        setBuyButton(R.id.btnBuyHoney, "honey", 20, false);  // 여러번 구매 가능
        setLevelUpKeyButton();  // 레벨업 키는 별도 처리

        // 📦 보관함으로 이동
        findViewById(R.id.btn_go_to_inventory).setOnClickListener(v -> {
            startActivity(new Intent(ShopActivity.this, InventoryActivity.class));
        });

        // 버튼 상태 업데이트
        updateButtonStates();
    }

    // ✅ 일반 아이템 구매 처리 (확인 다이얼로그 추가)
    private void setBuyButton(int buttonId, String itemKey, int price, boolean onlyOnce) {
        Button buyBtn = findViewById(buttonId);
        buyBtn.setOnClickListener(v -> {
            // 구매 확인 다이얼로그 표시
            showPurchaseConfirmDialog(itemKey, price, onlyOnce, buyBtn);
        });
    }

    // 🔑 레벨업 키 전용 구매 버튼 (exp 200 이상 조건)
    private void setLevelUpKeyButton() {
        Button keyBtn = findViewById(R.id.btnBuyKey);
        keyBtn.setOnClickListener(v -> {
            // exp 확인 후 구매 진행
            userRef.child("exp").get().addOnSuccessListener(expSnap -> {
                long currentExp = expSnap.exists() ? expSnap.getValue(Long.class) : 0L;

                if (currentExp < 200) {
                    Toast.makeText(this, "레벨업 키는 경험치 200 이상일 때 구매할 수 있습니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                // exp 조건 만족 시 구매 확인 다이얼로그
                showPurchaseConfirmDialog("key", 150, false, keyBtn);
            });
        });
    }

    // 💬 구매 확인 다이얼로그
    private void showPurchaseConfirmDialog(String itemKey, int price, boolean onlyOnce, Button buyBtn) {
        String itemName = getItemName(itemKey);

        new AlertDialog.Builder(this)
                .setTitle("구매 확인")
                .setMessage(itemName + "을(를) " + price + " 코인에 구매하시겠습니까?")
                .setPositiveButton("구매", (dialog, which) -> {
                    purchaseItem(itemKey, price, onlyOnce, buyBtn);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // 🛒 실제 구매 처리
    private void purchaseItem(String itemKey, int price, boolean onlyOnce, Button buyBtn) {
        buyBtn.setEnabled(false); // 중복 클릭 방지

        DatabaseReference itemRef = userRef.child("inventory").child(itemKey);
        itemRef.get().addOnSuccessListener(itemSnap -> {
            long currentCount = itemSnap.exists() ? itemSnap.getValue(Long.class) : 0L;

            // 한번만 구매 가능한 아이템이고 이미 보유중인 경우
            if (onlyOnce && currentCount > 0) {
                Toast.makeText(this, "이미 보유중인 아이템입니다.", Toast.LENGTH_SHORT).show();
                buyBtn.setEnabled(true);
                return;
            }

            userRef.child("coin").get().addOnSuccessListener(coinSnap -> {
                long currentCoin = coinSnap.exists() ? coinSnap.getValue(Long.class) : 0L;

                if (currentCoin >= price) {
                    itemRef.setValue(currentCount + 1).addOnSuccessListener(aVoid -> {
                        userRef.child("coin").setValue(currentCoin - price);
                        Toast.makeText(this,
                                getItemName(itemKey) + "을(를) 구매했습니다!",
                                Toast.LENGTH_SHORT).show();

                        // 버튼 상태 업데이트
                        updateButtonStates();
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
    }

    // 🔄 버튼 상태 업데이트 (보유중인 아이템은 구매 불가)
    private void updateButtonStates() {
        // 모자 버튼 상태 확인
        checkItemAndUpdateButton("hat", R.id.btnBuyHat, true);

        // 티셔츠 버튼 상태 확인
        checkItemAndUpdateButton("shirt", R.id.btnBuyShirt, true);

        // 꿀통은 여러번 구매 가능하므로 항상 활성화
        Button honeyBtn = findViewById(R.id.btnBuyHoney);
        honeyBtn.setEnabled(true);
        honeyBtn.setText("구매");

        // 레벨업 키는 exp 조건 확인
        updateLevelUpKeyButton();
    }

    // 📝 개별 아이템 버튼 상태 확인
    private void checkItemAndUpdateButton(String itemKey, int buttonId, boolean onlyOnce) {
        if (!onlyOnce) return; // 여러번 구매 가능한 아이템은 건너뛰기

        Button button = findViewById(buttonId);
        userRef.child("inventory").child(itemKey).get().addOnSuccessListener(snap -> {
            long count = snap.exists() ? snap.getValue(Long.class) : 0L;

            if (count > 0) {
                button.setText("보유중");
                button.setEnabled(false);
            } else {
                button.setText("구매");
                button.setEnabled(true);
            }
        });
    }

    // 🔑 레벨업 키 버튼 상태 업데이트
    private void updateLevelUpKeyButton() {
        Button keyBtn = findViewById(R.id.btnBuyKey);

        userRef.child("exp").get().addOnSuccessListener(expSnap -> {
            long currentExp = expSnap.exists() ? expSnap.getValue(Long.class) : 0L;

            if (currentExp < 200) {
                keyBtn.setText("경험치 부족");
                keyBtn.setEnabled(false);
            } else {
                keyBtn.setText("구매");
                keyBtn.setEnabled(true);
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        // 화면 복귀 시 버튼 상태 업데이트
        updateButtonStates();
    }
}