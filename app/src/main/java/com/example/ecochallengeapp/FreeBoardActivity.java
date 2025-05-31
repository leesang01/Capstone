package com.example.ecochallengeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class FreeBoardActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PostAdapter adapter;
    ArrayList<Post> postList;
    ArrayList<String> postKeyList; // 🔑 게시글 key 리스트
    DatabaseReference dbRef;
    Button btnAdd;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_board);

        // 1. 뷰 연결
        recyclerView = findViewById(R.id.recyclerView);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);

        // 2. 뒤로가기 버튼 클릭 시 커뮤니티 화면으로 이동
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(FreeBoardActivity.this, CommunityActivity.class);
            startActivity(intent);
            finish();
        });

        // 3. RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postKeyList = new ArrayList<>();
        adapter = new PostAdapter(FreeBoardActivity.this, postList, postKeyList); // ✅ context 추가
        recyclerView.setAdapter(adapter);

        // 4. Firebase 경로 설정
        dbRef = FirebaseDatabase.getInstance().getReference("freeboard");

        // 5. Firebase에서 글 불러오기
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                postList.clear();
                postKeyList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Post post = data.getValue(Post.class);
                    String key = data.getKey();
                    postList.add(post);
                    postKeyList.add(key);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // 오류 처리 가능
            }
        });

        // 6. 글쓰기 화면으로 이동
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(FreeBoardActivity.this, WriteActivity.class);
            startActivity(intent);
        });
    }
}
