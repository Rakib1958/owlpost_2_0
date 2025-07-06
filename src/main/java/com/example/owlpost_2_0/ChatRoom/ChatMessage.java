package com.example.owlpost_2_0.ChatRoom;


import java.io.Serializable;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sender;
    private String receiver;
    private String content;
    private String fileName;
    private byte[] fileData;
    private boolean isFile;

    // default constructor
    public ChatMessage() {
    }

    // message constructor
    public ChatMessage(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.isFile = false;
    }

    // file constructor
    public ChatMessage(String sender, String receiver, String fileName, byte[] fileData) {
        this.sender = sender;
        this.receiver = receiver;
        this.isFile = true;
        this.fileName = fileName;
        this.fileData = fileData;
        this.content = "File: " + fileName;
    }

    // getters
    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public boolean isFile() {
        return isFile;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public String getDisplayText() {
        return isFile ? ("📎" + fileName) : content;
    }

    // setters
    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFile(boolean file) {
        this.isFile = file;
    }

    public void setFileData(byte[] data) {
        this.fileData = data;
    }

    // show method
    @Override
    public String toString() {
        return sender + " to " + receiver + " [" + "]: " + content;
    }
}