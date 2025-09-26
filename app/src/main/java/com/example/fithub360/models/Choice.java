package com.example.fithub360.models;

public class Choice {
    private int index;
    private ChatMessage message;
    private String finish_reason;

    public int getIndex() { return index; }
    public ChatMessage getMessage() { return message; }
    public String getFinish_reason() { return finish_reason; }
}

