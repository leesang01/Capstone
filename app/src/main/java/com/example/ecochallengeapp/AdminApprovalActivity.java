package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.*;

public class AdminApprovalActivity extends AppCompatActivity {

    private LinearLayout photoListContainer;
    private DatabaseReference photosRef;
    private ImageView btnBack; // ✅ 뒤로가기 버튼 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_approval);

        photoListContainer = findViewById(R.id.photoListContainer);
        btnBack = findViewById(R.id.btnBack); // ✅ 뒤로가기 버튼 연결
        photosRef = FirebaseDatabase.getInstance().getReference("Photos");

        // ✅ 뒤로가기 버튼 클릭 시 MainActivity로 이동
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(AdminApprovalActivity.this, MainActivity.class));
            finish();
        });

        loadPendingPhotos();
    }

    private void loadPendingPhotos() {
        photosRef.orderByChild("status").equalTo("pending")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        photoListContainer.removeAllViews();

                        int count = 0;

                        // ✅ 실제 인증 사진 최대 2개까지 표시
                        for (DataSnapshot photoSnap : snapshot.getChildren()) {
                            if (count >= 2) break;

                            String photoId = photoSnap.getKey();
                            String imageUrl = photoSnap.child("imageUrl").getValue(String.class);
                            String missionTitle = photoSnap.child("missionTitle").getValue(String.class);

                            addPhotoItem(photoId, imageUrl, missionTitle);
                            count++;
                        }

                        // ✅ 예시 박스 부족한 수만큼 추가
                        for (int i = count; i < 2; i++) {
                            addPhotoItem("dummy_" + i, null, "미션 없음");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
    }

    private void addPhotoItem(String photoId, String imageUrl, String missionTitle) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_photo, photoListContainer, false);

        ImageView photoView = itemView.findViewById(R.id.imageView);
        TextView titleText = itemView.findViewById(R.id.tv_missionTitle);
        Button approveBtn = itemView.findViewById(R.id.btn_approve);
        Button rejectBtn = itemView.findViewById(R.id.btn_reject);

        // 🔹 이미지 없으면 기본 곰 이미지 사용
        Glide.with(this)
                .load(imageUrl != null ? imageUrl : R.drawable.bear_happy)
                .into(photoView);

        titleText.setText(missionTitle != null ? missionTitle : "미션 제목 없음");

        // 🔹 예시 항목은 버튼 비활성화
        if (photoId.startsWith("dummy_")) {
            approveBtn.setEnabled(false);
            rejectBtn.setEnabled(false);
        } else {
            approveBtn.setOnClickListener(v -> updateStatus(photoId, "approved"));
            rejectBtn.setOnClickListener(v -> updateStatus(photoId, "rejected"));
        }

        photoListContainer.addView(itemView);
    }

    private void updateStatus(String photoId, String newStatus) {
        photosRef.child(photoId).child("status").setValue(newStatus);
        loadPendingPhotos(); // 갱신
    }
}
