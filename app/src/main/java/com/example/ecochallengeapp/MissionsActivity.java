package com.example.ecochallengeapp;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.*;

public class MissionsActivity extends AppCompatActivity {

    private String uid;

    private LinearLayout mission1, mission2, mission3, mission4;
    private TextView coinText1, coinText2, coinText3, coinText4;
    private TextView completeMessage;

    private TextView completeText1, completeText2, completeText3, completeText4;
    private LinearLayout coinLayout1, coinLayout2, coinLayout3, coinLayout4;

    private TextView missionTitle1, missionTitle2, missionTitle3, missionTitle4;
    private ImageView missionImage1, missionImage2, missionImage3, missionImage4;

    private List<Mission> allMissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missions_2);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        uid = user.getUid();

        mission1 = findViewById(R.id.mission1);
        mission2 = findViewById(R.id.mission2);
        mission3 = findViewById(R.id.mission3);
        mission4 = findViewById(R.id.mission4);

        coinText1 = findViewById(R.id.mission1Coin);
        coinText2 = findViewById(R.id.mission2Coin);
        coinText3 = findViewById(R.id.mission3Coin);
        coinText4 = findViewById(R.id.mission4Coin);

        coinLayout1 = findViewById(R.id.mission1CoinLayout);
        coinLayout2 = findViewById(R.id.mission2CoinLayout);
        coinLayout3 = findViewById(R.id.mission3CoinLayout);
        coinLayout4 = findViewById(R.id.mission4CoinLayout);

        completeText1 = findViewById(R.id.mission1CompleteText);
        completeText2 = findViewById(R.id.mission2CompleteText);
        completeText3 = findViewById(R.id.mission3CompleteText);
        completeText4 = findViewById(R.id.mission4CompleteText);

        missionTitle1 = findViewById(R.id.mission1Title);
        missionTitle2 = findViewById(R.id.mission2Title);
        missionTitle3 = findViewById(R.id.mission3Title);
        missionTitle4 = findViewById(R.id.mission4Title);

        missionImage1 = findViewById(R.id.mission1Image);
        missionImage2 = findViewById(R.id.mission2Image);
        missionImage3 = findViewById(R.id.mission3Image);
        missionImage4 = findViewById(R.id.mission4Image);

        completeMessage = findViewById(R.id.completeMessage);
        completeMessage.setVisibility(View.GONE);

        allMissions = loadAllMissions();

        findViewById(R.id.refreshButton).setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            long lastRefresh = prefs.getLong("lastRefreshTime", 0);
            long interval = 24 * 60 * 60 * 1000L;

            if (now - lastRefresh >= interval) {
                prefs.edit().putLong("lastRefreshTime", now).apply();
                refreshMissions();
                Toast.makeText(this, "미션이 새로고침되었습니다!", Toast.LENGTH_SHORT).show();
            } else {
                long remainMillis = interval - (now - lastRefresh);
                long hours = remainMillis / (1000 * 60 * 60);
                long minutes = (remainMillis / (1000 * 60)) % 60;
                Toast.makeText(this, "새로고침까지 " + hours + "시간 " + minutes + "분 남았습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        List<Mission> saved = loadSelectedMissionsFromPrefs();
        if (saved != null && saved.size() == 4) {
            applyMissions(saved);
        } else {
            refreshMissions();
        }

        findViewById(R.id.backButton).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private List<Mission> loadAllMissions() {
        return Arrays.asList(
                new Mission("텀블러 사용하기", 10), new Mission("쓰레기 줍기", 20), new Mission("전등 끄고 나가기", 30),
                new Mission("장바구니 사용하기", 40), new Mission("대중교통 이용하기", 15), new Mission("전자영수증 받기", 10),
                new Mission("물 절약하기", 12), new Mission("친환경 제품 사용하기", 25), new Mission("중고 거래하기", 18),
                new Mission("리필 용기 사용하기", 14), new Mission("에코백 사용하기", 13), new Mission("종이 아껴쓰기", 16),
                new Mission("플라스틱 줄이기", 22), new Mission("일회용품 사용 줄이기", 17), new Mission("비건 식사하기", 21),
                new Mission("재활용 분리배출", 19)
        );
    }

    private void refreshMissions() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("completedMissions");

        ref.get().addOnSuccessListener(snapshot -> {
            Set<String> completed = new HashSet<>();
            for (DataSnapshot snap : snapshot.getChildren()) {
                completed.add(snap.getKey());
            }

            List<Mission> selected = new ArrayList<>(Arrays.asList(null, null, null, null));
            List<Mission> available = new ArrayList<>();
            int index = 0;

            for (Mission m : allMissions) {
                if (completed.contains(m.getTitle())) {
                    if (index < 4) selected.set(index++, m);
                }
            }

            for (Mission m : allMissions) {
                if (!completed.contains(m.getTitle()) && !selected.contains(m)) {
                    available.add(m);
                }
            }

            Collections.shuffle(available);
            int availIndex = 0;
            for (int i = 0; i < 4; i++) {
                if (selected.get(i) == null && availIndex < available.size()) {
                    selected.set(i, available.get(availIndex++));
                }
            }

            saveSelectedMissionsToPrefs(selected);
            applyMissions(selected);
        });
    }

    private void applyMissions(List<Mission> missions) {
        bindMissionToView(mission1, missionTitle1, coinText1, completeText1, coinLayout1, missionImage1, missions.get(0));
        bindMissionToView(mission2, missionTitle2, coinText2, completeText2, coinLayout2, missionImage2, missions.get(1));
        bindMissionToView(mission3, missionTitle3, coinText3, completeText3, coinLayout3, missionImage3, missions.get(2));
        bindMissionToView(mission4, missionTitle4, coinText4, completeText4, coinLayout4, missionImage4, missions.get(3));
    }

    private void saveSelectedMissionsToPrefs(List<Mission> missions) {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < missions.size(); i++) {
            editor.putString("mission_title_" + i, missions.get(i).getTitle());
            editor.putInt("mission_coin_" + i, missions.get(i).getCoin());
        }
        editor.apply();
    }

    private List<Mission> loadSelectedMissionsFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        List<Mission> result = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            String title = prefs.getString("mission_title_" + i, null);
            int coin = prefs.getInt("mission_coin_" + i, -1);
            if (title == null || coin == -1) return null;
            result.add(new Mission(title, coin));
        }
        return result;
    }

    private void bindMissionToView(LinearLayout layout, TextView title, TextView coinText,
                                   TextView completeText, LinearLayout coinLayout,
                                   ImageView iconView, Mission mission) {
        if (mission == null) {
            layout.setVisibility(View.GONE);
            return;
        }
        layout.setVisibility(View.VISIBLE);
        title.setText(mission.getTitle());
        coinText.setText(String.valueOf(mission.getCoin()));
        completeText.setVisibility(View.GONE);
        coinLayout.setVisibility(View.VISIBLE);
        iconView.setImageResource(getIconResourceForMission(mission.getTitle()));
        layout.setOnClickListener(v -> openCameraActivity(mission.getCoin(), mission.getTitle()));
    }

    private int getIconResourceForMission(String title) {
        Map<String, Integer> iconMap = new HashMap<>();
        iconMap.put("텀블러 사용하기", R.drawable.ic_tumbler);
        iconMap.put("쓰레기 줍기", R.drawable.ic_trash);
        iconMap.put("전등 끄고 나가기", R.drawable.ic_light);
        iconMap.put("장바구니 사용하기", R.drawable.ic_shopping_bag);
        iconMap.put("대중교통 이용하기", R.drawable.ic_transport);
        iconMap.put("전자영수증 받기", R.drawable.ic_receipt);
        iconMap.put("물 절약하기", R.drawable.ic_water);
        iconMap.put("친환경 제품 사용하기", R.drawable.ic_eco_product);
        iconMap.put("중고 거래하기", R.drawable.ic_secondhand);
        iconMap.put("리필 용기 사용하기", R.drawable.ic_container);
        iconMap.put("에코백 사용하기", R.drawable.ic_ecobag);
        iconMap.put("종이 아껴쓰기", R.drawable.ic_paper);
        iconMap.put("플라스틱 줄이기", R.drawable.ic_plastic);
        iconMap.put("일회용품 사용 줄이기", R.drawable.ic_disposable);
        iconMap.put("비건 식사하기", R.drawable.ic_vegan);
        iconMap.put("재활용 분리배출", R.drawable.ic_recycle);
        Integer resId = iconMap.get(title);
        return resId != null ? resId : R.drawable.ic_coin;
    }

    private void openCameraActivity(int coinValue, String missionId) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("rewardCoin", coinValue);
        intent.putExtra("missionId", missionId);
        startActivity(intent);
    }

    static class Mission {
        private final String title;
        private final int coin;

        public Mission(String title, int coin) {
            this.title = title;
            this.coin = coin;
        }

        public String getTitle() {
            return title;
        }

        public int getCoin() {
            return coin;
        }
    }
}
