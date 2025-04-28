package com.example.ecochallengeapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MissionsActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1; // 사진 촬영 요청 코드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missions_2); // 네 XML 파일명

        // ✅ 뒤로가기 버튼 클릭
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // 현재 화면 종료 → 이전(MainActivity)로 돌아감
        });

        // ✅ 미션 1 레이아웃 클릭 시 카메라 열기
        LinearLayout mission1 = findViewById(R.id.mission1); // 미션1 레이아웃 id
        mission1.setOnClickListener(v -> {
            openCamera();
        });
    }

    // ✅ 카메라 앱 열기
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // ✅ 사진 찍은 결과 받기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // 📷 여기서 사진(imageBitmap)을 활용 가능
            // 예시: 이미지뷰에 사진 보여주거나, 인증 완료 처리하기
        }
    }
}


