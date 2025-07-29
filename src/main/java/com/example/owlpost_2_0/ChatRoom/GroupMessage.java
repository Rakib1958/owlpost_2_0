package com.example.owlpost_2_0.ChatRoom;

import java.io.Serializable;
import java.util.Date;

public class GroupMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String messageId;
    private String groupId;
    private String senderUsername;
    private String content;
    private Date timestamp;
    private boolean isFile;
    private String fileName;
    private byte[] fileData;
    private MessageType messageType;

    public enum MessageType {
        TEXT,
        FILE,
        IMAGE,
        SYSTEM,
        MEMBER_JOINED,
        MEMBER_LEFT,
        GROUP_CREATED
    }

    // Constructors
    public GroupMessage() {
        this.timestamp = new Date();
        this.messageType = MessageType.TEXT;
    }

    // Text message constructor
    public GroupMessage(String groupId, String senderUsername, String content) {
        this();
        this.groupId = groupId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.isFile = false;
        this.messageType = MessageType.TEXT;
    }

    // File message constructor
    public GroupMessage(String groupId, String senderUsername, String fileName, byte[] fileData) {
        this();
        this.groupId = groupId;
        this.senderUsername = senderUsername;
        this.fileName = fileName;
        this.fileData = fileData;
        this.isFile = true;
        this.content = "File: " + fileName;

        // Determine if it's an image or regular file
        if (isImageFile(fileName)) {
            this.messageType = MessageType.IMAGE;
        } else {
            this.messageType = MessageType.FILE;
        }
    }

    // System message constructor
    public GroupMessage(String groupId, String content, MessageType messageType) {
        this();
        this.groupId = groupId;
        this.senderUsername = "SYSTEM";
        this.content = content;
        this.messageType = messageType;
        this.isFile = false;
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isFile() { return isFile; }
    public void setFile(boolean file) { isFile = file; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    // Utility methods
    public boolean isSystemMessage() {
        return "SYSTEM".equals(senderUsername) || messageType == MessageType.SYSTEM;
    }

    public boolean isImageMessage() {
        return messageType == MessageType.IMAGE;
    }

    public boolean isFileMessage() {
        return messageType == MessageType.FILE;
    }

    private boolean isImageFile(String filename) {
        if (filename == null) return false;
        String lowerCase = filename.toLowerCase();
        return lowerCase.endsWith(".png") || lowerCase.endsWith(".jpg") ||
                lowerCase.endsWith(".jpeg") || lowerCase.endsWith(".gif") ||
                lowerCase.endsWith(".bmp") || lowerCase.endsWith(".webp");
    }

    public String getFormattedTimestamp() {
        if (timestamp == null) return "";
        return java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(timestamp);
    }

    @Override
    public String toString() {
        return "GroupMessage{" +
                "groupId='" + groupId + '\'' +
                ", sender='" + senderUsername + '\'' +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 50)) : "null") + '\'' +
                ", messageType=" + messageType +
                ", timestamp=" + timestamp +
                '}';
    }
}