package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class InventoryActivity extends AppCompatActivity {

    private LinearLayout inventoryContainer;
    private DatabaseReference userRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        inventoryContainer = findViewById(R.id.inventoryContainer);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("inventory");

        // ✅ 뒤로가기 버튼 클릭 시 ShopActivity로 이동
        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, ShopActivity.class);
            startActivity(intent);
            finish();
        });

        loadInventory();
    }

    private void loadInventory() {
        userRef.get().addOnSuccessListener(snapshot -> {
            inventoryContainer.removeAllViews();

            if (snapshot.exists()) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    if (Boolean.TRUE.equals(item.getValue(Boolean.class))) {
                        addInventoryItem(item.getKey());
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
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(16, 16, 16, 16);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        icon.setImageResource(getIconForItem(itemKey));

        TextView name = new TextView(this);
        name.setText(getItemName(itemKey));
        name.setTextSize(18);
        name.setPadding(24, 0, 0, 0);

        itemLayout.addView(icon);
        itemLayout.addView(name);

        inventoryContainer.addView(itemLayout);
    }

    private int getIconForItem(String itemKey) {
        switch (itemKey) {
            case "hat": return R.drawable.ic_hat;
            case "shirt": return R.drawable.ic_shirt;
            case "honey": return R.drawable.ic_honey;
            case "key": return R.drawable.ic_key;
            default: return R.drawable.ic_coin; // ✅ ic_default → ic_coin
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
