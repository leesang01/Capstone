package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MissionsActivity extends AppCompatActivity {

    private String uid;

    private LinearLayout mission1, mission2, mission3, mission4;
    private TextView coinText1, coinText2, coinText3, coinText4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missions_2);

        // 🔐 로그인된 사용자 확인
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        uid = user.getUid(); // 로그인한 사용자 UID

        // 🔗 미션 뷰 연결
        mission1 = findViewById(R.id.mission1);
        mission2 = findViewById(R.id.mission2);
        mission3 = findViewById(R.id.mission3);
        mission4 = findViewById(R.id.mission4);

        // 🔗 코인 텍스트뷰 연결
        coinText1 = findViewById(R.id.mission1Coin);
        coinText2 = findViewById(R.id.mission2Coin);
        coinText3 = findViewById(R.id.mission3Coin);
        coinText4 = findViewById(R.id.mission4Coin);

        // ✅ 완료된 미션 숨기기
        hideCompletedMissions();

        // ✅ 클릭 시 코인과 ID 전달
        mission1.setOnClickListener(v -> openCameraActivity(getCoinFromText(coinText1), "mission1"));
        mission2.setOnClickListener(v -> openCameraActivity(getCoinFromText(coinText2), "mission2"));
        mission3.setOnClickListener(v -> openCameraActivity(getCoinFromText(coinText3), "mission3"));
        mission4.setOnClickListener(v -> openCameraActivity(getCoinFromText(coinText4), "mission4"));

        // ✅ 뒤로가기
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    // 🔍 텍스트에서 코인 값 추출
    private int getCoinFromText(TextView textView) {
        try {
            return Integer.parseInt(textView.getText().toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ✅ Firebase에서 완료된 미션 숨김
    private void hideCompletedMissions() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("completedMissions");

        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                for (DataSnapshot missionSnap : snapshot.getChildren()) {
                    String missionId = missionSnap.getKey();
                    if ("mission1".equals(missionId)) mission1.setVisibility(View.GONE);
                    if ("mission2".equals(missionId)) mission2.setVisibility(View.GONE);
                    if ("mission3".equals(missionId)) mission3.setVisibility(View.GONE);
                    if ("mission4".equals(missionId)) mission4.setVisibility(View.GONE);
                }
            }
        });
    }

    // ✅ 카메라 화면으로 이동 + 값 전달
    private void openCameraActivity(int coinValue, String missionId) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("rewardCoin", coinValue);
        intent.putExtra("missionId", missionId);
        startActivity(intent);
    }
}
