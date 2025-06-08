package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class InventoryActivity extends AppCompatActivity {

    private LinearLayout inventoryContainer;
    private DatabaseReference userRef;
    private DatabaseReference rootRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        inventoryContainer = findViewById(R.id.inventoryContainer);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef = rootRef.child("inventory");

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ShopActivity.class));
            finish();
        });

        loadInventory();
    }

    private void loadInventory() {
        userRef.get().addOnSuccessListener(snapshot -> {
            inventoryContainer.removeAllViews();

            if (snapshot.exists()) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    String itemKey = item.getKey();
                    Long count = item.getValue(Long.class);
                    if (count != null && count > 0 || "hat".equals(itemKey) || "shirt".equals(itemKey)) {
                        addInventoryItem(itemKey);
                    }
                }
            } else {
                TextView empty = new TextView(this);
                empty.setText("보관 중인 아이템이 없습니다.");
                empty.setTextSize(16);
                inventoryContainer.addView(empty);
            }
        });
    }

    private void addInventoryItem(String itemKey) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_inventory, inventoryContainer, false);

        ImageView icon = itemView.findViewById(R.id.itemIcon);
        TextView name = itemView.findViewById(R.id.itemName);
        Button btnWear = itemView.findViewById(R.id.btnWear);
        Button btnUnwear = itemView.findViewById(R.id.btnUnwear);

        icon.setImageResource(getIconForItem(itemKey));

        if ("hat".equals(itemKey) || "shirt".equals(itemKey)) {
            name.setText(getItemName(itemKey));

            String wearKey = "hat".equals(itemKey) ? "isWearingHat" : "isWearingShirt";

            // 🔄 착용해제 버튼 숨기고 착용 버튼만 사용하여 토글 기능 구현
            btnUnwear.setVisibility(View.GONE);

            // 현재 착용 상태 확인 후 버튼 업데이트
            rootRef.child(wearKey).get().addOnSuccessListener(snapshot -> {
                boolean isWearing = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                updateWearButton(btnWear, isWearing);
            });

            // 착용/해제 토글 기능
            btnWear.setOnClickListener(v -> {
                rootRef.child(wearKey).get().addOnSuccessListener(snapshot -> {
                    boolean currentlyWearing = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));

                    // 착용 상태 토글
                    rootRef.child(wearKey).setValue(!currentlyWearing).addOnSuccessListener(aVoid -> {
                        String message = !currentlyWearing ?
                                getItemName(itemKey) + " 착용했어요!" :
                                getItemName(itemKey) + " 착용 해제했어요!";

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        // 버튼 상태 업데이트
                        updateWearButton(btnWear, !currentlyWearing);
                    });
                });
            });

        } else {
            // 소비 아이템 (꿀통, 레벨업 키 등)
            userRef.child(itemKey).get().addOnSuccessListener(snapshot -> {
                long count = snapshot.exists() ? snapshot.getValue(Long.class) : 0;
                name.setText(getItemName(itemKey) + " (" + count + ")");
            });

            // 착용해제 버튼 숨기고 착용 버튼만 사용
            btnUnwear.setVisibility(View.GONE);
            btnWear.setText("사용");

            btnWear.setOnClickListener(v -> {
                userRef.child(itemKey).get().addOnSuccessListener(snapshot -> {
                    Long latestCount = snapshot.getValue(Long.class);
                    if (latestCount == null || latestCount <= 0) {
                        userRef.child(itemKey).removeValue();
                        inventoryContainer.removeView(itemView);
                        Toast.makeText(this, getItemName(itemKey) + "을(를) 모두 사용했습니다!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long newCount = latestCount - 1;

                    if (newCount <= 0) {
                        userRef.child(itemKey).removeValue();
                        inventoryContainer.removeView(itemView);
                        Toast.makeText(this, getItemName(itemKey) + "을(를) 모두 사용했습니다!", Toast.LENGTH_SHORT).show();
                    } else {
                        userRef.child(itemKey).setValue(newCount);
                        name.setText(getItemName(itemKey) + " (" + newCount + ")");
                        Toast.makeText(this, getItemName(itemKey) + " 사용! 남은 수량: " + newCount, Toast.LENGTH_SHORT).show();
                    }

                    // ✅ 꿀통 → EXP 증가
                    if ("honey".equals(itemKey)) {
                        DatabaseReference expRef = rootRef.child("exp");
                        expRef.get().addOnSuccessListener(expSnap -> {
                            long currentExp = expSnap.exists() ? expSnap.getValue(Long.class) : 0L;
                            if (currentExp >= 200) {
                                Toast.makeText(this, "EXP가 이미 최대입니다!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            long updatedExp = Math.min(currentExp + 40, 200);
                            expRef.setValue(updatedExp);
                            Toast.makeText(this, "EXP +40 증가! (현재: " + updatedExp + ")", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "EXP 업데이트 실패", Toast.LENGTH_SHORT).show();
                        });
                    }

                    // ✅ 레벨업 키 → 진화
                    if ("key".equals(itemKey)) {
                        DatabaseReference evolveRef = rootRef.child("isEvolved");
                        evolveRef.setValue(true).addOnSuccessListener(unused -> {
                            Toast.makeText(this, "곰이 진화했어요! 🐻‍❄️", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "진화 실패", Toast.LENGTH_SHORT).show();
                        });
                    }

                });
            });
        }

        inventoryContainer.addView(itemView);
    }

    // 🔄 착용/해제 버튼 상태 업데이트
    private void updateWearButton(Button button, boolean isWearing) {
        if (isWearing) {
            button.setText("착용해제");
            // 빨간색 배경으로 변경 (해제 상태를 나타냄)
            button.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light, null));
        } else {
            button.setText("착용");
            // 초록색 배경으로 변경 (착용 가능 상태를 나타냄)
            button.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_light, null));
        }
    }

    private int getIconForItem(String itemKey) {
        switch (itemKey) {
            case "hat": return R.drawable.ic_hat;
            case "shirt": return R.drawable.ic_shirt;
            case "honey": return R.drawable.ic_honey;
            case "key": return R.drawable.ic_key;
            default: return R.drawable.ic_coin;
        }
    }

    private String getItemName(String itemKey) {
        switch (itemKey) {
            case "hat": return "귀여운 모자";
            case "shirt": return "곰 티셔츠";
            case "honey": return "꿀통";
            case "key": return "레벨업 키";
            default: return "알 수 없는 아이템";
        }
    }
}