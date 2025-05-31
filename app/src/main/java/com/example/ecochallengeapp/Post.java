package com.example.ecochallengeapp;

public class Post {
    public String title;
    public String content; // 🆕 작성 내용
    public String author;
    public String date;
    public String uid; // 🔐 작성자의 UID

    // Firebase에서 객체로 가져올 때 필요
    public Post() {
    }

    public Post(String title, String content, String author, String date, String uid) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.date = date;
        this.uid = uid;
    }
}
