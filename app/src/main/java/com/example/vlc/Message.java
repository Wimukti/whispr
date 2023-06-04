package com.example.vlc;

public class Message {
    private final long id;
    private final long timestamp;
    private final String content;
    private boolean isSent;

    public Message(long id, long timestamp, String content, boolean isSent) {
        this.id = id;
        this.timestamp = timestamp;
        this.content = content;
        this.isSent = isSent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    public long getId() {
        return id;
    }

    public boolean isSent() {
        return isSent;
    }
}
