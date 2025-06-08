package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

// kakao
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.common.model.ClientError;
import com.kakao.sdk.common.model.ClientErrorCause;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.KakaoSdk;

// kotlin용 콜백
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // ✅ Google 로그인 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // ✅ 버튼 참조
        SignInButton googleLoginBtn = findViewById(R.id.btnGoogleLogin);
        Button kakaoLoginBtn = findViewById(R.id.btnKakaoLogin);

        // ✅ Google 로그인
        googleLoginBtn.setOnClickListener(view -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // Kakao SDK 초기화
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key));

        // ✅ Kakao 로그인

        // 로그인 버튼 클릭 이벤트
        Button btnKakao = findViewById(R.id.btnKakaoLogin);
        btnKakao.setOnClickListener(v -> {
            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
                UserApiClient.getInstance().loginWithKakaoTalk(this, kakaoCallback);
            } else {
                UserApiClient.getInstance().loginWithKakaoAccount(this, kakaoCallback);
            }
        });
    }

    // Kakao 로그인 콜백
    private final Function2<OAuthToken, Throwable, Unit> kakaoCallback = (token, error) -> {
        if (error != null) {
            Log.e("KAKAO_LOGIN", "로그인 실패", error);
            Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("KAKAO_LOGIN", "로그인 성공");

            UserApiClient.getInstance().me((user, meError) -> {
                if (meError != null) {
                    Log.e("KAKAO_USER", "사용자 정보 요청 실패", meError);
                } else {
                    String nickname = "";
                    if (user.getKakaoAccount() != null && user.getKakaoAccount().getProfile() != null) {
                        nickname = user.getKakaoAccount().getProfile().getNickname();
                    }

                    Log.i("KAKAO_USER", "닉네임: " + nickname);
                    Toast.makeText(LoginActivity.this, "카카오 로그인 성공", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                return null;
            });

        }
        return null;
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google 로그인 실패: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Firebase 인증 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
