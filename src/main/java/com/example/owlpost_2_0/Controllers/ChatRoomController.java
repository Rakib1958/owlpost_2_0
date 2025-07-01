package com.example.owlpost_2_0.Controllers;

import com.example.owlpost_2_0.Client.Client;

public class ChatRoomController {
    private Client client;
    public void getClient(Client client) {
        this.client = client;
        System.out.println("Got client");
    }
}
