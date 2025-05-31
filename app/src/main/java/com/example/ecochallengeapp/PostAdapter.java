package com.example.ecochallengeapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> posts;
    private List<String> postKeys;

    public PostAdapter(Context context, List<Post> posts, List<String> postKeys) {
        this.context = context;
        this.posts = posts;
        this.postKeys = postKeys;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textMeta;

        public PostViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textMeta = itemView.findViewById(R.id.textMeta);
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        String postKey = postKeys.get(position);

        holder.textTitle.setText(post.title);
        holder.textMeta.setText(post.author + "   " + post.date);

        // ✅ 게시글 클릭 시 상세보기로 이동 + content도 넘기기
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("title", post.title);
            intent.putExtra("content", post.content); // 🔹 내용 추가
            intent.putExtra("author", post.author);
            intent.putExtra("date", post.date);
            intent.putExtra("uid", post.uid);
            intent.putExtra("key", postKey);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
