package com.example.contactManager.helper;

public class Message {
    private String content;
    private String text;

    public Message(String content, String text) {
        this.content = content;
        this.text = text;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
