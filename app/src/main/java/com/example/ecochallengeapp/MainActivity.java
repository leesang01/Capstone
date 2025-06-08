package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ImageView bearImage;
    private TextView coinText;
    private TextView speechBubbleText;
    private TextView expText;
    private View expFillView;
    private FrameLayout expBox;

    private DatabaseReference userRef;
    private String uid;

    private boolean isWearingHat = false;
    private boolean isWearingShirt = false;
    private boolean isEvolved = false;
    private boolean surprised = false;

    private enum BearState { HAPPY, SAD, ANGRY }
    private BearState currentBearState = BearState.HAPPY;
    private final Random random = new Random();

    private static final int MAX_EXP = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        uid = user.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        initializeViews();
        loadUserData();
        checkEquipStatus();
    }

    private void initializeViews() {
        bearImage = findViewById(R.id.bear_image);
        coinText = findViewById(R.id.coin_text);
        speechBubbleText = findViewById(R.id.speechBubbleText);
        expText = findViewById(R.id.expText);
        expFillView = findViewById(R.id.expFillView);
        expBox = findViewById(R.id.expBox);

        bearImage.setOnClickListener(v -> onBearClicked());

        findViewById(R.id.btn_missions).setOnClickListener(v -> goToMissions());
        findViewById(R.id.btn_community).setOnClickListener(v -> goToCommunity());
        findViewById(R.id.btn_shop).setOnClickListener(v -> goToShop());
        findViewById(R.id.btn_home).setOnClickListener(v -> {});
    }

    private void loadUserData() {
        userRef.child("coin").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                int coin = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                coinText.setText(String.valueOf(coin));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                coinText.setText("0");
            }
        });

        userRef.child("exp").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                int exp = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                updateExpUI(exp);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                updateExpUI(0);
            }
        });
    }

    private void updateExpUI(int currentExp) {
        expText.setText(currentExp + "/" + MAX_EXP);
        expBox.post(() -> {
            int boxWidth = expBox.getWidth();
            float ratio = currentExp / (float) MAX_EXP;
            int fillWidth = (int)(ratio * boxWidth);
            ViewGroup.LayoutParams params = expFillView.getLayoutParams();
            params.width = fillWidth;
            expFillView.setLayoutParams(params);
        });
    }

    private void checkEquipStatus() {
        userRef.child("isWearingHat").get().addOnSuccessListener(snap -> {
            isWearingHat = snap.exists() && Boolean.TRUE.equals(snap.getValue(Boolean.class));
            userRef.child("isWearingShirt").get().addOnSuccessListener(s -> {
                isWearingShirt = s.exists() && Boolean.TRUE.equals(s.getValue(Boolean.class));
                userRef.child("isEvolved").get().addOnSuccessListener(evo -> {
                    isEvolved = evo.exists() && Boolean.TRUE.equals(evo.getValue(Boolean.class));
                    checkBearState();
                });
            });
        });
    }

    private void checkBearState() {
        userRef.child("lastMissionDate").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lastMissionDate = snapshot.getValue(String.class);
                updateBearState(lastMissionDate);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                updateBearState(null);
            }
        });
    }

    private void updateBearState(String lastMissionDate) {
        long daysSince = calculateDaysSince(lastMissionDate);
        if (daysSince >= 3) currentBearState = BearState.ANGRY;
        else if (daysSince >= 1) currentBearState = BearState.SAD;
        else currentBearState = BearState.HAPPY;
        setBearImageByState();
    }

    private long calculateDaysSince(String dateString) {
        if (dateString == null) return 7;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date last = sdf.parse(dateString);
            long diff = new Date().getTime() - last.getTime();
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return 7;
        }
    }

    private void onBearClicked() {
        if (!surprised) {
            surprised = true;
            setBearImage("greeting");
            showSpeechBubble(getSurprisedMessage());
        } else {
            surprised = false;
            setBearImageByState();
            showSpeechBubble(getBearMessage());
        }
    }

    private void setBearImage(String emotion) {
        String drawableName = "bear_";
        if (isEvolved) drawableName += "evolved_";
        drawableName += emotion;

        if (isWearingHat && isWearingShirt) drawableName += "_hat_shirt";
        else if (isWearingHat) drawableName += "_hat";
        else if (isWearingShirt) drawableName += "_shirt";

        int resId = getResources().getIdentifier(drawableName, "drawable", getPackageName());
        if (resId != 0) {
            bearImage.setImageResource(resId);
        } else {
            bearImage.setImageResource(R.drawable.bear_happy);
        }
    }

    private void setBearImageByState() {
        String emotion;
        switch (currentBearState) {
            case SAD: emotion = "sad"; break;
            case ANGRY: emotion = "angry"; break;
            case HAPPY:
            default: emotion = "happy"; break;
        }
        setBearImage(emotion);
    }

    private String getSurprisedMessage() {
        return getBearMessage();
    }

    private String getBearMessage() {
        switch (currentBearState) {
            case HAPPY:
                String[] happy = {
                        "\uD83D\uDC3B 안녕! 오늘도 환경을 위해 노력해줘서 고마워!",
                        "\uD83C\uDF31 너와 함께하니까 지구가 더 건강해지는 것 같아!",
                        "\u2728 오늘도 멋진 하루 보내자!",
                        "\uD83C\uDF89 환경 보호 챔피언! 정말 자랑스러워!",
                        "\uD83D\uDC9A 네가 하는 작은 실천들이 큰 변화를 만들어!"
                };
                return happy[random.nextInt(happy.length)];
            case SAD:
                String[] sad = {
                        "\uD83D\uDC3B\uD83D\uDC94 어제 미션을 못했구나... 괜찮아, 오늘부터 다시 시작하자!",
                        "\uD83D\uDE22 조금 아쉽지만 포기하지 말고 다시 도전해보자!",
                        "\uD83C\uDF27\uFE0F 가끔은 쉬는 것도 필요해. 오늘은 작은 것부터 시작해볼까?",
                        "\uD83D\uDC99 네가 다시 돌아와줘서 기뻐! 함께 환경을 지켜나가자!"
                };
                return sad[random.nextInt(sad.length)];
            case ANGRY:
                String[] angry = {
                        "\uD83D\uDC3B\uD83D\uDCA2 이봐! 벌써 3일이나 미션을 안 했어! 지구가 울고 있다구!",
                        "\uD83D\uDE24 환경 보호는 매일매일이 중요해! 오늘부터라도 다시 시작하자!",
                        "\u26A1 미션을 너무 오래 안 했어... 지금이라도 하나씩 해보자!",
                        "\uD83D\uDD25 3일 동안 뭘 했던 거야?! 지구를 구하는 건 미룰 수 없어!"
                };
                return angry[random.nextInt(angry.length)];
            default:
                return "\uD83D\uDC3B 안녕!";
        }
    }

    private void showSpeechBubble(String message) {
        if (speechBubbleText == null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        speechBubbleText.setText(message);
        speechBubbleText.setVisibility(View.VISIBLE);
        speechBubbleText.setAlpha(1f);

        new Handler().postDelayed(() -> {
            if (speechBubbleText != null) {
                speechBubbleText.setVisibility(View.GONE);
            }
        }, 3000);
    }

    public void goToMissions() { startActivity(new Intent(this, MissionsActivity.class)); }
    public void goToCommunity() { startActivity(new Intent(this, CommunityActivity.class)); }
    public void goToShop() { startActivity(new Intent(this, ShopActivity.class)); }

    @Override
    protected void onResume() {
        super.onResume();
        checkEquipStatus();
    }
}
