package com.example.owlpost_2_0.Controllers;

import com.example.owlpost_2_0.ChatRoom.ChatMessage;
import com.example.owlpost_2_0.Client.ChatClient;
import com.example.owlpost_2_0.Client.Client;
import com.example.owlpost_2_0.Database.DatabaseHandler;
import com.example.owlpost_2_0.Resources.Audios;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.sql.Time;
import java.time.LocalTime;
import java.util.*;

import static java.nio.file.Files.readAllBytes;

public class ChatRoomController implements Initializable {
    // imageview
    @FXML
    private ImageView userImage;
    @FXML
    private ImageView clientImage;
    @FXML
    private ImageView chatBG;

    // panes
    @FXML
    private ScrollPane leftpane;
    @FXML
    private Pane leftbase;
    @FXML
    private ScrollPane chatScroll;
    @FXML
    private StackPane callOverlay;

    // buttons
    @FXML
    private Button sendImg;
    @FXML
    private Button sendEmoji;
    @FXML
    private Button sendMsg;
    @FXML
    private Button audiocall;
    @FXML
    private Button videocall;

    // labels
    @FXML
    private Label userIdLabel;
    @FXML
    private Label clientIdLabel;

    // texts
    @FXML
    private TextField msgField;

    // geometry
    // boxes
    @FXML
    private VBox friendslist;
    @FXML
    private VBox chatbody;
    @FXML
    private VBox msgbox;
    @FXML
    private HBox userCard;
    @FXML
    private Circle userImageClip;
    @FXML
    private Circle clientImageClip;

    // Call UI Components
    private VBox incomingCallUI;
    private VBox activeCallUI;
    private Label callStatusLabel;
    private Label callDurationLabel;
    private Button acceptCallBtn;
    private Button rejectCallBtn;
    private Button endCallBtn;
    private Button muteBtn;
    private Button videoToggleBtn;
    private ImageView callerImageView;
    private ImageView localVideoView;
    private ImageView remoteVideoView;

    // Call state variables
    private boolean isAudioCall = false;
    private boolean isVideoCall = false;
    private boolean isInCall = false;
    private boolean isMuted = false;
    private boolean isVideoEnabled = true;
    private String currentCaller = null;
    private Timeline callDurationTimer;
    private int callDurationSeconds = 0;

    // miscelleneous
    private Client client;
    private ChatClient chatClient;
    private String currentReceiver = null;
    private String[] BGs = {"morning", "day", "evening", "night"};
    private Timer BackgroundTimer;

    private VBox outgoingCallUI;
    private boolean isOutgoingCall = false;
    private Thread audioThread;
    private Thread videoThread;
    private String currentMusic;

    // Initialize call UI components
    private void initializeCallUI() {
        // Create call overlay if not exists in FXML
        if (callOverlay == null) {
            callOverlay = new StackPane();
            callOverlay.setVisible(false);
            callOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
            chatbody.getChildren().add(callOverlay);
        }

        createIncomingCallUI();
        createActiveCallUI();
    }

    private void createIncomingCallUI() {
        incomingCallUI = new VBox(20);
        incomingCallUI.setAlignment(Pos.CENTER);
        incomingCallUI.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 30; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        incomingCallUI.setMaxWidth(300);
        incomingCallUI.setMaxHeight(400);

        // Caller image
        callerImageView = new ImageView();
        callerImageView.setFitWidth(100);
        callerImageView.setFitHeight(100);
        Circle callerClip = new Circle(50);
        callerImageView.setClip(callerClip);

        // Caller name
        Label callerNameLabel = new Label("Incoming Call");
        callerNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        callerNameLabel.setTextFill(Color.BLACK);

        // Call type label
        callStatusLabel = new Label("Audio Call");
        callStatusLabel.setFont(Font.font("Arial", 14));
        callStatusLabel.setTextFill(Color.GRAY);

        // Action buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        acceptCallBtn = new Button("Accept");
        acceptCallBtn.setStyle("-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 25; " +
                "-fx-cursor: hand;");
        acceptCallBtn.setOnAction(this::acceptCall);

        rejectCallBtn = new Button("Reject");
        rejectCallBtn.setStyle("-fx-background-color: #f44336; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 25; " +
                "-fx-cursor: hand;");
        rejectCallBtn.setOnAction(this::rejectCall);

        buttonBox.getChildren().addAll(acceptCallBtn, rejectCallBtn);

        incomingCallUI.getChildren().addAll(callerImageView, callerNameLabel, callStatusLabel, buttonBox);
        callOverlay.getChildren().add(incomingCallUI);
        incomingCallUI.setVisible(false);
    }

    private void createActiveCallUI() {
        activeCallUI = new VBox(15);
        activeCallUI.setAlignment(Pos.CENTER);
        activeCallUI.setStyle("-fx-background-color: rgba(50, 50, 50, 0.95); " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 20;");
        activeCallUI.setMaxWidth(400);
        activeCallUI.setMaxHeight(500);

        // Call duration
        callDurationLabel = new Label("00:00");
        callDurationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        callDurationLabel.setTextFill(Color.WHITE);

        // Video views for video calls
        VBox videoContainer = new VBox(10);
        videoContainer.setAlignment(Pos.CENTER);

        // Remote video (larger)
        remoteVideoView = new ImageView();
        remoteVideoView.setFitWidth(300);
        remoteVideoView.setFitHeight(200);
        remoteVideoView.setStyle("-fx-background-color: black; " +
                "-fx-background-radius: 10;");
        remoteVideoView.setVisible(false);

        // Local video (smaller, overlay)
        localVideoView = new ImageView();
        localVideoView.setFitWidth(100);
        localVideoView.setFitHeight(75);
        localVideoView.setStyle("-fx-background-color: #333; " +
                "-fx-background-radius: 8;");
        localVideoView.setVisible(false);

        videoContainer.getChildren().addAll(remoteVideoView, localVideoView);

        // Call controls
        HBox controlsBox = new HBox(15);
        controlsBox.setAlignment(Pos.CENTER);

        muteBtn = new Button("🔊");
        muteBtn.setStyle("-fx-background-color: #666; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 20px; " +
                "-fx-padding: 10; " +
                "-fx-background-radius: 25; " +
                "-fx-min-width: 50px; " +
                "-fx-min-height: 50px; " +
                "-fx-cursor: hand;");
        muteBtn.setOnAction(this::toggleMute);

        videoToggleBtn = new Button("📹");
        videoToggleBtn.setStyle("-fx-background-color: #666; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 20px; " +
                "-fx-padding: 10; " +
                "-fx-background-radius: 25; " +
                "-fx-min-width: 50px; " +
                "-fx-min-height: 50px; " +
                "-fx-cursor: hand;");
        videoToggleBtn.setOnAction(this::toggleVideo);
        videoToggleBtn.setVisible(false); // Only show for video calls

        endCallBtn = new Button("📞");
        endCallBtn.setStyle("-fx-background-color: #f44336; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 20px; " +
                "-fx-padding: 10; " +
                "-fx-background-radius: 25; " +
                "-fx-min-width: 50px; " +
                "-fx-min-height: 50px; " +
                "-fx-cursor: hand;");
        endCallBtn.setOnAction(this::endCall);

        controlsBox.getChildren().addAll(muteBtn, videoToggleBtn, endCallBtn);

        activeCallUI.getChildren().addAll(callDurationLabel, videoContainer, controlsBox);
        callOverlay.getChildren().add(activeCallUI);
        activeCallUI.setVisible(false);
    }

    private void createOutgoingCallUI() {
        VBox outgoingCallUI = new VBox(20);
        outgoingCallUI.setAlignment(Pos.CENTER);
        outgoingCallUI.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 30; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        outgoingCallUI.setMaxWidth(300);
        outgoingCallUI.setMaxHeight(400);

        // Receiver image
        ImageView receiverImageView = new ImageView();
        receiverImageView.setFitWidth(100);
        receiverImageView.setFitHeight(100);
        Circle receiverClip = new Circle(50);
        receiverImageView.setClip(receiverClip);

        // Receiver name
        Label receiverNameLabel = new Label("Calling...");
        receiverNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        receiverNameLabel.setTextFill(Color.BLACK);

        // Call status
        Label outgoingCallStatus = new Label("Connecting...");
        outgoingCallStatus.setFont(Font.font("Arial", 14));
        outgoingCallStatus.setTextFill(Color.GRAY);

        // Cancel button
        Button cancelCallBtn = new Button("Cancel");
        cancelCallBtn.setStyle("-fx-background-color: #f44336; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 25; " +
                "-fx-cursor: hand;");
        cancelCallBtn.setOnAction(e -> {
            try {
                ChatMessage cancelMsg = new ChatMessage(client.getUsername(), currentReceiver, "CALL_CANCELLED");
                chatClient.sendMessage(cancelMsg);
            } catch (Exception ex) {
                System.err.println("Error cancelling call: " + ex.getMessage());
            }
            hideCallUI();
            resetCallState();
        });

        outgoingCallUI.getChildren().addAll(receiverImageView, receiverNameLabel, outgoingCallStatus, cancelCallBtn);
        callOverlay.getChildren().add(outgoingCallUI);
        outgoingCallUI.setVisible(false);
        this.outgoingCallUI = outgoingCallUI;
    }

    // Call handling methods
    private void handleIncomingAudioCall(String caller) {
        Platform.runLater(() -> {
            if (caller.equals(client.getUsername())) {
                return;
            }

            currentCaller = caller;
            isAudioCall = true;
            isVideoCall = false;
            isOutgoingCall = false;

            // Update caller info
            callStatusLabel.setText("Audio Call");
            Label callerLabel = (Label) incomingCallUI.getChildren().get(1);
            callerLabel.setText(caller);

            // Set caller image
            Client callerClient = findClientByUsername(caller);
            if (callerClient != null) {
                callerImageView.setImage(loadProfileImage(callerClient.getProfilePicturePath()));
            }

            showIncomingCallUI();
        });
    }

    private void handleIncomingVideoCall(String caller) {
        Platform.runLater(() -> {
            if (caller.equals(client.getUsername())) {
                return;
            }

            currentCaller = caller;
            isAudioCall = false;
            isVideoCall = true;
            isOutgoingCall = false;

            // Update caller info
            callStatusLabel.setText("Video Call");
            Label callerLabel = (Label) incomingCallUI.getChildren().get(1);
            callerLabel.setText(caller);

            // Set caller image
            Client callerClient = findClientByUsername(caller);
            if (callerClient != null) {
                callerImageView.setImage(loadProfileImage(callerClient.getProfilePicturePath()));
            }

            showIncomingCallUI();
        });
    }

    private void showIncomingCallUI() {
        callOverlay.setVisible(true);
        incomingCallUI.setVisible(true);
        activeCallUI.setVisible(false);

        // Play incoming call sound
        //Audios.playSound("incoming_call");
    }

    private void showActiveCallUI() {
        Audios.stopBGM();
        callOverlay.setVisible(true);
        incomingCallUI.setVisible(false);
        activeCallUI.setVisible(true);

        // Setup for video call
        if (isVideoCall) {
            remoteVideoView.setVisible(true);
            localVideoView.setVisible(true);
            videoToggleBtn.setVisible(true);
        } else {
            remoteVideoView.setVisible(false);
            localVideoView.setVisible(false);
            videoToggleBtn.setVisible(false);
        }

        startCallDurationTimer();
    }

    private void showOutgoingCallUI(String callType) {
        Platform.runLater(() -> {
            if (!isOutgoingCall) {
                return;
            }

            callOverlay.setVisible(true);
            outgoingCallUI.setVisible(true);
            incomingCallUI.setVisible(false);
            activeCallUI.setVisible(false);

            // Update UI elements
            Label statusLabel = (Label) outgoingCallUI.getChildren().get(2);
            statusLabel.setText(callType);

            Label nameLabel = (Label) outgoingCallUI.getChildren().get(1);
            nameLabel.setText("Calling " + currentReceiver + "...");

            // Set receiver image
            ImageView receiverImg = (ImageView) outgoingCallUI.getChildren().get(0);
            Client receiverClient = findClientByUsername(currentReceiver);
            if (receiverClient != null) {
                receiverImg.setImage(loadProfileImage(receiverClient.getProfilePicturePath()));
            }
        });
    }

    private void hideCallUI() {
        Audios.playBGM(TimeBasedBG());
        callOverlay.setVisible(false);
        incomingCallUI.setVisible(false);
        activeCallUI.setVisible(false);

        if (callDurationTimer != null) {
            callDurationTimer.stop();
        }
    }

    @FXML
    private void acceptCall(ActionEvent event) {
        try {
            ChatMessage acceptMsg = new ChatMessage(client.getUsername(), currentCaller, "CALL_ACCEPTED");
            chatClient.sendMessage(acceptMsg);

            // Set current receiver to the caller
            currentReceiver = currentCaller;
            isInCall = true;

            if (isAudioCall) {
                startAudioCall();
            } else if (isVideoCall) {
                startVideoCall();
            }

            showActiveCallUI();

        } catch (Exception e) {
            System.err.println("Error accepting call: " + e.getMessage());
        }
    }

    @FXML
    private void rejectCall(ActionEvent event) {
        try {
            // Send rejection message to caller
            ChatMessage rejectMsg = new ChatMessage(client.getUsername(), currentCaller, "CALL_REJECTED");
            chatClient.sendMessage(rejectMsg);
        } catch (Exception e) {
            System.err.println("Error rejecting call: " + e.getMessage());
        }

        hideCallUI();
        resetCallState();
    }

    @FXML
    private void endCall(ActionEvent event) {
        // Send end call message
        try {
            String receiver = isOutgoingCall ? currentReceiver : currentCaller;
            ChatMessage endMsg = new ChatMessage(client.getUsername(), receiver, "CALL_ENDED");
            chatClient.sendMessage(endMsg);
        } catch (Exception e) {
            System.err.println("Error ending call: " + e.getMessage());
        }

        // Stop audio/video threads
        if (audioThread != null) {
            audioThread.interrupt();
        }
        if (videoThread != null) {
            videoThread.interrupt();
        }

        hideCallUI();
        resetCallState();
    }

    @FXML
    private void toggleMute(ActionEvent event) {
        isMuted = !isMuted;
        muteBtn.setText(isMuted ? "🔇" : "🔊");
        muteBtn.setStyle("-fx-background-color: " + (isMuted ? "#f44336" : "#666") + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 20px; " +
                "-fx-padding: 10; " +
                "-fx-background-radius: 25; " +
                "-fx-min-width: 50px; " +
                "-fx-min-height: 50px; " +
                "-fx-cursor: hand;");

    }

    private void startAudioCall() {
        audioThread = new Thread(() -> {
            try {
                // Start your UDP audio client
                ClientUDP.start("localhost", 9806);
            } catch (Exception e) {
                System.err.println("Audio call error: " + e.getMessage());
            }
        });
        audioThread.start();
    }

    private void startVideoCall() {
        videoThread = new Thread(() -> {
            try {
                // Start video sender
                VideoSender.start("localhost", 9807);
            } catch (Exception e) {
                System.err.println("Video call error: " + e.getMessage());
            }
        });
        videoThread.start();

        new Thread(() -> {
            try {
                VideoReceiver.start(9808);
            } catch (Exception e) {
                System.err.println("Video receiver error: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void toggleVideo(ActionEvent event) {
        isVideoEnabled = !isVideoEnabled;
        videoToggleBtn.setText(isVideoEnabled ? "📹" : "📷");
        videoToggleBtn.setStyle("-fx-background-color: " + (isVideoEnabled ? "#666" : "#f44336") + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 20px; " +
                "-fx-padding: 10; " +
                "-fx-background-radius: 25; " +
                "-fx-min-width: 50px; " +
                "-fx-min-height: 50px; " +
                "-fx-cursor: hand;");

        localVideoView.setVisible(isVideoEnabled);

        //video toggle logic lagbe
    }

    private void startCallDurationTimer() {
        callDurationSeconds = 0;
        callDurationTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            callDurationSeconds++;
            int minutes = callDurationSeconds / 60;
            int seconds = callDurationSeconds % 60;
            callDurationLabel.setText(String.format("%02d:%02d", minutes, seconds));
        }));
        callDurationTimer.setCycleCount(Timeline.INDEFINITE);
        callDurationTimer.play();
    }

    private void resetCallState() {
        isInCall = false;
        isAudioCall = false;
        isVideoCall = false;
        isOutgoingCall = false;
        isMuted = false;
        isVideoEnabled = true;
        currentCaller = null;
        callDurationSeconds = 0;
        if (audioThread != null) {
            audioThread.interrupt();
            audioThread = null;
        }
        if (videoThread != null) {
            videoThread.interrupt();
            videoThread = null;
        }

        // Reset button states
        muteBtn.setText("🔊");
        videoToggleBtn.setText("📹");
        callDurationLabel.setText("00:00");
    }

    private Client findClientByUsername(String username) {
        List<Client> allClients = DatabaseHandler.getInstance().loadUsers();
        return allClients.stream()
                .filter(c -> c.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    private void initiateAudioCall() {
        System.out.println("Initiating Audio Call...");
        try {
            isOutgoingCall = true;
            isAudioCall = true;
            isVideoCall = false;

            // Show outgoing call UI
            showOutgoingCallUI("Audio Call");

            // Send call initiation message
            ChatMessage callMsg = new ChatMessage(client.getUsername(), currentReceiver, "AUDIO_CALL_INITIATED");
            chatClient.sendMessage(callMsg);

            // Start ringing sound
            //Audios.playSound("outgoing_call");

        } catch (Exception e) {
            System.err.println("Error initiating audio call: " + e.getMessage());
        }
    }

    private void initiateVideoCall() {
        System.out.println("Initiating Video Call...");
        try {
            isOutgoingCall = true;
            isAudioCall = false;
            isVideoCall = true;

            // Show outgoing call UI
            showOutgoingCallUI("Video Call");

            ChatMessage callMsg = new ChatMessage(client.getUsername(), currentReceiver, "VIDEO_CALL_INITIATED");
            chatClient.sendMessage(callMsg);

            // Start ringing sound
            Audios.playSound("outgoing_call");

        } catch (Exception e) {
            System.err.println("Error initiating video call: " + e.getMessage());
        }
    }

    @FXML
    private void onSendMessage() throws Exception {
        if (chatClient == null) {
            System.err.println("ChatClient is not initialized. Cannot send message.");
            return;
        }

        if (client == null) {
            System.err.println("Client is not initialized. Cannot send message.");
            return;
        }
        String content = msgField.getText().trim();
        if (!content.isEmpty() && currentReceiver != null) {
            ChatMessage msg = new ChatMessage(client.getUsername(), currentReceiver, content);
            chatClient.sendMessage(msg);
            msgField.clear();
            showMessageInChat(msg);
            DatabaseHandler.getInstance().saveChatMessage(msg);
        }
    }

    @FXML
    private void onSendFile() {
        if (chatClient == null) {
            System.err.println("ChatClient is not initialized. Cannot send message.");
            return;
        }

        if (client == null) {
            System.err.println("Client is not initialized. Cannot send message.");
            return;
        }
        if (currentReceiver == null) {
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File to send");
        File selected = fileChooser.showOpenDialog(null);
        if (selected != null) {
            try {
                byte[] fileData = readAllBytes(selected.toPath());
                ChatMessage msg = new ChatMessage(client.getUsername(), currentReceiver, "File: " + selected.getName(), fileData);
                chatClient.sendMessage(msg);
                showMessageInChat(msg);
                DatabaseHandler.getInstance().saveChatMessage(msg);

            } catch (Exception e) {
                System.out.println("Error sending file: " + e.getMessage());
            }
        }
    }

    private void handleIncomingMsg(ChatMessage msg) {
        if (msg.getContent().equals("AUDIO_CALL_INITIATED")) {
            if (msg.getSender().equals(client.getUsername())) {
                return;
            } else {
                handleIncomingAudioCall(msg.getSender());
                return;
            }
        } else if (msg.getContent().equals("VIDEO_CALL_INITIATED")) {
            if (msg.getSender().equals(client.getUsername())) {
                return;
            } else {
                handleIncomingVideoCall(msg.getSender());
                return;
            }
        } else if (msg.getContent().equals("CALL_ACCEPTED")) {
            if (!msg.getSender().equals(client.getUsername()) && isOutgoingCall) {
                Platform.runLater(() -> {
                    isInCall = true;
                    if (outgoingCallUI != null) {
                        outgoingCallUI.setVisible(false);
                    }

                    if (isAudioCall) {
                        startAudioCall();
                    } else if (isVideoCall) {
                        startVideoCall();
                    }
                    showActiveCallUI();
                });
            }
            return;
        } else if (msg.getContent().equals("CALL_REJECTED")) {
            if (!msg.getSender().equals(client.getUsername()) && isOutgoingCall) {
                Platform.runLater(() -> {
                    hideCallUI();
                    resetCallState();
                    System.out.println("Call was rejected by " + msg.getSender());
                });
            }
            return;
        } else if (msg.getContent().equals("CALL_ENDED")) {
            if (!msg.getSender().equals(client.getUsername())) {
                Platform.runLater(() -> {
                    hideCallUI();
                    resetCallState();
                    System.out.println("Call ended by " + msg.getSender());
                });
            }
            return;
        } else if (msg.getContent().equals("CALL_CANCELLED")) {
            if (!msg.getSender().equals(client.getUsername())) {
                Platform.runLater(() -> {
                    hideCallUI();
                    resetCallState();
                    System.out.println("Call cancelled by " + msg.getSender());
                });
            }
            return;
        }

        if (msg.getSender().equals(client.getUsername())) {
            return;
        }

        // Handle regular messages
        if (currentReceiver != null &&
                (msg.getSender().equals(currentReceiver) || msg.getReceiver().equals(currentReceiver))) {
            Platform.runLater(() -> showMessageInChat(msg));
        }
    }

    private void showMessageInChat(ChatMessage msg) {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(3));
        hBox.setAlignment(msg.getSender().equals(client.getUsername()) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Node messageNode;
        if (msg.isFile()) {
            String filename = msg.getFileName().toLowerCase();
            if (isImageFile(filename)) {
                messageNode = buildImageBubble(msg);
            } else {
                messageNode = buildFileBubble(msg);
            }
        } else {
            messageNode = buildTextBubble(msg);
        }

        hBox.getChildren().add(messageNode);
        msgbox.getChildren().add(hBox);
        chatScroll.setVvalue(1.0);
    }

    private Node buildTextBubble(ChatMessage msg) {
        Label label = new Label(msg.getContent());
        label.setWrapText(true);
        label.maxWidthProperty().bind(chatScroll.widthProperty().multiply(0.6));
        label.setMaxHeight(Double.MAX_VALUE);
        label.setPadding(new Insets(12));

        if (msg.getSender().equals(client.getUsername())) {
            label.setStyle("-fx-background-color: #128C7E; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 1, 1); " +
                    "-fx-padding: 12; " +
                    "-fx-background-radius: 18; " +
                    "-fx-border-radius: 18; " +
                    "-fx-border-color: rgba(255,255,255,0.3); " +
                    "-fx-border-width: 2;");
        } else {
            label.setStyle("-fx-background-color: white; " +
                    "-fx-text-fill: #2c3e50; " +
                    "-fx-font-weight: bold; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 4, 0, 2, 2); " +
                    "-fx-padding: 12; " +
                    "-fx-background-radius: 18; " +
                    "-fx-border-radius: 18; " +
                    "-fx-border-color: #34495e; " +
                    "-fx-border-width: 2;");
        }
        return label;
    }

    private Node buildFileBubble(ChatMessage msg) {
        Button downloadBtn = new Button("Download " + msg.getFileName());
        downloadBtn.setStyle("-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 1, 1); " +
                "-fx-padding: 10 15; " +
                "-fx-background-radius: 15; " +
                "-fx-border-radius: 15; " +
                "-fx-border-color: #2980b9; " +
                "-fx-border-width: 2; " +
                "-fx-cursor: hand;");

        downloadBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save file");
            chooser.setInitialFileName(msg.getFileName());
            File savelocation = chooser.showSaveDialog(null);
            if (savelocation != null) {
                try {
                    java.nio.file.Files.write(savelocation.toPath(), msg.getFileData());
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
        return downloadBtn;
    }

    private Node buildImageBubble(ChatMessage msg) {
        Image img = new Image(new java.io.ByteArrayInputStream(msg.getFileData()));
        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);

        imageView.setOnMouseClicked(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save image");
            fileChooser.setInitialFileName(msg.getFileName());
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try {
                    java.nio.file.Files.write(file.toPath(), msg.getFileData());
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
        return imageView;
    }

    private boolean isImageFile(String filename) {
        return filename.endsWith(".png") || filename.endsWith(".jpg") ||
                filename.endsWith(".jpeg") || filename.endsWith(".gif");
    }

    public void ButtonAction(ActionEvent event) {
        Audios.playSound("spell");
        Button btn = (Button) event.getSource();

        if (btn.equals(sendMsg)) {
            try {
                onSendMessage();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (btn.equals(sendImg)) {
            onSendFile();
        } else if (btn.equals(audiocall)) {
            if (currentReceiver != null) {
                isAudioCall = true;
                isVideoCall = false;
                initiateAudioCall();
            }
        } else if (btn.equals(videocall)) {
            if (currentReceiver != null) {
                isAudioCall = false;
                isVideoCall = true;
                initiateVideoCall();
            }
        }
    }

    private Image loadProfileImage(String profilePicturePath) {
        if (profilePicturePath == null || profilePicturePath.isBlank()) {
            return loadDefaultProfileImage();
        }

        if (profilePicturePath.startsWith("BLOB:")) {
            String username = profilePicturePath.substring(5);
            Image img = DatabaseHandler.getInstance().getProfilePicture(username);
            if (img != null) {
                return img;
            } else {
                System.out.println("No BLOB found for user " + username + "; using default.");
                return loadDefaultProfileImage();
            }
        }

        try {
            return new Image(profilePicturePath);
        } catch (Exception e) {
            System.out.println("Error loading image from path [" + profilePicturePath + "]: " + e.getMessage());
            return loadDefaultProfileImage();
        }
    }

    private Image loadDefaultProfileImage() {
        URL url = getClass().getResource("/com/example/owlpost_2_0/Images/default-profile.png");
        if (url == null) {
            throw new IllegalStateException("Default profile image not found in resources!");
        }
        return new Image(url.toExternalForm());
    }



    public void getClient(Client client) {
        this.client = client;
        System.out.println("Got client");

        Image userProfileImage = loadProfileImage(client.getProfilePicturePath());
        userImage.setImage(userProfileImage);
        userIdLabel.setText(client.getUsername());

        setCircularImage(userImage, userImageClip, userProfileImage);

        ImageView backgroundImageView = new ImageView();
        backgroundImageView.setImage(new Image(getClass().getResource("/com/example/owlpost_2_0/Images/LoginForm/slytherin.gif").toExternalForm()));
        backgroundImageView.setFitWidth(319);
        backgroundImageView.setFitHeight(630);
        backgroundImageView.setLayoutY(100);
        backgroundImageView.setPreserveRatio(false);

        leftbase.getChildren().add(0, backgroundImageView);
        leftpane.setStyle(
                "-fx-background-color: transparent; " + "-fx-background: transparent;" +  // Dark blue-gray background
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 10;"
        );
        friendslist.setStyle("-fx-background-color: rgba(0,0,0,0.3); " + "-fx-background: transparent;" + "-fx-padding: 10;");
        UpdateBG();
        setUpBackgroundTimer();

        try {
            chatClient = new ChatClient("localhost", client.getUsername());
            chatClient.listenForMsg(this::handleIncomingMsg);
        } catch (Exception e) {
            System.out.println("Error connecting to server");
        }

        List<Client> allclients = DatabaseHandler.getInstance().loadUsers();
        loadFriends(allclients);

    }

    private void setUpBackgroundTimer() {
        BackgroundTimer = new Timer(true);
        BackgroundTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> UpdateBG());
            }
        }, 0, 30 * 60 * 1000);
    }

    private void loadFriends(List<Client> allclients) {
        friendslist.getChildren().clear();

        for (var c : allclients) {
            if (c.getUsername().equals(client.getUsername())) {
                continue;
            }
            HBox card = createFriendCard(c);
            styleFriendCard(card, c);

            friendslist.getChildren().add(card);
        }
    }

    private HBox createFriendCard(Client c) {
        HBox card = new HBox(10);
        card.setPrefWidth(300);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(5));
        String pp = c.getProfilePicturePath();
        Image avatarImage;
        if (pp != null && pp.startsWith("BLOB:")) {
            String username = pp.substring(5);
            avatarImage = DatabaseHandler.getInstance().getProfilePicture(username);
            if (avatarImage == null) {
                System.out.println("No BLOB found for user " + username + ", using default.");
                URL def = getClass().getResource("/com/example/owlpost_2_0/Images/default-profile.png");
                avatarImage = new Image(def.toExternalForm());
            }
        } else if (pp != null && !pp.isBlank()) {
            try {
                avatarImage = new Image(pp);
            } catch (Exception e) {
                System.out.println("Bad image path [" + pp + "], falling back: " + e.getMessage());
                URL def = getClass().getResource("/com/example/owlpost_2_0/Images/default-profile.png");
                avatarImage = new Image(def.toExternalForm());
            }
        } else {
            URL def = getClass().getResource("/com/example/owlpost_2_0/Images/default-profile.png");
            avatarImage = new Image(def.toExternalForm());
        }
        ImageView img = new ImageView(avatarImage);
        img.setFitWidth(40);
        img.setFitHeight(48);
        Circle clip = new Circle(24, 24, 24);
        Image friendImage = loadProfileImage(c.getProfilePicturePath());
        //setCircularImage(img, clip, friendImage);

        Label name = new Label(c.getUsername());
        name.setStyle("-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px;");
        card.getChildren().addAll(img, name);
        return card;
    }

    private void styleFriendCard(HBox card, Client c) {
        card.setOnMouseClicked(e -> {
            currentReceiver = c.getUsername();
            msgbox.getChildren().clear();
            loadChatHistory(client.getUsername(), currentReceiver);

            clientImage.setImage(loadProfileImage(c.getProfilePicturePath()));
            clientIdLabel.setText(c.getUsername());

            updateFriendCardStyle(card);
        });

        // Hover effects
        card.setOnMouseEntered(e -> {
            if (!c.getUsername().equals(currentReceiver)) {
                card.setStyle("-fx-background-color: rgba(255,255,255,0.6); " +  // Higher opacity
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-color: rgba(255,255,255,0.8); " +
                        "-fx-border-width: 2;");
            }
        });

        card.setOnMouseExited(e -> {
            if (!c.getUsername().equals(currentReceiver)) {
                card.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;");
            }
        });
    }

    private void updateFriendCardStyle(HBox selectedCard) {
        // Visual feedback for selected friend
        friendslist.getChildren().forEach(node -> {
            node.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                    "-fx-background-radius: 10; " +
                    "-fx-cursor: hand;");
        });
        selectedCard.setStyle("-fx-background-color: rgba(255,255,255,0.3); " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand;");
    }

    private void loadChatHistory(String sender, String receiver) {
        msgbox.getChildren().clear();
        List<ChatMessage> messages = DatabaseHandler.getInstance().loadChatHistory(sender, receiver);
        //msgbox.getChildren().clear();
        for (var msg : messages) {
//            handleIncomingMsg(msg);
            showMessageInChat(msg);
        }

    }

    private void setCircularImage(ImageView imageView, Circle clip, Image imagePath) {
        imageView.setImage(imagePath);
        userImageClip.radiusProperty().bind(imageView.fitWidthProperty().divide(2));
        userImageClip.centerXProperty().bind(imageView.fitWidthProperty().divide(2));
        userImageClip.centerYProperty().bind(imageView.fitWidthProperty().divide(2));
        imageView.setClip(clip);

    }

    private String TimeBasedBG() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        if (hour >= 6 && hour < 12) {
            return BGs[0];
        } else if (hour >= 12 && hour < 18) {
            return BGs[1];
        } else if (hour >= 18 && hour < 21) {
            return BGs[2];
        } else {
            return BGs[3];
        }
    }

    private void UpdateBG() {
        try {
            String BGPath = "/com/example/owlpost_2_0/Images/Backgrounds/" + TimeBasedBG() + ".gif";
            Image image = new Image(getClass().getResource(BGPath).toExternalForm());
            Audios.playBGM(TimeBasedBG());

            Platform.runLater(() -> {
                chatBG.setImage(image);
                chatBG.setFitWidth(chatbody.getWidth());
                chatBG.setFitHeight(chatbody.getHeight());
                chatBG.setPreserveRatio(false);

                chatBG.toBack();
//                chatScroll.setStyle("-fx-background: rgba(255, 255, 255, 0.1); -fx-background-color: rgba(255, 255, 255, 0.1;");
                chatScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
                msgbox.setStyle("-fx-background-color: transparent; -fx-padding: 10");
            });

        } catch (Exception e) {
            System.out.println("Error setting background: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeCallUI();
        createOutgoingCallUI();
        msgbox.heightProperty().addListener((obs, oldVal, newVal) -> {
            chatScroll.setVvalue(1.0);
        });

        chatScroll.setFitToWidth(true);
        msgbox.prefWidthProperty().bind(chatScroll.widthProperty().subtract(20));
        msgField.setOnAction(e -> {
            try {
                onSendMessage();
            } catch (Exception ex) {
                System.out.println("Error sending message: " + ex.getMessage());
            }
        });

//        Audios.playBGM();

    }


}