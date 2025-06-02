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

public class NoticeBoardActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PostAdapter adapter;
    ArrayList<Post> postList;
    ArrayList<String> postKeyList;
    DatabaseReference dbRef;
    Button btnAdd;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board); // 직접 만들 레이아웃

        recyclerView = findViewById(R.id.recyclerView);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommunityActivity.class);
            startActivity(intent);
            finish();
        });

        String boardType = "noticeboard"; // ✅ 공지사항용 경로
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postKeyList = new ArrayList<>();
        adapter = new PostAdapter(this, postList, postKeyList, boardType);
        recyclerView.setAdapter(adapter);

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
            public void onCancelled(DatabaseError error) { }
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, NoticeWriteActivity.class);
            startActivity(intent);
        });
    }
}
