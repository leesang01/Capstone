package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ImageView bearImage;
    private TextView coinText;
    private TextView speechBubbleText;
    private DatabaseReference userRef;
    private String uid;

    // 곰의 감정 상태
    private static enum BearState {
        HAPPY,    // 기본 상태
        SAD,      // 24시간 미션 안함
        ANGRY     // 3일 이상 미션 안함
    }

    // 터치 상호작용 상태
    private static enum TouchState {
        HAPPY,     // 평상시 대화 (happy 이미지)
        SURPRISED  // 놀란 반응 (greeting 이미지)
    }

    private BearState currentBearState = BearState.HAPPY;
    private TouchState currentTouchState = TouchState.HAPPY;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase 사용자 확인
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
        checkBearState();
    }

    private void initializeViews() {
        bearImage = findViewById(R.id.bear_image);
        coinText = findViewById(R.id.coin_text);
        speechBubbleText = findViewById(R.id.speechBubbleText);

        // 🐻 곰 클릭 이벤트
        bearImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBearClicked();
            }
        });

        // 기존 하단 메뉴 버튼들
        findViewById(R.id.btn_missions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMissions();
            }
        });
        findViewById(R.id.btn_community).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCommunity();
            }
        });
        findViewById(R.id.btn_shop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToShop();
            }
        });
        findViewById(R.id.btn_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSpeechBubble("🏠 이미 홈 화면이야!");
            }
        });
    }

    public void goToMissions() {
        startActivity(new Intent(MainActivity.this, MissionsActivity.class));
    }

    public void goToCommunity() {
        startActivity(new Intent(MainActivity.this, CommunityActivity.class));
    }

    public void goToShop() {
        startActivity(new Intent(MainActivity.this, ShopActivity.class));
    }

    private void loadUserData() {
        userRef.child("coin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int coin = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                coinText.setText(String.valueOf(coin));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                coinText.setText("0");
            }
        });
    }

    private void checkBearState() {
        userRef.child("lastMissionDate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lastMissionDate = snapshot.getValue(String.class);
                updateBearState(lastMissionDate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateBearState(null);
            }
        });
    }

    private void updateBearState(String lastMissionDate) {
        long daysSinceLastMission = calculateDaysSince(lastMissionDate);

        if (daysSinceLastMission >= 3) {
            currentBearState = BearState.ANGRY;
        } else if (daysSinceLastMission >= 1) {
            currentBearState = BearState.SAD;
        } else {
            currentBearState = BearState.HAPPY;
        }

        setBearImageByState();
    }

    private long calculateDaysSince(String dateString) {
        if (dateString == null) {
            return 7;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date lastDate = sdf.parse(dateString);
            Date currentDate = new Date();

            long diffInMillis = currentDate.getTime() - lastDate.getTime();
            return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return 7;
        }
    }

    // 🐻 곰 클릭 이벤트
    private void onBearClicked() {
        // 터치 상태 랜덤 변경 (50% 확률)
        if (random.nextBoolean()) {
            currentTouchState = TouchState.SURPRISED;
            bearImage.setImageResource(R.drawable.bear_greeting);
        } else {
            currentTouchState = TouchState.HAPPY;
            setBearImageByState();
        }

        String message = getTouchMessage();
        showSpeechBubble(message);

        // 곰 클릭 시 작은 애니메이션 효과
        bearImage.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        bearImage.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(200)
                                .start();

                        // 6초 후 원래 감정 상태로 복구 (시간 연장!)
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setBearImageByState();
                            }
                        }, 6000); // 3000에서 6000으로 변경!
                    }
                })
                .start();
    }

    // 감정 상태에 맞는 이미지 설정
    private void setBearImageByState() {
        switch (currentBearState) {
            case HAPPY:
                bearImage.setImageResource(R.drawable.bear_happy);
                break;
            case SAD:
                bearImage.setImageResource(R.drawable.bear_sad);
                break;
            case ANGRY:
                bearImage.setImageResource(R.drawable.bear_angry);
                break;
        }
    }

    // 터치 상태에 따른 메시지
    private String getTouchMessage() {
        if (currentTouchState == TouchState.SURPRISED) {
            return getSurprisedMessage();
        } else {
            return getBearMessage();
        }
    }

    // 놀란 반응 메시지
    private String getSurprisedMessage() {
        String[] surprisedMessages = {
                "🐻😮 깜짝이야! 갑자기 만져서 놀랐잖아!",
                "😲 어? 뭐야뭐야? 왜 갑자기 터치했어?",
                "🐻💫 앗! 깜빡 졸고 있었는데 깼네!",
                "😮✨ 헉! 무슨 일이야? 뭔가 긴급한 거야?",
                "🐻😯 어머! 갑작스럽게 왜 그래?",
                "😲💭 앗 깜짝아! 뭔가 중요한 일이야?",
                "🐻😮 어라? 나한테 뭔가 할 말이 있어?",
                "😯🎯 우와! 갑자기 놀래키면 안 되지!",
                "🐻💥 깜짝이야! 심장이 덜컥했네!",
                "😮🌟 헉! 뭔가 재미있는 일이 생겼어?"
        };
        return surprisedMessages[random.nextInt(surprisedMessages.length)];
    }

    // 감정 상태에 따른 기본 메시지
    private String getBearMessage() {
        switch (currentBearState) {
            case HAPPY:
                String[] happyMessages = {
                        "🐻 안녕! 오늘도 환경을 위해 노력해줘서 고마워!",
                        "🌱 너와 함께하니까 지구가 더 건강해지는 것 같아!",
                        "✨ 오늘도 멋진 하루 보내자!",
                        "🎉 환경 보호 챔피언! 정말 자랑스러워!",
                        "💚 네가 하는 작은 실천들이 큰 변화를 만들어!",
                        "🌍 지구가 너에게 고마워하고 있어!"
                };
                return happyMessages[random.nextInt(happyMessages.length)];

            case SAD:
                String[] sadMessages = {
                        "🐻💧 어제 미션을 못했구나... 괜찮아, 오늘부터 다시 시작하자!",
                        "😢 조금 아쉽지만 포기하지 말고 다시 도전해보자!",
                        "🌧️ 가끔은 쉬는 것도 필요해. 오늘은 작은 것부터 시작해볼까?",
                        "💙 네가 다시 돌아와줘서 기뻐! 함께 환경을 지켜나가자!"
                };
                return sadMessages[random.nextInt(sadMessages.length)];

            case ANGRY:
                String[] angryMessages = {
                        "🐻💢 이봐! 벌써 3일이나 미션을 안 했어! 지구가 울고 있다구!",
                        "😤 환경 보호는 매일매일이 중요해! 오늘부터라도 다시 시작하자!",
                        "⚡ 미션을 너무 오래 안 했어... 지금이라도 하나씩 해보자!",
                        "🔥 3일 동안 뭘 했던 거야?! 지구를 구하는 건 미룰 수 없어!"
                };
                return angryMessages[random.nextInt(angryMessages.length)];

            default:
                return "🐻 안녕!";
        }
    }

    // 말풍선 표시 함수
    private void showSpeechBubble(String message) {
        if (speechBubbleText == null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return;
        }

        speechBubbleText.setText(message);
        speechBubbleText.setVisibility(View.VISIBLE);
        speechBubbleText.setAlpha(0f);

        speechBubbleText.animate()
                .alpha(1f)
                .setDuration(500)
                .start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (speechBubbleText != null) {
                    speechBubbleText.animate()
                            .alpha(0f)
                            .setDuration(500)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    if (speechBubbleText != null) {
                                        speechBubbleText.setVisibility(View.GONE);
                                    }
                                }
                            })
                            .start();
                }
            }
        }, 4000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBearState();
    }
}