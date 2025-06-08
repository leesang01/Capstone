package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.common.model.ClientError;
import com.kakao.sdk.common.model.ClientErrorCause;
import com.kakao.sdk.auth.model.OAuthToken;

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

        // ✅ 버튼 참조 (모두 Button으로 선언)
        Button googleLoginBtn = findViewById(R.id.btnGoogleLogin);
        Button kakaoLoginBtn = findViewById(R.id.btnKakaoLogin);

        // ✅ Google 로그인
        googleLoginBtn.setOnClickListener(view -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // ✅ Kakao 로그인
        kakaoLoginBtn.setOnClickListener(view -> {
            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
                UserApiClient.getInstance().loginWithKakaoTalk(this, (token, error) -> {
                    handleKakaoResult(token, error);
                    return null;
                });
            } else {
                UserApiClient.getInstance().loginWithKakaoAccount(this, (token, error) -> {
                    handleKakaoResult(token, error);
                    return null;
                });
            }
        });
    }

    private void handleKakaoResult(OAuthToken token, Throwable error) {
        if (error != null) {
            if (error instanceof ClientError && ((ClientError) error).getReason() == ClientErrorCause.Cancelled) {
                Toast.makeText(this, "사용자가 로그인 취소함", Toast.LENGTH_SHORT).show();
                Log.w("Kakao", "사용자가 로그인 취소함");
            } else {
                Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show();
                Log.e("Kakao", "카카오 로그인 실패", error);
            }
        } else if (token != null) {
            Log.i("Kakao", "카카오 로그인 성공: " + token.getAccessToken());
            // TODO: 로그인 성공 후 처리
        }
    }

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
