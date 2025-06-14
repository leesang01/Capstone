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

        // 2. 뒤로가기 → 커뮤니티로
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(FreeBoardActivity.this, CommunityActivity.class);
            startActivity(intent);
            finish();
        });

        // 3. RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postKeyList = new ArrayList<>();

        // 🔥 boardType 명시적으로 전달
        String boardType = "freeboard";
        adapter = new PostAdapter(FreeBoardActivity.this, postList, postKeyList, boardType);
        recyclerView.setAdapter(adapter);

        // 4. Firebase에서 데이터 불러오기
        dbRef = FirebaseDatabase.getInstance().getReference(boardType);
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
                // 에러 처리
            }
        });

        // 5. 글쓰기 버튼
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(FreeBoardActivity.this, WriteActivity.class);
            startActivity(intent);
        });
    }
}
