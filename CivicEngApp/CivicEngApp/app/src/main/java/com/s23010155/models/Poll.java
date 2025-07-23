package com.s23010155.models;

public class Poll {
    private String id;
    private String question;
    private int likes;
    private int dislikes;
    private int userVote; // 0 = no vote, 1 = like, -1 = dislike

    public Poll(String id, String question, int likes, int dislikes) {
        this.id = id;
        this.question = question;
        this.likes = likes;
        this.dislikes = dislikes;
        this.userVote = 0;
    }

    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public int getTotalVotes() {
        return likes + dislikes;
    }

    public int getUserVote() {
        return userVote;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public void setUserVote(int userVote) {
        this.userVote = userVote;
    }
} 