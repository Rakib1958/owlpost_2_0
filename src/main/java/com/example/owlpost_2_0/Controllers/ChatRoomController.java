package com.example.owlpost_2_0.Controllers;
import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.Transcript;
import com.example.owlpost_2_0.ChatRoom.ChatMessage;
import com.example.owlpost_2_0.ChatRoom.GroupChat;
import com.example.owlpost_2_0.ChatRoom.GroupMessage;
import com.example.owlpost_2_0.Client.ChatClient;
import com.example.owlpost_2_0.Client.Client;
import com.example.owlpost_2_0.Database.DatabaseHandler;
import com.example.owlpost_2_0.Game.GameServer;
import com.example.owlpost_2_0.Gemini.GeminiApiClient;
import com.example.owlpost_2_0.Resources.Animations;
import com.example.owlpost_2_0.Resources.Audios;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.sql.Time;
import java.time.LocalTime;
import java.util.*;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
//import javafx.scene.control.GridPane;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import java.util.Optional;
import java.util.UUID;
import com.example.owlpost_2_0.Game.GameManager;
import javafx.scene.control.ChoiceDialog;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.sound.sampled.*;

import static com.example.owlpost_2_0.VoiceAPI.TestVoiceAPI.*;
import static java.nio.file.Files.readAllBytes;

public class ChatRoomController implements Initializable {

    @FXML
    private ImageView userImage;
    @FXML
    private ImageView clientImage;
    @FXML
    private ImageView chatBG;

    @FXML
    private ScrollPane leftpane;
    @FXML
    private Pane leftbase;
    @FXML
    private ScrollPane chatScroll;
    @FXML
    private StackPane callOverlay;

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
    @FXML
    private Button closeEmojiPicker;
    @FXML
    private Button voiceToText;

    @FXML
    private Label userIdLabel;
    @FXML
    private Label clientIdLabel;
    @FXML
    private TextField msgField;
    @FXML
    private VBox friendslist;
    @FXML
    private VBox chatbody;
    @FXML
    private VBox msgbox;
    @FXML
    private HBox userCard;
    @FXML
    private HBox clientCard;
    @FXML
    private Button infobtn;
    @FXML
    private Circle userImageClip;
    @FXML
    private Circle clientImageClip;
    @FXML
    private Pane emojipane_test;
    @FXML
    private VBox emojibox;
    @FXML
    private ScrollPane emojiscroller;

    // Gemini Resources
    @FXML
    private Button geminiBTN;
    @FXML
    private Pane infoPane;
    @FXML
    private Pane geminiPane;
    @FXML
    private Button geminisend;
    @FXML
    private TextField geminimsg;
    @FXML
    private ScrollPane geminiscrollpane;
    @FXML
    private VBox geminibox;
    private GeminiApiClient apiClient;

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
    private boolean isAudioCall = false;
    private boolean isVideoCall = false;
    private boolean isInCall = false;
    private boolean isMuted = false;
    private boolean isVideoEnabled = true;
    private String currentCaller = null;
    private Timeline callDurationTimer;
    private int callDurationSeconds = 0;
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
    private Thread videoSenderThread;
    private Thread videoReceiverThread;
    private boolean isGroupChatMode = false;
    private Dialog<GroupChat> createGroupDialog;
    private TextField groupNameField;
    private TextArea groupDescriptionField;
    private ListView<String> availableUsersListView;
    private ListView<String> selectedMembersListView;

    // voice recognition
    private boolean recording = false;
    TargetDataLine microphone;
    Thread recordingThread;
    AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
    File audioFile = new File("recording.wav");

    @FXML
    private Pane introvideopane, footstepPane;
    @FXML
    private MediaView owlvideo;
    @FXML
    private Label owltext, privacytext;


    private HBox groupButtonsContainer;
    private Button groupInfoBtn;
    private Button addMemberBtn;
    private Button leaveGroupBtn;
    private GameManager gameManager;
    private Button gameBtn;
    private void initializeCallUI() {
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
        incomingCallUI.setStyle("-fx-background-color: rgba(26, 15, 8, 0.98); " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 30; " +
                "-fx-border-color: #8b4513; " +
                "-fx-border-width: 3; " +
                "-fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, #000000, 15, 0.8, 0, 0);");
        incomingCallUI.setMaxWidth(300);
        incomingCallUI.setMaxHeight(400);

        callerImageView = new ImageView();
        callerImageView.setFitWidth(100);
        callerImageView.setFitHeight(100);
        Label callerNameLabel = new Label("Incoming Call");
        callerNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        callerNameLabel.setTextFill(Color.BLACK);

        callStatusLabel = new Label("Audio Call");
        callStatusLabel.setFont(Font.font("Arial", 14));
        callStatusLabel.setTextFill(Color.GRAY);

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

        callDurationLabel = new Label("00:00");
        callDurationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        callDurationLabel.setTextFill(Color.WHITE);

        VBox videoContainer = new VBox(10);
        videoContainer.setAlignment(Pos.CENTER);

        remoteVideoView = new ImageView();
        remoteVideoView.setFitWidth(300);
        remoteVideoView.setFitHeight(200);
        remoteVideoView.setStyle("-fx-background-color: black; " +
                "-fx-background-radius: 10;");
        remoteVideoView.setVisible(false);

        localVideoView = new ImageView();
        localVideoView.setFitWidth(100);
        localVideoView.setFitHeight(75);
        localVideoView.setStyle("-fx-background-color: #333; " +
                "-fx-background-radius: 8;");
        localVideoView.setVisible(false);

        videoContainer.getChildren().addAll(remoteVideoView, localVideoView);

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
        videoToggleBtn.setVisible(false);

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

        ImageView receiverImageView = new ImageView();
        receiverImageView.setFitWidth(100);
        receiverImageView.setFitHeight(100);
        Circle receiverClip = new Circle(50);
        receiverImageView.setClip(receiverClip);

        Label receiverNameLabel = new Label("Calling...");
        receiverNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        receiverNameLabel.setTextFill(Color.BLACK);

        Label outgoingCallStatus = new Label("Connecting...");
        outgoingCallStatus.setFont(Font.font("Arial", 14));
        outgoingCallStatus.setTextFill(Color.GRAY);

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

    private void handleIncomingAudioCall(String caller) {
        Platform.runLater(() -> {
            if (caller.equals(client.getUsername())) {
                return;
            }

            currentCaller = caller;
            isAudioCall = true;
            isVideoCall = false;
            isOutgoingCall = false;

            callStatusLabel.setText("Audio Call");
            Label callerLabel = (Label) incomingCallUI.getChildren().get(1);
            callerLabel.setText(caller);

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

            callStatusLabel.setText("Video Call");
            Label callerLabel = (Label) incomingCallUI.getChildren().get(1);
            callerLabel.setText(caller);

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
        //Audios.playSound("incoming_call");
    }

    private void showActiveCallUI() {
        Audios.stopBGM();
        callOverlay.setVisible(true);
        incomingCallUI.setVisible(false);
        activeCallUI.setVisible(true);
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
            Label statusLabel = (Label) outgoingCallUI.getChildren().get(2);
            statusLabel.setText(callType);

            Label nameLabel = (Label) outgoingCallUI.getChildren().get(1);
            nameLabel.setText("Calling " + currentReceiver + "...");
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
        try {
            String receiver = isOutgoingCall ? currentReceiver : currentCaller;
            ChatMessage endMsg = new ChatMessage(client.getUsername(), receiver, "CALL_ENDED");
            chatClient.sendMessage(endMsg);
        } catch (Exception e) {
            System.err.println("Error ending call: " + e.getMessage());
        }
        stopAllCallComponents();
        hideCallUI();
        resetCallState();
    }

    private void stopAllCallComponents() {
        ClientUDP.stop();
        if (audioThread != null) {
            audioThread.interrupt();
            audioThread = null;
        }
        if (videoSenderThread != null) {
            VideoSender.stop();
            videoSenderThread.interrupt();
            videoSenderThread = null;
        }

        if (videoReceiverThread != null) {
            VideoReceiver.stop();
            videoReceiverThread.interrupt();
            videoReceiverThread = null;
        }
    }

    @FXML
    private void toggleMute(ActionEvent event) {
        isMuted = !isMuted;
        ClientUDP.setMuted(isMuted);
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
                ClientUDP.start(IpNiyeMaramari.serverip, 9806);
            } catch (Exception e) {
                System.err.println("Audio call error: " + e.getMessage());
            }
        });
        audioThread.setDaemon(true);
        audioThread.start();
    }

    private void startVideoCall() {
        videoSenderThread = new Thread(() -> {
            try {
                VideoSender.start(IpNiyeMaramari.friendip, 9807);
            } catch (Exception e) {
                System.err.println("Video sender error: " + e.getMessage());
            }
        });
        videoSenderThread.start();
        videoReceiverThread = new Thread(() -> {
            try {
                VideoReceiver.start(9808, (image) -> {
                    if (remoteVideoView != null) {
                        remoteVideoView.setImage(image);
                    }
                });
            } catch (Exception e) {
                System.err.println("Video receiver error: " + e.getMessage());
            }
        });
        videoReceiverThread.start();
        startAudioCall();
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

        if (isVideoEnabled) {
            if (!VideoSender.isRunning()) {
                videoSenderThread = new Thread(() -> {
                    try {
                        VideoSender.start(IpNiyeMaramari.friendip, 9807);
                    } catch (Exception e) {
                        System.err.println("Video sender error: " + e.getMessage());
                    }
                });
                videoSenderThread.start();
            }
            localVideoView.setVisible(true);
        } else {
            VideoSender.stop();
            localVideoView.setVisible(false);
        }
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
        stopAllCallComponents();
        muteBtn.setText("🔊");
        muteBtn.setStyle("-fx-background-color: #666; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 20px; " +
                "-fx-padding: 10; " +
                "-fx-background-radius: 25; " +
                "-fx-min-width: 50px; " +
                "-fx-min-height: 50px; " +
                "-fx-cursor: hand;");

        videoToggleBtn.setText("📹");
        callDurationLabel.setText("00:00");
        if (remoteVideoView != null) {
            remoteVideoView.setImage(null);
        }
        if (localVideoView != null) {
            localVideoView.setImage(null);
        }
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
            showOutgoingCallUI("Audio Call");
            ChatMessage callMsg = new ChatMessage(client.getUsername(), currentReceiver, "AUDIO_CALL_INITIATED");
            chatClient.sendMessage(callMsg);
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

            showOutgoingCallUI("Video Call");

            ChatMessage callMsg = new ChatMessage(client.getUsername(), currentReceiver, "VIDEO_CALL_INITIATED");
            chatClient.sendMessage(callMsg);

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
        if (content.isEmpty()) return;

        if (isGroupChatMode && getCurrentSelectedGroup() != null) {
            msgField.clear();
            sendGroupMessageFromMainChat(content);
        }
        else if (!content.isEmpty() && currentReceiver != null) {
            ChatMessage msg = new ChatMessage(client.getUsername(), currentReceiver, content);
            chatClient.sendMessage(msg);
            msgField.clear();
            showMessageInChat(msg);
            DatabaseHandler.getInstance().saveChatMessageAsync(msg, () -> {
                System.out.println("Message saved to database");
            });
        }
    }

    private void onSendGemini() {
        String content = geminimsg.getText().trim();
        System.out.println(content);
        if (!content.isEmpty()) {
            System.out.println("Showing in gemini box");
            showChatWithGemini(content, true);
            geminimsg.clear();
            System.out.println("Asking gemini");
            geminiQuery(content);
        }
    }

    private void showChatWithGemini(String content, boolean isClient) {
        if (isClient) {
            System.out.println("Client");
            HBox hBox = new HBox();
            hBox.setPadding(new Insets(3));
            hBox.setAlignment(Pos.CENTER_RIGHT);

            Label label = new Label(content);
            label.setWrapText(true);
            label.maxWidthProperty().bind(chatScroll.widthProperty().multiply(0.6));
            label.setMaxHeight(Double.MAX_VALUE);
            label.setPadding(new Insets(12));
            label.setFont(Font.font("Segoe UI Emoji", 12));
            label.setTextFill(Color.BLACK);
            label.setStyle("-fx-background-color: #128C7E; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 1, 1); " +
                    "-fx-padding: 12; " +
                    "-fx-background-radius: 18; " +
                    "-fx-border-radius: 18; " +
                    "-fx-border-color: rgba(255,255,255,0.3); " +
                    "-fx-border-width: 2;");
            hBox.getChildren().add(label);
            geminibox.getChildren().add(hBox);
            geminiscrollpane.setVvalue(1.0);
        } else {
            System.out.println("Gemini");
            HBox hBox = new HBox();
            hBox.setPadding(new Insets(3));
            hBox.setAlignment(Pos.CENTER_LEFT);

            Label label = new Label(content);
            label.setWrapText(true);
            label.maxWidthProperty().bind(chatScroll.widthProperty().multiply(0.6));
            label.setMaxHeight(Double.MAX_VALUE);
            label.setPadding(new Insets(12));
            label.setFont(Font.font("Segoe UI Emoji", 12));
            label.setTextFill(Color.BLACK);
            label.setStyle("-fx-background-color: white; " +
                    "-fx-text-fill: #2c3e50; " +
                    "-fx-font-weight: bold; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 4, 0, 2, 2); " +
                    "-fx-padding: 12; " +
                    "-fx-background-radius: 18; " +
                    "-fx-border-radius: 18; " +
                    "-fx-border-color: #34495e; " +
                    "-fx-border-width: 2;");
            hBox.getChildren().add(label);
            geminibox.getChildren().add(hBox);
            geminiscrollpane.setVvalue(1.0);
        }
    }

    @FXML
    private void onSendFile() {
        if(isGroupChatMode){
            sendGroupFile();
            return;
        }
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
                DatabaseHandler.getInstance().saveChatMessageAsync(msg, null);

            } catch (Exception e) {
                System.out.println("Error sending file: " + e.getMessage());
            }
        }
    }

    private void updateSpecificUserStatus(String username, boolean isOnline) {
        for (Node node : friendslist.getChildren()) {
            if (node instanceof HBox) {
                HBox card = (HBox) node;
                VBox userInfo = (VBox) card.getChildren().get(1);
                Label nameLabel = (Label) userInfo.getChildren().get(0);

                if (nameLabel.getText().equals(username)) {
                    Label statusLabel = (Label) userInfo.getChildren().get(1);
                    Circle indicator = (Circle) card.getChildren().get(2);

                    if (isOnline) {
                        statusLabel.setText("Online");
                        statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px;");
                        indicator.setFill(Color.web("#4CAF50"));
                    } else {
                        statusLabel.setText("Offline");
                        statusLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
                        indicator.setFill(Color.web("#888888"));
                        Task<Client> fetchUserTask = new Task<Client>() {
                            @Override
                            protected Client call() throws Exception {
                                return DatabaseHandler.getInstance().loadUsers().stream()
                                        .filter(c -> c.getUsername().equals(username))
                                        .findFirst().orElse(null);
                            }
                        };
                        fetchUserTask.setOnSucceeded(e -> {
                            Client user = fetchUserTask.getValue();
                            if (user != null && user.getLastSeen() != null) {
                                statusLabel.setText("Last seen: " + formatLastSeen(user.getLastSeen()));
                            }
                        });
                        new Thread(fetchUserTask).start();
                    }
                    break;
                }
            }
        }
    }
    private void handleIncomingMsg(ChatMessage msg) {
        if (msg.getSender().equals("SYSTEM")) {
            if (msg.getContent().startsWith("USER_ONLINE:") || msg.getContent().startsWith("USER_OFFLINE:")) {
                String username = msg.getContent().substring(msg.getContent().indexOf(":") + 1);
                Platform.runLater(() -> updateSpecificUserStatus(username, msg.getContent().startsWith("USER_ONLINE:")));
            }
            else if (msg.getContent().startsWith("NEW_USER_SIGNUP:")) {
                String newUsername = msg.getContent().substring("NEW_USER_SIGNUP:".length());
                Platform.runLater(() -> {
                    loadFriendsAsync();
                });
            } else if (msg.getContent().startsWith("GROUP_INVITATION:")) {
                String groupId = msg.getContent().substring("GROUP_INVITATION:".length());
                Platform.runLater(() -> handleGroupInvitation(groupId));
            }
            return;
        }
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
        if (msg.getReceiver().startsWith("GROUP:")) {
            handleGroupMessage(msg);
            return;
        }
        if (!msg.getSender().equals(client.getUsername())) {
            if (!isGroupChatMode && currentReceiver != null && (msg.getSender().equals(currentReceiver) || msg.getReceiver().equals(currentReceiver))) {
                Platform.runLater(() -> showMessageInChat(msg));
            }
        }
    }

    private void handleGroupInvitation(String groupId) {
        System.out.println("Handling group invitation for group ID: " + groupId);

        Task<GroupChat> loadGroupTask = new Task<GroupChat>() {
            @Override
            protected GroupChat call() throws Exception {
                boolean joined = DatabaseHandler.getInstance().addMemberToGroup(groupId, client.getUsername());
                if (!joined) {
                    System.err.println("Failed to join group: " + groupId);
                    return null;
                }
                return DatabaseHandler.getInstance().getGroupById(groupId);
            }
        };
        loadGroupTask.setOnSucceeded(e -> {
            GroupChat group = loadGroupTask.getValue();
            if (group != null) {
                Platform.runLater(() -> {
                    System.out.println("Successfully joined group: " + group.getGroupName());
                    try {
                        ChatMessage registerMsg = new ChatMessage(client.getUsername(),
                                "GROUP:" + groupId, "JOIN_GROUP");
                        chatClient.sendMessage(registerMsg);
                        System.out.println("Registered for group messages: " + group.getGroupName());
                    } catch (Exception ex) {
                        System.err.println("Error registering for group messages: " + ex.getMessage());
                    }
                    loadFriendsAsync();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Group Invitation");
                    alert.setHeaderText("You've been added to a group!");
                    alert.setContentText("You have been added to the group: " + group.getGroupName());
                    ButtonType switchToGroupButton = new ButtonType("Open Group");
                    ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                    alert.getButtonTypes().setAll(switchToGroupButton, okButton);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == switchToGroupButton) {
                        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), event -> {
                            HBox groupCard = findGroupCardByName(group.getGroupName());
                            if (groupCard != null) {
                                switchToGroupChat(group);
                                updateCardSelection(groupCard);
                            }
                        }));
                        timeline.play();
                    }
                });
            } else {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to join the group. Please try again.");
                });
            }
        });
        loadGroupTask.setOnFailed(e -> {
            System.err.println("Failed to handle group invitation: " + loadGroupTask.getException().getMessage());
            Platform.runLater(() -> {
                showAlert("Error", "Failed to join the group: " + loadGroupTask.getException().getMessage());
            });
        });

        new Thread(loadGroupTask).start();
    }

    @FXML
    private void showGroupInfo(ActionEvent event) {
        GroupChat currentGroup = getCurrentSelectedGroup();
        if (currentGroup == null) return;
        Dialog<Void> infoDialog = new Dialog<>();
        infoDialog.setTitle("Group Information");
        infoDialog.setHeaderText(currentGroup.getGroupName());
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        Label membersLabel = new Label("Members (" + currentGroup.getMemberCount() + "):");
        ListView<String> membersList = new ListView<>();
        Task<List<String>> loadMembersTask = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                return DatabaseHandler.getInstance().loadGroupMembers(currentGroup.getGroupId());
            }
        };

        loadMembersTask.setOnSucceeded(e -> {
            membersList.getItems().addAll(loadMembersTask.getValue());
        });
        new Thread(loadMembersTask).start();
        content.getChildren().addAll(membersLabel, membersList);
        infoDialog.getDialogPane().setContent(content);
        infoDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        infoDialog.showAndWait();
    }

    private void handleGroupMessage(ChatMessage msg) {
        String groupId = msg.getReceiver().substring(6);
        if (msg.getContent().equals("JOIN_GROUP")) {
            if (!msg.getSender().equals(client.getUsername())) {
                Platform.runLater(() -> {
                    Task<Boolean> checkMembershipTask = new Task<Boolean>() {
                        @Override
                        protected Boolean call() throws Exception {
                            List<String> members = DatabaseHandler.getInstance().loadGroupMembers(groupId);
                            return members.contains(client.getUsername());
                        }
                    };
                    checkMembershipTask.setOnSucceeded(e -> {
                        if (checkMembershipTask.getValue()) {
                            loadGroupsForFriendsList();
                        }
                    });
                    new Thread(checkMembershipTask).start();
                });
            }
            return;
        }
        if (msg.getContent().startsWith("MEMBER_JOINED:") || msg.getContent().startsWith("MEMBER_LEFT:")) {
            handleGroupNotification(msg, groupId);
            return;
        }

        if (!msg.getSender().equals(client.getUsername())) {
            GroupMessage groupMsg = new GroupMessage(groupId, msg.getSender(), msg.getContent());
            if (msg.isFile()) {
                groupMsg = new GroupMessage(groupId, msg.getSender(), msg.getFileName(), msg.getFileData());
            }
            if (isGroupChatMode &&
                    getCurrentSelectedGroup() != null &&
                    getCurrentSelectedGroup().getGroupId().equals(groupId)) {
                GroupMessage finalGroupMsg = groupMsg;
                Platform.runLater(() -> showGroupMessageInMainChat(finalGroupMsg));
            }
        }
    }

    private HBox findGroupCardByName(String groupName) {
        for (Node node : friendslist.getChildren()) {
            if (node instanceof HBox) {
                HBox card = (HBox) node;
                if (card.getChildren().size() >= 2) {
                    VBox groupInfo = (VBox) card.getChildren().get(1);
                    if (groupInfo.getChildren().size() >= 1) {
                        Label nameLabel = (Label) groupInfo.getChildren().get(0);
                        if (nameLabel.getText().equals(groupName)) {
                            return card;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void handleGroupNotification(ChatMessage msg, String groupId) {
        if (msg.getSender().equals(client.getUsername())) {
            return;
        }
        Platform.runLater(() -> {
            GroupMessage systemMsg;
            if (msg.getContent().startsWith("MEMBER_JOINED:")) {
                String addedMembers = msg.getContent().substring("MEMBER_JOINED:".length());
                systemMsg = new GroupMessage(groupId,
                        msg.getSender() + " added " + addedMembers + " to the group",
                        GroupMessage.MessageType.MEMBER_JOINED);
            } else if (msg.getContent().startsWith("MEMBER_LEFT:")) {
                String leftMember = msg.getContent().substring("MEMBER_LEFT:".length());
                systemMsg = new GroupMessage(groupId,
                        leftMember + " left the group",
                        GroupMessage.MessageType.MEMBER_LEFT);
            } else {
                return;
            }
            DatabaseHandler.getInstance().saveGroupMessageAsync(systemMsg, null);
            if (isGroupChatMode &&
                    getCurrentSelectedGroup() != null &&
                    getCurrentSelectedGroup().getGroupId().equals(groupId)) {
                showGroupMessageInMainChat(systemMsg);
            }
            loadFriendsAsync();
        });
    }

    @FXML
    private void sendGroupFile() {
        if (!isGroupChatMode) return;
        GroupChat currentGroup = getCurrentSelectedGroup();
        if (currentGroup == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File to Send to Group");
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                byte[] fileData = readAllBytes(selectedFile.toPath());
                GroupMessage groupMsg = new GroupMessage(currentGroup.getGroupId(),
                        client.getUsername(), selectedFile.getName(), fileData);

                showGroupMessageInMainChat(groupMsg);
                DatabaseHandler.getInstance().saveGroupMessageAsync(groupMsg, null);
                String groupReceiver = "GROUP:" + currentGroup.getGroupId();
                ChatMessage serverMsg = new ChatMessage(client.getUsername(), groupReceiver,
                        "File: " + selectedFile.getName(), fileData);
                chatClient.sendMessage(serverMsg);

            } catch (Exception e) {
                System.out.println("Error sending group file: " + e.getMessage());
            }
        }
    }

    private void initializeUserGroups() {
        if (client == null) return;
        Task<List<GroupChat>> loadUserGroupsTask = new Task<List<GroupChat>>() {
            @Override
            protected List<GroupChat> call() throws Exception {
                return DatabaseHandler.getInstance().loadUserGroups(client.getUsername());
            }
        };
        new Thread(loadUserGroupsTask).start();
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
        label.setPadding(new Insets(15));
        if (msg.getSender().equals(client.getUsername())) {
            label.setStyle("-fx-background-color: linear-gradient(to bottom, #2d1b69, #1a1a2e); " +
                    "-fx-text-fill: #ffd700; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-family: 'Cinzel', 'Times New Roman', serif; " +
                    "-fx-effect: dropshadow(gaussian, #2d1b69, 8, 0.7, 2, 4); " +
                    "-fx-padding: 15; " +
                    "-fx-background-radius: 25; " +
                    "-fx-border-radius: 25; " +
                    "-fx-border-color: #6b5b73; " +
                    "-fx-border-width: 2;");
        } else {
            label.setStyle("-fx-background-color: linear-gradient(to bottom, #3d2d5d, #2a1f3d); " +
                    "-fx-text-fill: #e6ddd4; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-family: 'Cinzel', 'Times New Roman', serif; " +
                    "-fx-effect: dropshadow(gaussian, #1a1a2e, 6, 0.6, 3, 4); " +
                    "-fx-padding: 15; " +
                    "-fx-background-radius: 25; " +
                    "-fx-border-radius: 25; " +
                    "-fx-border-color: #6b5b73; " +
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
        } else if (btn.equals(infobtn)) {
            Animations.FadeTransition(geminiPane, false);
            Animations.FadeTransition(infoPane, true);
        } else if (btn.equals(geminiBTN)) {
            Animations.FadeTransition(infoPane, false);
            Animations.FadeTransition(geminiPane, true);
        } else if (btn.equals(sendEmoji)) {
            emojipane_test.setVisible(true);
        } else if (btn.equals(closeEmojiPicker)) {
            emojipane_test.setVisible(false);
        } else if (btn.equals(voiceToText)) {
            if (!recording) {
                startRecording();
            } else {
                stopRecordingAndTranscribe();
            }
        }
    }

    public void startRecording() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            AudioInputStream ais = new AudioInputStream(microphone);
            recording = true;

            recordingThread = new Thread(() -> {
                try {
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, audioFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            recordingThread.start();
            System.out.println("Recording started...");

        } catch (Exception e) {
            System.out.println("Error starting recording: " + e);
        }
    }

    public void stopRecordingAndTranscribe() {
        try {
            recording = false;
            microphone.stop();
            microphone.close();
            recordingThread.join(); // ensure writing is finished

            System.out.println("Recording stopped. Uploading...");
            getKey();
            System.out.println(api);

            AssemblyAI assemblyAI = AssemblyAI.builder().apiKey(api).build();
            String url = uploadFile(audioFile.getAbsolutePath(), api);
            System.out.println("Audio URL: " + url);

            Transcript transcript = assemblyAI.transcripts().transcribe(url);
            System.out.println("Transcription: " + transcript.getText());

            Platform.runLater(() -> msgField.appendText(String.valueOf(transcript.getText()).substring(9, String.valueOf(transcript.getText()).indexOf(']'))));

        } catch (Exception e) {
            System.out.println("Error stopping recording/transcribing: " + e);
        }
    }

    private Image loadProfileImage(String profilePicturePath) {
        if (profilePicturePath == null || profilePicturePath.isBlank()) {
            return loadDefaultProfileImage();
        }
        if (profilePicturePath.startsWith("profile_pictures/")) {
            String username = profilePicturePath.substring("profile_pictures/".length());
            Image img = DatabaseHandler.getInstance().getProfilePicture(username);
            if (img != null) {
                return img;
            } else {
                System.out.println("No Firebase image found for user " + username + "; using default.");
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
        loadUserProfileImageAsync();
        userIdLabel.setText(client.getUsername());
        setupBackgroundAndUI();
        initializeChatClientAsync();
        loadFriendsAsync();
        initializeUserGroups();
        initializeGameManager();
    }

    private void loadUserProfileImageAsync() {
        Task<Image> userImageTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                return loadProfileImage(client.getProfilePicturePath());
            }
        };

        userImageTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                Image userProfileImage = userImageTask.getValue();
                userImage.setImage(userProfileImage);
                setCircularImage(userImage, userImageClip, userProfileImage);
            });
        });

        new Thread(userImageTask).start();
    }

    private void setupBackgroundAndUI() {
        ImageView backgroundImageView = new ImageView();
        backgroundImageView.setImage(new Image(getClass().getResource("/com/example/owlpost_2_0/Images/LoginForm/slytherin.gif").toExternalForm()));
        backgroundImageView.setFitWidth(319);
        backgroundImageView.setFitHeight(630);
        backgroundImageView.setLayoutY(100);
        backgroundImageView.setPreserveRatio(false);

        leftbase.getChildren().add(0, backgroundImageView);
        leftpane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-background-radius: 10; -fx-padding: 10;" +
                "-fx-background-radius: 15; " +
                "-fx-border-color: #8b4513; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 15; " +
                "-fx-padding: 15;");

        friendslist.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(44, 24, 16, 0.8), rgba(26, 15, 8, 0.9)); -fx-background: transparent; " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: #8b4513; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 12; " +
                "-fx-padding: 12; " +
                "-fx-effect: innershadow(gaussian, #000000, 8, 0.4, 0, 0);");
        UpdateBG();
        setUpBackgroundTimer();
    }

    private void initializeChatClientAsync() {
        Task<Void> chatClientTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                chatClient = new ChatClient(IpNiyeMaramari.serverip, client.getUsername());
                chatClient.listenForMsg(ChatRoomController.this::handleIncomingMsg);
                return null;
            }
        };
        chatClientTask.setOnFailed(e -> {
            System.out.println("Error connecting to server: " + chatClientTask.getException().getMessage());
        });

        new Thread(chatClientTask).start();
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
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.3);");
        friendslist.getChildren().add(separator);
        Label groupsLabel = new Label("Groups");
        groupsLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 0;");
        friendslist.getChildren().add(groupsLabel);
        loadGroupsForFriendsList();
    }

    private void loadGroupsForFriendsList() {
        Task<List<GroupChat>> loadGroupsTask = new Task<List<GroupChat>>() {
            @Override
            protected List<GroupChat> call() throws Exception {
                return DatabaseHandler.getInstance().loadUserGroups(client.getUsername());
            }
        };
        loadGroupsTask.setOnSucceeded(e -> {
            List<GroupChat> groups = loadGroupsTask.getValue();
            Platform.runLater(() -> {
                List<Node> toRemove = new ArrayList<>();
                boolean foundSeparator = false;
                for (Node node : friendslist.getChildren()) {
                    if (node instanceof Separator) {
                        foundSeparator = true;
                        continue;
                    }
                    if (foundSeparator && !(node instanceof Label && ((Label) node).getText().equals("Groups"))) {
                        toRemove.add(node);
                    }
                }
                friendslist.getChildren().removeAll(toRemove);
                String selectedGroupName = null;
                if (isGroupChatMode && currentSelectedGroup != null) {
                    selectedGroupName = currentSelectedGroup.getGroupName();
                }
                for (GroupChat group : groups) {
                    HBox groupCard = createGroupCard(group);
                    friendslist.getChildren().add(groupCard);
                    if (selectedGroupName != null && group.getGroupName().equals(selectedGroupName)) {
                        updateCardSelection(groupCard);
                    }
                }
            });
        });
        loadGroupsTask.setOnFailed(e -> {
            System.err.println("Failed to load groups: " + loadGroupsTask.getException().getMessage());
        });
        new Thread(loadGroupsTask).start();
    }

    private HBox createGroupCard(GroupChat group) {
        HBox card = new HBox(10);
        card.setPrefWidth(300);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(5));
        ImageView img = new ImageView();
        img.setFitWidth(40);
        img.setFitHeight(48);
        Circle clip = new Circle(24, 24, 24);
        img.setClip(clip);
        img.setImage(loadDefaultGroupImage());
        VBox groupInfo = new VBox(2);
        Label name = new Label(group.getGroupName());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label memberCount = new Label(group.getMemberCount() + " members");
        memberCount.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px;");
        groupInfo.getChildren().addAll(name, memberCount);
        Circle groupIndicator = new Circle(6);
        groupIndicator.setFill(Color.web("#FF9800"));
        card.getChildren().addAll(img, groupInfo, groupIndicator);
        card.setOnMouseClicked(e -> {
            switchToGroupChat(group);
            updateCardSelection(card);
        });
        styleGroupCard(card);
        return card;
    }

    private void updateCardSelection(HBox selectedCard) {
        try {
            for (Node node : friendslist.getChildren()) {
                if (node instanceof HBox) {
                    HBox card = (HBox) node;
                    card.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                            "-fx-background-radius: 10; " +
                            "-fx-cursor: hand;");
                }
            }
            if (selectedCard != null) {
                selectedCard.setStyle("-fx-background-color: rgba(255,255,255,0.3); " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;");
            }
        } catch (Exception e) {
            System.err.println("Error updating card selection: " + e.getMessage());
        }
    }

    private void styleGroupCard(HBox card) {
        card.setOnMouseEntered(e -> {
            if (!isCardCurrentlySelected(card)) {
                card.setStyle("-fx-background-color: rgba(44, 24, 16, 0.8); " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-color: #d4af37; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, #d4af37, 8, 0.6, 0, 0);");
            }
        });
        card.setOnMouseExited(e -> {
            if (!isCardCurrentlySelected(card)) {
                card.setStyle("-fx-background-color: rgba(13, 7, 7, 0.6); " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-color: rgba(139, 69, 19, 0.5); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12;");
            }
        });
    }
    private boolean isCardCurrentlySelected(HBox card) {
        try {
            if (!isGroupChatMode || card.getChildren().size() < 2) {
                return false;
            }
            String cardStyle = card.getStyle();
            return cardStyle != null && cardStyle.contains("rgba(255,255,255,0.3)");
        } catch (Exception e) {
            System.err.println("Error checking card selection: " + e.getMessage());
            return false;
        }
    }
    private void switchToGroupChat(GroupChat group) {
        isGroupChatMode = true;
        currentSelectedGroup = group;
        currentReceiver = null;
        clientIdLabel.setText(group.getGroupName());
        setCircularImage(clientImage, clientImageClip, loadDefaultGroupImage());
        msgbox.getChildren().clear();
        loadGroupChatHistoryInMainChat(group.getGroupId());
        showGroupButtons();
        removeGameButtonFromClientCard();
    }
    private void loadGroupChatHistoryInMainChat(String groupId) {
        Task<List<GroupMessage>> loadChatTask = new Task<List<GroupMessage>>() {
            @Override
            protected List<GroupMessage> call() throws Exception {
                return DatabaseHandler.getInstance().loadGroupChatHistory(groupId);
            }
        };

        loadChatTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                List<GroupMessage> messages = loadChatTask.getValue();
                if (messages != null && !messages.isEmpty()) {
                    for (GroupMessage msg : messages) {
                        showGroupMessageInMainChat(msg);
                    }
                }
            });
        });

        new Thread(loadChatTask).start();
    }

    private Node buildSystemMessageForMainChat(GroupMessage msg) {
        Label systemLabel = new Label(msg.getContent());
        systemLabel.setStyle("-fx-text-fill: #ffffff; " +
                "-fx-font-style: italic; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-color: rgba(50, 50, 50, 0.9); " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 12 16; " +
                "-fx-border-color: rgba(255, 255, 255, 0.3); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 4, 0.5, 2, 2);");
        systemLabel.setWrapText(true);
        return systemLabel;
    }

    private Node buildGroupTextForMainChat(GroupMessage msg) {
        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(12));
        bubble.maxWidthProperty().bind(chatScroll.widthProperty().multiply(0.6));
        Label contentLabel = new Label(msg.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: " +
                (msg.getSenderUsername().equals(client.getUsername()) ? "#ffd700" : "white") +
                "; -fx-font-weight: bold;");

        Label timeLabel = new Label(msg.getFormattedTimestamp());
        timeLabel.setStyle("-fx-text-fill: " +
                (msg.getSenderUsername().equals(client.getUsername()) ? "#cccccc" : "#666666") +
                "; -fx-font-size: 10px;");

        bubble.getChildren().addAll(contentLabel, timeLabel);

        if (msg.getSenderUsername().equals(client.getUsername())) {
            bubble.setStyle("-fx-background-color: linear-gradient(to bottom, #2d1b69, #1a1a2e); " +
                    "-fx-text-fill: #ffd700; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-family: 'Cinzel', 'Times New Roman', serif; " +
                    "-fx-effect: dropshadow(gaussian, #2d1b69, 8, 0.7, 2, 4); " +
                    "-fx-padding: 15; " +
                    "-fx-background-radius: 25; " +
                    "-fx-border-radius: 25; " +
                    "-fx-border-color: #6b5b73; " +
                    "-fx-border-width: 2;");
        } else {
            bubble.setStyle("-fx-background-color: linear-gradient(to bottom, #3d2d5d, #2a1f3d); " +
                    "-fx-text-fill: #e6ddd4; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-family: 'Cinzel', 'Times New Roman', serif; " +
                    "-fx-effect: dropshadow(gaussian, #1a1a2e, 6, 0.6, 3, 4); " +
                    "-fx-padding: 15; " +
                    "-fx-background-radius: 25; " +
                    "-fx-border-radius: 25; " +
                    "-fx-border-color: #6b5b73; " +
                    "-fx-border-width: 2;");
        }

        return bubble;
    }

    private Node buildGroupFileForMainChat(GroupMessage msg) {
        VBox fileContainer = new VBox(5);
        fileContainer.setPadding(new Insets(8));
        fileContainer.maxWidthProperty().bind(chatScroll.widthProperty().multiply(0.6));
        if (!msg.getSenderUsername().equals(client.getUsername())) {
            Label senderLabel = new Label(msg.getSenderUsername());
            senderLabel.setStyle("-fx-text-fill: #2980b9; " +
                    "-fx-font-size: 10px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 0 0 3 0;");
            fileContainer.getChildren().add(senderLabel);
        }

        Button downloadBtn = new Button("📎 " + msg.getFileName());
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
            File saveLocation = chooser.showSaveDialog(null);
            if (saveLocation != null) {
                try {
                    java.nio.file.Files.write(saveLocation.toPath(), msg.getFileData());
                    showAlert("Success", "File saved successfully!");
                } catch (Exception ex) {
                    System.out.println("Error saving file: " + ex.getMessage());
                    showAlert("Error", "Failed to save file: " + ex.getMessage());
                }
            }
        });

        Label timeLabel = new Label(msg.getFormattedTimestamp());
        timeLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");

        fileContainer.getChildren().addAll(downloadBtn, timeLabel);
        if (msg.getSenderUsername().equals(client.getUsername())) {
            fileContainer.setStyle("-fx-background-color: rgba(18, 140, 126, 0.1); " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-radius: 15; " +
                    "-fx-border-color: #128C7E; " +
                    "-fx-border-width: 1;");
        } else {
            fileContainer.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-radius: 15; " +
                    "-fx-border-color: #bdc3c7; " +
                    "-fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 1, 1);");
        }

        return fileContainer;
    }

    private Node buildGroupImageForMainChat(GroupMessage msg) {
        VBox imageContainer = new VBox(5);
        imageContainer.setPadding(new Insets(8));
        imageContainer.maxWidthProperty().bind(chatScroll.widthProperty().multiply(0.7));
        if (!msg.getSenderUsername().equals(client.getUsername())) {
            Label senderLabel = new Label(msg.getSenderUsername());
            senderLabel.setStyle("-fx-text-fill: #2980b9; " +
                    "-fx-font-size: 10px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 0 0 3 0;");
            imageContainer.getChildren().add(senderLabel);
        }

        Image img = new Image(new java.io.ByteArrayInputStream(msg.getFileData()));
        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 1, 1);");

        Label timeLabel = new Label(msg.getFormattedTimestamp());
        timeLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");

        imageContainer.getChildren().addAll(imageView, timeLabel);
        if (msg.getSenderUsername().equals(client.getUsername())) {
            imageContainer.setStyle("-fx-background-color: rgba(18, 140, 126, 0.1); " +
                    "-fx-background-radius: 12; " +
                    "-fx-padding: 5;");
        } else {
            imageContainer.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); " +
                    "-fx-background-radius: 12; " +
                    "-fx-padding: 5; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 1, 1);");
        }

        imageView.setOnMouseClicked(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save image");
            fileChooser.setInitialFileName(msg.getFileName());
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try {
                    java.nio.file.Files.write(file.toPath(), msg.getFileData());
                    showAlert("Success", "Image saved successfully!");
                } catch (Exception ex) {
                    System.out.println("Error saving image: " + ex.getMessage());
                    showAlert("Error", "Failed to save image: " + ex.getMessage());
                }
            }
        });

        return imageContainer;
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showGroupMessageInMainChat(GroupMessage groupMsg) {
        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(3));

        VBox messageContainer = new VBox(5);
        if (!groupMsg.getSenderUsername().equals(client.getUsername()) && !groupMsg.isSystemMessage()) {
            Label senderLabel = new Label(groupMsg.getSenderUsername());
            senderLabel.setStyle("-fx-text-fill: #3498db; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 0 0 2 0;");
            messageContainer.getChildren().add(senderLabel);
        }

        Node messageNode;
        if (groupMsg.isSystemMessage()) {
            messageNode = buildSystemMessageForMainChat(groupMsg);
            messageBox.setAlignment(Pos.CENTER);
        } else if (groupMsg.isFile()) {
            if (groupMsg.isImageMessage()) {
                messageNode = buildGroupImageForMainChat(groupMsg);
            } else {
                messageNode = buildGroupFileForMainChat(groupMsg);
            }
            messageBox.setAlignment(groupMsg.getSenderUsername().equals(client.getUsername()) ?
                    Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        } else {
            messageNode = buildGroupTextForMainChat(groupMsg);
            messageBox.setAlignment(groupMsg.getSenderUsername().equals(client.getUsername()) ?
                    Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        }

        messageContainer.getChildren().add(messageNode);
        messageBox.getChildren().add(messageContainer);
        msgbox.getChildren().add(messageBox);
        chatScroll.setVvalue(1.0);
    }

    private Image loadDefaultGroupImage() {
        try {
            URL url = getClass().getResource("/com/example/owlpost_2_0/Images/default-group.png");
            if (url == null) {
                url = getClass().getResource("/com/example/owlpost_2_0/Images/default-profile.png");
            }
            if (url == null) {
                return createDefaultGroupIcon();
            }
            return new Image(url.toExternalForm());
        } catch (Exception e) {
            System.out.println("Error loading group image: " + e.getMessage());
            return createDefaultGroupIcon();
        }
    }

    private Image createDefaultGroupIcon() {
        return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==");
    }

    private void switchToDirectChat(Client client) {
        hideGroupButtons();
        isGroupChatMode = false;
        currentSelectedGroup = null;
        currentReceiver = client.getUsername();
        clientImage.setImage(loadProfileImage(client.getProfilePicturePath()));
        clientIdLabel.setText(client.getUsername());
        msgbox.getChildren().clear();
        loadChatHistory(this.client.getUsername(), currentReceiver);
        addGameButtonToClientCard();
    }

    private String formatLastSeen(Date lastSeen) {
        long diff = System.currentTimeMillis() - lastSeen.getTime();
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        if (hours < 24) return hours + "h ago";
        return days + "d ago";
    }

    private HBox createFriendCard(Client c) {
        HBox card = new HBox(10);
        card.setPrefWidth(300);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(5));

        ImageView img = new ImageView();
        img.setFitWidth(40);
        img.setFitHeight(48);
        Circle clip = new Circle(24, 24, 24);
        img.setClip(clip);
        img.setImage(loadDefaultProfileImage());
        loadProfileImageAsync(c.getProfilePicturePath(), img);

        VBox userInfo = new VBox(2);
        Label name = new Label(c.getUsername());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label status = new Label();
        if (c.isOnline()) {
            status.setText("Online");
            status.setStyle("-fx-text-fill: #00ff41; " +
                    "-fx-font-size: 12px; " +
                    "-fx-font-family: 'Times New Roman', serif; " +
                    "-fx-effect: dropshadow(gaussian, #00ff41, 3, 0.5, 0, 0);");
        } else {
            if (c.getLastSeen() != null) {
                status.setText("Last seen: " + formatLastSeen(c.getLastSeen()));
            } else {
                status.setText("Offline");
            }
            status.setStyle("-fx-text-fill: #666666; " +
                    "-fx-font-size: 12px; " +
                    "-fx-font-family: 'Times New Roman', serif;");
        }

        userInfo.getChildren().addAll(name, status);
        Circle onlineIndicator = new Circle(6);
        onlineIndicator.setFill(c.isOnline() ? Color.web("#4CAF50") : Color.web("#888888"));

        card.getChildren().addAll(img, userInfo, onlineIndicator);
        return card;
    }

    private void loadProfileImageAsync(String profilePicturePath, ImageView imageView) {
        Task<Image> imageTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                return loadProfileImage(profilePicturePath);
            }
        };

        imageTask.setOnSucceeded(e -> {
            Image loadedImage = imageTask.getValue();
            if (loadedImage != null) {
                Platform.runLater(() -> imageView.setImage(loadedImage));
            }
        });

        imageTask.setOnFailed(e -> {
            System.out.println("Failed to load profile image: " + imageTask.getException().getMessage());
        });

        new Thread(imageTask).start();
    }

    private void styleFriendCard(HBox card, Client c) {
        card.setStyle("-fx-background-color: linear-gradient(to right, rgba(44, 24, 16, 0.6), rgba(26, 15, 8, 0.7)); " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: rgba(139, 69, 19, 0.5); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 12; " +
                "-fx-cursor: hand;");

        card.setOnMouseClicked(e -> {
            switchToDirectChat(c);
            updateFriendCardStyle(card);
        });

        card.setOnMouseEntered(e -> {
            if (!c.getUsername().equals(currentReceiver)) {
                card.setStyle("-fx-background-color: linear-gradient(to right, rgba(61, 34, 26, 0.8), rgba(38, 22, 18, 0.9)); " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #d4af37; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, #d4af37, 4, 0.3, 0, 0);");
            }
        });
        card.setOnMouseExited(e -> {
            if (!c.getUsername().equals(currentReceiver)) {
                card.setStyle("-fx-background-color: linear-gradient(to right, rgba(44, 24, 16, 0.6), rgba(26, 15, 8, 0.7)); " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: rgba(139, 69, 19, 0.5); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-cursor: hand;");
            }
        });
    }

    private void updateFriendCardStyle(HBox selectedCard) {
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
        Label loadingLabel = new Label("Loading chat history...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-style: italic;");
        HBox loadingBox = new HBox(loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        msgbox.getChildren().add(loadingBox);

        Task<List<ChatMessage>> loadChatTask = new Task<List<ChatMessage>>() {
            @Override
            protected List<ChatMessage> call() throws Exception {
                return DatabaseHandler.getInstance().loadChatHistory(sender, receiver);
            }
        };

        loadChatTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                msgbox.getChildren().clear();
                List<ChatMessage> messages = loadChatTask.getValue();

                if (messages != null && !messages.isEmpty()) {
                    for (ChatMessage msg : messages) {
                        showMessageInChat(msg);
                    }
                } else {
                    Label noMessagesLabel = new Label("No messages yet. Start the conversation!");
                    noMessagesLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-style: italic;");
                    HBox noMsgBox = new HBox(noMessagesLabel);
                    noMsgBox.setAlignment(Pos.CENTER);
                    msgbox.getChildren().add(noMsgBox);
                }
            });
        });

        loadChatTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                msgbox.getChildren().clear();
                Label errorLabel = new Label("Failed to load chat history. Please try again.");
                errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-style: italic;");
                HBox errorBox = new HBox(errorLabel);
                errorBox.setAlignment(Pos.CENTER);
                msgbox.getChildren().add(errorBox);
            });
        });

        new Thread(loadChatTask).start();
    }

    private void loadFriendsAsync() {
        Task<List<Client>> loadFriendsTask = new Task<List<Client>>() {
            @Override
            protected List<Client> call() throws Exception {
                return DatabaseHandler.getInstance().loadUsers();
            }
        };

        loadFriendsTask.setOnSucceeded(e -> {
            List<Client> allClients = loadFriendsTask.getValue();
            Platform.runLater(() -> {
                String selectedUser = currentReceiver;
                GroupChat selectedGroup = currentSelectedGroup;

                loadFriends(allClients);
                if (selectedGroup != null && isGroupChatMode) {
                    HBox groupCard = findGroupCardByName(selectedGroup.getGroupName());
                    if (groupCard != null) {
                        updateCardSelection(groupCard);
                    }
                } else if (selectedUser != null && !isGroupChatMode) {
                    for (Node node : friendslist.getChildren()) {
                        if (node instanceof HBox) {
                            HBox card = (HBox) node;
                            if (card.getChildren().size() >= 2) {
                                VBox userInfo = (VBox) card.getChildren().get(1);
                                if (userInfo.getChildren().size() >= 1) {
                                    Label nameLabel = (Label) userInfo.getChildren().get(0);
                                    if (nameLabel.getText().equals(selectedUser)) {
                                        updateFriendCardStyle(card);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });
        });
        loadFriendsTask.setOnFailed(e -> {
            System.out.println("Failed to load friends: " + loadFriendsTask.getException().getMessage());
            Platform.runLater(() -> {
                Label errorLabel = new Label("Failed to load friends");
                errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
                friendslist.getChildren().add(errorLabel);
            });
        });
        new Thread(loadFriendsTask).start();
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

    public void sendGroupMessageFromMainChat(String content) {
        if (chatClient == null || client == null || !isGroupChatMode) {
            System.err.println("Cannot send group message: missing required components or not in group mode");
            return;
        }
        GroupChat currentGroup = getCurrentSelectedGroup();
        if (currentGroup == null) {
            System.err.println("No group selected");
            return;
        }

        try {
            GroupMessage groupMsg = new GroupMessage(currentGroup.getGroupId(),
                    client.getUsername(), content);
            showGroupMessageInMainChat(groupMsg);
            DatabaseHandler.getInstance().saveGroupMessageAsync(groupMsg, () -> {
                System.out.println("Group message saved to database");
            });

            String groupReceiver = "GROUP:" + currentGroup.getGroupId();
            ChatMessage serverMsg = new ChatMessage(client.getUsername(), groupReceiver, content);
            chatClient.sendMessage(serverMsg);

        } catch (Exception e) {
            System.err.println("Error sending group message: " + e.getMessage());
        }
    }
    private GroupChat currentSelectedGroup = null;
    private GroupChat getCurrentSelectedGroup() {
        if (isGroupChatMode && currentSelectedGroup != null) {
            return currentSelectedGroup;
        }

        try {
            for (Node node : friendslist.getChildren()) {
                if (node instanceof HBox) {
                    HBox card = (HBox) node;
                    if (card.getStyle() != null && card.getStyle().contains("rgba(255,255,255,0.3)")) {
                        if (card.getChildren().size() >= 2) {
                            VBox groupInfo = (VBox) card.getChildren().get(1);
                            if (groupInfo.getChildren().size() >= 1) {
                                Label nameLabel = (Label) groupInfo.getChildren().get(0);
                                String groupName = nameLabel.getText();
                                currentSelectedGroup = findGroupByName(groupName);
                                return currentSelectedGroup;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting current selected group: " + e.getMessage());
        }

        return null;
    }

    private GroupChat findGroupByName(String groupName) {
        try {
            List<GroupChat> userGroups = DatabaseHandler.getInstance().loadUserGroups(client.getUsername());
            return userGroups.stream()
                    .filter(group -> group.getGroupName().equals(groupName))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Error finding group by name: " + e.getMessage());
            return null;
        }
    }

    @FXML
    private void createGroup(ActionEvent event) {
        Audios.playSound("spell");
        showCreateGroupDialog();
    }

    private void setupCreateGroupDialog() {
        createGroupDialog = new Dialog<>();
        createGroupDialog.setTitle("Create New Group");
        createGroupDialog.setHeaderText("Create a new group chat");
        createGroupDialog.getDialogPane().setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1a0f08, #0d0707);" +
                        "-fx-text-fill: #d4af37;" +
                        "-fx-border-color: #8b4513;" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, #000000, 10, 0.6, 0, 5);"
        );

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setStyle("-fx-background-color: #2b2b2b;");
        groupNameField = new TextField();
        groupNameField.setPromptText("Enter group name");
        groupNameField.setPrefWidth(250);
        groupNameField.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(44, 24, 16, 0.9), rgba(26, 15, 8, 0.9));" +
                        "-fx-text-fill: #d4af37;" +
                        "-fx-prompt-text-fill: rgba(212, 175, 55, 0.6);" +
                        "-fx-border-color: #8b4513;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-family: 'Times New Roman', serif;" +
                        "-fx-font-weight: bold;"
        );
        groupDescriptionField = new TextArea();
        groupDescriptionField.setPromptText("Enter group description (optional)");
        groupDescriptionField.setPrefRowCount(3);
        groupDescriptionField.setPrefWidth(250);
        groupDescriptionField.setWrapText(true);
        groupDescriptionField.setStyle(
                "-fx-background-color: #3c3c3c;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-prompt-text-fill: #888888;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-control-inner-background: #3c3c3c;"
        );
        Label groupNameLabel = new Label("Group Name:");
        groupNameLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        Label descriptionLabel = new Label("Description:");
        descriptionLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        Label membersLabel = new Label("Add Members:");
        membersLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        membersLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        Label availableLabel = new Label("Available Users:");
        availableLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        Label selectedLabel = new Label("Selected Members:");
        selectedLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
        availableUsersListView = new ListView<>();
        availableUsersListView.setPrefHeight(180);
        availableUsersListView.setPrefWidth(200);
        availableUsersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        availableUsersListView.setStyle(
                "-fx-background-color: #3c3c3c;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-control-inner-background: #3c3c3c;"
        );

        selectedMembersListView = new ListView<>();
        selectedMembersListView.setPrefHeight(180);
        selectedMembersListView.setPrefWidth(200);
        selectedMembersListView.setStyle(
                "-fx-background-color: #3c3c3c;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-control-inner-background: #3c3c3c;"
        );
        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button addMemberBtn = new Button("Add →");
        addMemberBtn.setPrefWidth(80);
        addMemberBtn.setStyle(
                "-fx-background-color: #0078d4;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-cursor: hand;"
        );

        Button removeMemberBtn = new Button("← Remove");
        removeMemberBtn.setPrefWidth(80);
        removeMemberBtn.setStyle(
                "-fx-background-color: #0078d4;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-cursor: hand;"
        );
        addMemberBtn.setOnMouseEntered(e -> addMemberBtn.setStyle(
                "-fx-background-color: #106ebe;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-cursor: hand;"
        ));

        addMemberBtn.setOnMouseExited(e -> addMemberBtn.setStyle(
                "-fx-background-color: #0078d4;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-cursor: hand;"
        ));

        removeMemberBtn.setOnMouseEntered(e -> removeMemberBtn.setStyle(
                "-fx-background-color: #106ebe;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-cursor: hand;"
        ));

        removeMemberBtn.setOnMouseExited(e -> removeMemberBtn.setStyle(
                "-fx-background-color: #0078d4;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-cursor: hand;"
        ));

        addMemberBtn.setOnAction(e -> {
            List<String> selected = new ArrayList<>(availableUsersListView.getSelectionModel().getSelectedItems());
            for (String user : selected) {
                if (!selectedMembersListView.getItems().contains(user)) {
                    selectedMembersListView.getItems().add(user);
                }
            }
            availableUsersListView.getSelectionModel().clearSelection();
        });

        removeMemberBtn.setOnAction(e -> {
            List<String> selected = new ArrayList<>(selectedMembersListView.getSelectionModel().getSelectedItems());
            selectedMembersListView.getItems().removeAll(selected);
            selectedMembersListView.getSelectionModel().clearSelection();
        });

        buttonBox.getChildren().addAll(addMemberBtn, removeMemberBtn);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(200);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setMinWidth(200);
        grid.getColumnConstraints().addAll(col1, col2, col3);
        grid.add(groupNameLabel, 0, 0);
        grid.add(groupNameField, 1, 0, 2, 1);

        grid.add(descriptionLabel, 0, 1);
        grid.add(groupDescriptionField, 1, 1, 2, 1);

        grid.add(membersLabel, 0, 2, 3, 1);

        grid.add(availableLabel, 0, 3);
        grid.add(selectedLabel, 2, 3);

        grid.add(availableUsersListView, 0, 4);
        grid.add(buttonBox, 1, 4);
        grid.add(selectedMembersListView, 2, 4);

        createGroupDialog.getDialogPane().setContent(grid);
        ButtonType createButtonType = new ButtonType("Create Group", ButtonBar.ButtonData.OK_DONE);
        createGroupDialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        Node createButton = createGroupDialog.getDialogPane().lookupButton(createButtonType);
        Node cancelButton = createGroupDialog.getDialogPane().lookupButton(ButtonType.CANCEL);

        createButton.setDisable(true);
        createButton.setStyle(
                "-fx-background-color: #2a2a2a;" +
                        "-fx-text-fill: #666666;" +
                        "-fx-border-color: #444444;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-padding: 8px 16px;"
        );

        cancelButton.setStyle(
                "-fx-background-color: #3c3c3c;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-padding: 8px 16px;"
        );
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(
                "-fx-background-color: #4a4a4a;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-padding: 8px 16px;"
        ));

        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(
                "-fx-background-color: #3c3c3c;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 4px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-padding: 8px 16px;"
        ));
        groupNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isEmpty = newValue == null || newValue.trim().isEmpty();
            createButton.setDisable(isEmpty);
            if (!isEmpty) {
                createButton.setStyle(
                        "-fx-background-color: #0078d4;" +
                                "-fx-text-fill: #ffffff;" +
                                "-fx-border-color: #0078d4;" +
                                "-fx-border-width: 1px;" +
                                "-fx-border-radius: 4px;" +
                                "-fx-background-radius: 4px;" +
                                "-fx-padding: 8px 16px;"
                );
                createButton.setOnMouseEntered(e -> createButton.setStyle(
                        "-fx-background-color: #106ebe;" +
                                "-fx-text-fill: #ffffff;" +
                                "-fx-border-color: #106ebe;" +
                                "-fx-border-width: 1px;" +
                                "-fx-border-radius: 4px;" +
                                "-fx-background-radius: 4px;" +
                                "-fx-padding: 8px 16px;"
                ));

                createButton.setOnMouseExited(e -> createButton.setStyle(
                        "-fx-background-color: #0078d4;" +
                                "-fx-text-fill: #ffffff;" +
                                "-fx-border-color: #0078d4;" +
                                "-fx-border-width: 1px;" +
                                "-fx-border-radius: 4px;" +
                                "-fx-background-radius: 4px;" +
                                "-fx-padding: 8px 16px;"
                ));
            } else {
                createButton.setStyle(
                        "-fx-background-color: #2a2a2a;" +
                                "-fx-text-fill: #666666;" +
                                "-fx-border-color: #444444;" +
                                "-fx-border-width: 1px;" +
                                "-fx-border-radius: 4px;" +
                                "-fx-background-radius: 4px;" +
                                "-fx-padding: 8px 16px;"
                );
                createButton.setOnMouseEntered(null);
                createButton.setOnMouseExited(null);
            }
        });

        createGroupDialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String groupName = groupNameField.getText().trim();
                String description = groupDescriptionField.getText().trim();

                if (!groupName.isEmpty()) {
                    String groupId = UUID.randomUUID().toString();
                    GroupChat newGroup = new GroupChat(groupId, groupName, client.getUsername());

                    if (!description.isEmpty()) {
                        newGroup.setGroupDescription(description);
                    }
                    for (String memberUsername : selectedMembersListView.getItems()) {
                        newGroup.addMember(memberUsername);
                    }

                    return newGroup;
                }
            }
            return null;
        });
    }

    private void showCreateGroupDialog() {
        if (client == null) return;

        Task<List<Client>> loadUsersTask = new Task<List<Client>>() {
            @Override
            protected List<Client> call() throws Exception {
                return DatabaseHandler.getInstance().loadUsers();
            }
        };

        loadUsersTask.setOnSucceeded(e -> {
            List<Client> allUsers = loadUsersTask.getValue();
            availableUsersListView.getItems().clear();
            selectedMembersListView.getItems().clear();

            for (Client user : allUsers) {
                if (!user.getUsername().equals(client.getUsername())) {
                    availableUsersListView.getItems().add(user.getUsername());
                }
            }

            Optional<GroupChat> result = createGroupDialog.showAndWait();
            result.ifPresent(this::createGroupInDatabase);
        });

        new Thread(loadUsersTask).start();
    }

    private void createGroupInDatabase(GroupChat groupChat) {
        Task<Boolean> createGroupTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                boolean success = DatabaseHandler.getInstance().createGroup(groupChat);
                if (success) {
                    try {
                        ChatMessage registerMsg = new ChatMessage(client.getUsername(),
                                "GROUP:" + groupChat.getGroupId(), "JOIN_GROUP");
                        chatClient.sendMessage(registerMsg);
                        System.out.println("Registered creator for group: " + groupChat.getGroupName());
                    } catch (Exception e) {
                        System.err.println("Error registering creator in group: " + e.getMessage());
                    }
                    for (String memberUsername : groupChat.getMembers()) {
                        if (!memberUsername.equals(client.getUsername())) {
                            try {
                                ChatMessage inviteMsg = new ChatMessage("SYSTEM",
                                        memberUsername, "GROUP_INVITATION:" + groupChat.getGroupId());
                                chatClient.sendMessage(inviteMsg);
                                System.out.println("Sent group invitation to: " + memberUsername);
                            } catch (Exception e) {
                                System.err.println("Error sending group invitation to " + memberUsername + ": " + e.getMessage());
                            }
                        }
                    }
                }
                return success;
            }
        };

        createGroupTask.setOnSucceeded(e -> {
            if (createGroupTask.getValue()) {
                Platform.runLater(() -> {
                    System.out.println("Group created successfully: " + groupChat.getGroupName());
                    GroupMessage systemMsg = new GroupMessage(groupChat.getGroupId(),
                            "Group '" + groupChat.getGroupName() + "' created by " + client.getUsername(),
                            GroupMessage.MessageType.GROUP_CREATED);
                    DatabaseHandler.getInstance().saveGroupMessageAsync(systemMsg, null);
                    loadFriendsAsync();
                    Platform.runLater(() -> {
                        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), event -> {
                            HBox groupCard = findGroupCardByName(groupChat.getGroupName());
                            if (groupCard != null) {
                                switchToGroupChat(groupChat);
                                updateCardSelection(groupCard);
                            }
                        }));
                        timeline.play();
                    });

                    showAlert("Success", "Group created successfully!");
                });
            } else {
                Platform.runLater(() -> showAlert("Error", "Failed to create group. Please try again."));
            }
        });
        createGroupTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                System.out.println("Failed to create group: " + createGroupTask.getException().getMessage());
                showAlert("Error", "Failed to create group: " + createGroupTask.getException().getMessage());
            });
        });

        new Thread(createGroupTask).start();
    }

    private void UpdateBG() {
        try {
            String BGPath = "/com/example/owlpost_2_0/Images/Backgrounds/" + TimeBasedBG() + ".gif";
            Image image = new Image(getClass().getResource(BGPath).toExternalForm());

            Platform.runLater(() -> {
                chatBG.setImage(image);
                chatBG.setFitWidth(chatbody.getWidth());
                chatBG.setFitHeight(chatbody.getHeight());
                chatBG.setPreserveRatio(false);

                chatBG.toBack();
                chatScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
                msgbox.setStyle("-fx-background-color: transparent; -fx-padding: 10");
            });

        } catch (Exception e) {
            System.out.println("Error setting background: " + e.getMessage());
        }
    }

    private void createGroupButtons() {
        if (groupButtonsContainer == null) {
            groupButtonsContainer = new HBox(15);
            groupButtonsContainer.setAlignment(Pos.CENTER);
            groupButtonsContainer.setPadding(new Insets(8));
            groupButtonsContainer.setStyle("-fx-background-color: rgba(26, 26, 26, 0.85); " +
                    "-fx-background-radius: 12; " +
                    "-fx-border-color: rgba(212, 175, 55, 0.6); " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 8, 0.4, 0, 3);");
            groupInfoBtn = new Button("🔮 Secrets");
            groupInfoBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #2c1810, #1a0f08); " +
                    "-fx-text-fill: #d4af37; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-family: 'Times New Roman', serif; " +
                    "-fx-padding: 8 15; " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: #8b4513; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 20; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #000000, 3, 0.3, 0, 2);");
            groupInfoBtn.setOnMouseEntered(e -> groupInfoBtn.setStyle(groupInfoBtn.getStyle() +
                    "-fx-background-color: linear-gradient(to bottom, #3d221a, #261612); " +
                    "-fx-text-fill: #ffd700;"));
            groupInfoBtn.setOnMouseExited(e -> groupInfoBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #2c1810, #1a0f08); " +
                    "-fx-text-fill: #d4af37; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-family: 'Times New Roman', serif; " +
                    "-fx-padding: 8 15; " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: #8b4513; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 20; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #000000, 3, 0.3, 0, 2);"));
            groupInfoBtn.setOnAction(this::showGroupInfo);
            addMemberBtn = new Button("⚡ Recruit");
            addMemberBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #0f2a0f, #071207); " +
                    "-fx-text-fill: #90ee90; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-family: 'Times New Roman', serif; " +
                    "-fx-padding: 8 15; " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: #228b22; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 20; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #000000, 3, 0.3, 0, 2);");
            addMemberBtn.setOnMouseEntered(e -> addMemberBtn.setStyle(addMemberBtn.getStyle() +
                    "-fx-background-color: linear-gradient(to bottom, #1a3d1a, #0d1f0d); " +
                    "-fx-text-fill: #98fb98;"));
            addMemberBtn.setOnMouseExited(e -> addMemberBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #0f2a0f, #071207); " +
                    "-fx-text-fill: #90ee90; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-family: 'Times New Roman', serif; " +
                    "-fx-padding: 8 15; " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: #228b22; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 20; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #000000, 3, 0.3, 0, 2);"));
            addMemberBtn.setOnAction(this::addMemberToGroup);
            leaveGroupBtn = new Button("💀 Vanish");
            leaveGroupBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #2a0a0a, #150505); " +
                    "-fx-text-fill: #ff6b6b; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-family: 'Times New Roman', serif; " +
                    "-fx-padding: 8 15; " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: #8b0000; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 20; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #000000, 3, 0.3, 0, 2);");
            leaveGroupBtn.setOnMouseEntered(e -> leaveGroupBtn.setStyle(leaveGroupBtn.getStyle() +
                    "-fx-background-color: linear-gradient(to bottom, #3d1010, #1f0808); " +
                    "-fx-text-fill: #ff4444;"));
            leaveGroupBtn.setOnMouseExited(e -> leaveGroupBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #2a0a0a, #150505); " +
                    "-fx-text-fill: #ff6b6b; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-family: 'Times New Roman', serif; " +
                    "-fx-padding: 8 15; " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: #8b0000; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 20; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #000000, 3, 0.3, 0, 2);"));
            leaveGroupBtn.setOnAction(this::leaveGroup);

            groupButtonsContainer.getChildren().addAll(groupInfoBtn, addMemberBtn, leaveGroupBtn);
        }
    }

    private void showGroupButtons() {
        createGroupButtons();
        if (!chatbody.getChildren().contains(groupButtonsContainer)) {
            groupButtonsContainer.setLayoutX(10);
            groupButtonsContainer.setLayoutY(10);
            if (chatbody.getParent() instanceof StackPane) {
                StackPane parent = (StackPane) chatbody.getParent();
                if (!parent.getChildren().contains(groupButtonsContainer)) {
                    parent.getChildren().add(groupButtonsContainer);
                    groupButtonsContainer.toFront();
                }
            } else {
                int insertIndex = 0;
                chatbody.getChildren().add(insertIndex, groupButtonsContainer);
            }
        }
        groupButtonsContainer.setVisible(true);
    }

    private void hideGroupButtons() {
        if (groupButtonsContainer != null) {
            groupButtonsContainer.setVisible(false);
            chatbody.getChildren().remove(groupButtonsContainer);
            if (chatbody.getParent() instanceof StackPane) {
                StackPane parent = (StackPane) chatbody.getParent();
                parent.getChildren().remove(groupButtonsContainer);
            }
        }
    }

    @FXML
    private void addMemberToGroup(ActionEvent event) {
        GroupChat currentGroup = getCurrentSelectedGroup();
        if (currentGroup == null) return;
        Dialog<List<String>> addMemberDialog = new Dialog<>();
        addMemberDialog.setTitle("Add Members");
        addMemberDialog.setHeaderText("Add members to " + currentGroup.getGroupName());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        ListView<String> availableUsersListView = new ListView<>();
        availableUsersListView.setPrefHeight(200);
        availableUsersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Label instructionLabel = new Label("Select users to add to the group:");
        content.getChildren().addAll(instructionLabel, availableUsersListView);

        addMemberDialog.getDialogPane().setContent(content);

        ButtonType addButtonType = new ButtonType("Add Selected", ButtonBar.ButtonData.OK_DONE);
        addMemberDialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        Task<List<String>> loadAvailableUsersTask = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<Client> allUsers = DatabaseHandler.getInstance().loadUsers();
                List<String> groupMembers = DatabaseHandler.getInstance().loadGroupMembers(currentGroup.getGroupId());

                return allUsers.stream()
                        .map(Client::getUsername)
                        .filter(username -> !username.equals(client.getUsername()))
                        .filter(username -> !groupMembers.contains(username))
                        .collect(java.util.stream.Collectors.toList());
            }
        };

        loadAvailableUsersTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                availableUsersListView.getItems().addAll(loadAvailableUsersTask.getValue());

                addMemberDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == addButtonType) {
                        return new ArrayList<>(availableUsersListView.getSelectionModel().getSelectedItems());
                    }
                    return null;
                });

                Optional<List<String>> result = addMemberDialog.showAndWait();
                result.ifPresent(selectedUsers -> addUsersToGroup(currentGroup.getGroupId(), selectedUsers));
            });
        });

        new Thread(loadAvailableUsersTask).start();
    }

    private void addUsersToGroup(String groupId, List<String> usernames) {
        if (usernames.isEmpty()) return;

        Task<Boolean> addUsersTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                boolean success = true;
                for (String username : usernames) {
                    boolean added = DatabaseHandler.getInstance().addMemberToGroup(groupId, username);
                    if (!added) {
                        success = false;
                        System.err.println("Failed to add user: " + username);
                    }
                }
                return success;
            }
        };

        addUsersTask.setOnSucceeded(e -> {
            if (addUsersTask.getValue()) {
                Platform.runLater(() -> {
                    String memberNames = String.join(", ", usernames);
                    GroupMessage systemMsg = new GroupMessage(groupId,
                            client.getUsername() + " added " + memberNames + " to the group",
                            GroupMessage.MessageType.MEMBER_JOINED);
                    DatabaseHandler.getInstance().saveGroupMessageAsync(systemMsg, null);
                    if (isGroupChatMode && getCurrentSelectedGroup() != null &&
                            getCurrentSelectedGroup().getGroupId().equals(groupId)) {
                        showGroupMessageInMainChat(systemMsg);
                    }

                    try {
                        ChatMessage notificationMsg = new ChatMessage(client.getUsername(),"GROUP:" + groupId, "MEMBER_JOINED:" + memberNames);
                        chatClient.sendMessage(notificationMsg);
                        for (String username : usernames) {
                            ChatMessage inviteMsg = new ChatMessage("SYSTEM",username, "GROUP_INVITATION:" + groupId);
                            chatClient.sendMessage(inviteMsg);
                            System.out.println("Sent invitation to: " + username + " for group: " + groupId);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error sending member added notification: " + ex.getMessage());
                    }
                    loadFriendsAsync();
                    showAlert("Success", "Members added successfully!");
                });
            } else {
                Platform.runLater(() -> showAlert("Error", "Failed to add some members. Please try again."));
            }
        });

        addUsersTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                System.err.println("Error adding members: " + addUsersTask.getException().getMessage());
                showAlert("Error", "Failed to add members: " + addUsersTask.getException().getMessage());
            });
        });
        new Thread(addUsersTask).start();
    }

    @FXML
    private void leaveGroup(ActionEvent event) {
        GroupChat currentGroup = getCurrentSelectedGroup();
        if (currentGroup == null) return;

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Leave Group");
        confirmDialog.setHeaderText("Leave " + currentGroup.getGroupName() + "?");
        confirmDialog.setContentText("You will no longer receive messages from this group and cannot rejoin unless added by another member.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> leaveTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return DatabaseHandler.getInstance().removeMemberFromGroup(
                            currentGroup.getGroupId(), client.getUsername());
                }
            };
            leaveTask.setOnSucceeded(e -> {
                if (leaveTask.getValue()) {
                    Platform.runLater(() -> {
                        try {
                            ChatMessage leaveMsg = new ChatMessage(client.getUsername(),
                                    "GROUP:" + currentGroup.getGroupId(),
                                    "MEMBER_LEFT:" + client.getUsername());
                            chatClient.sendMessage(leaveMsg);
                            isGroupChatMode = false;
                            currentSelectedGroup = null;
                            currentReceiver = null;
                            msgbox.getChildren().clear();
                            clientIdLabel.setText("Select a chat");
                            clientImage.setImage(loadDefaultProfileImage());
                            hideGroupButtons();
                            loadFriendsAsync();

                            showAlert("Success", "You have left the group successfully.");

                        } catch (Exception ex) {
                            System.err.println("Error sending leave notification: " + ex.getMessage());
                        }
                    });
                } else {
                    showAlert("Error", "Failed to leave group. Please try again.");
                }
            });

            leaveTask.setOnFailed(e -> {
                System.err.println("Error leaving group: " + leaveTask.getException().getMessage());
                showAlert("Error", "Failed to leave group: " + leaveTask.getException().getMessage());
            });

            new Thread(leaveTask).start();
        }
    }

    private void initializeGameManager() {
        if (client != null) {
            gameManager = new GameManager(client.getUsername());
            Task<Boolean> connectTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return gameManager.connectToGameServer();
                }
            };

            connectTask.setOnSucceeded(e -> {
                boolean connected = connectTask.getValue();
                if (connected) {
                    System.out.println("Connected to game server successfully");
                    Platform.runLater(() -> {
                        if (gameBtn != null) {
                            gameBtn.setDisable(false);
                            gameBtn.setText("⚔️ Challenge to Duel");
                        }
                    });
                } else {
                    System.out.println("Failed to connect to game server");
                    Platform.runLater(() -> {
                        if (gameBtn != null) {
                            gameBtn.setDisable(true);
                            gameBtn.setText("⚔️ Game Server Offline");
                        }
                    });
                }
            });

            new Thread(connectTask).start();
        }
    }

    private void createGameButton() {
        if (gameBtn != null) {
            return;
        }
        gameBtn = new Button("⚔️ Connecting...");
        gameBtn.setDisable(true);
        gameBtn.setPrefHeight(40.0);
        gameBtn.setPrefWidth(50.0);
        gameBtn.getStyleClass().add("call-btn"); // Use same style class as other buttons
        gameBtn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #2d1b69, #1a1a2e);" +
                        "-fx-text-fill: #ffd700;" +
                        "-fx-font-family: 'Cinzel', 'Times New Roman', serif;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 18px;" +
                        "-fx-padding: 10;" +
                        "-fx-background-radius: 25;" +
                        "-fx-border-color: #6b5b73;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 25;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, #2d1b69, 6, 0.7, 2, 4);"
        );

        gameBtn.setOnMouseEntered(e -> {
            if (!gameBtn.isDisabled()) {
                gameBtn.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #3d2c79, #2a2a3e);" +
                                "-fx-text-fill: #ffffff;" +
                                "-fx-font-family: 'Cinzel', 'Times New Roman', serif;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 18px;" +
                                "-fx-padding: 10;" +
                                "-fx-background-radius: 25;" +
                                "-fx-border-color: #6b5b73;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 25;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, #3d2c79, 8, 0.8, 2, 4);"
                );
            }
        });

        gameBtn.setOnMouseExited(e -> {
            if (!gameBtn.isDisabled()) {
                gameBtn.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #2d1b69, #1a1a2e);" +
                                "-fx-text-fill: #ffd700;" +
                                "-fx-font-family: 'Cinzel', 'Times New Roman', serif;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 18px;" +
                                "-fx-padding: 10;" +
                                "-fx-background-radius: 25;" +
                                "-fx-border-color: #6b5b73;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 25;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, #2d1b69, 6, 0.7, 2, 4);"
                );
            }
        });

        gameBtn.setOnAction(this::initiateGameChallenge);
    }

    private void initiateGameChallenge(ActionEvent event) {
        Audios.playSound("spell");

        if (currentReceiver == null) {
            showGameAlert("No Opponent", "Please select a wizard to challenge first!");
            return;
        }

        if (isGroupChatMode) {
            showGameAlert("Invalid Challenge", "You can only challenge individual wizards, not groups!");
            return;
        }
        if (gameManager == null || !gameManager.isConnected()) {
            showGameAlert("Connection Error", "Not connected to the game server!");
            return;
        }
        showGameTypeDialog();
    }

    private void showGameTypeDialog() {
        Platform.runLater(() -> {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("3x3", "3x3", "4x4");
            dialog.setTitle("⚔️ Choose Your Battlefield");
            dialog.setHeaderText("🧙‍♂️ Select the size of your magical duel arena:");
            dialog.setContentText("Arena Size:");
            dialog.getDialogPane().setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #1a0f08, #2c1810);" +
                            "-fx-border-color: #8b4513;" +
                            "-fx-border-width: 3px;" +
                            "-fx-border-radius: 15px;" +
                            "-fx-background-radius: 15px;"
            );
            dialog.getDialogPane().lookup(".header-panel .label").setStyle(
                    "-fx-text-fill: #ffd700;" +
                            "-fx-font-family: 'Cinzel', 'Times New Roman', serif;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 16px;"
            );
            dialog.getDialogPane().lookup(".content.label").setStyle(
                    "-fx-text-fill: #e6ddd4;" +
                            "-fx-font-family: 'Times New Roman', serif;" +
                            "-fx-font-size: 14px;"
            );

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(gameType -> {
                gameManager.sendGameRequest(currentReceiver, gameType);
                showGameAlert("Challenge Sent",
                        "⚡ Challenge sent to " + currentReceiver + " for " + gameType + " TicTacToe!\n" +
                                "Waiting for their response...");
            });
        });
    }

    private void showGameAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #1a0f08, #2c1810);" +
                            "-fx-border-color: #8b4513;" +
                            "-fx-border-width: 3px;" +
                            "-fx-border-radius: 15px;" +
                            "-fx-background-radius: 15px;"
            );

            alert.getDialogPane().lookup(".content.label").setStyle(
                    "-fx-text-fill: #e6ddd4;" +
                            "-fx-font-family: 'Times New Roman', serif;" +
                            "-fx-font-size: 14px;"
            );

            alert.showAndWait();
        });
    }

    private void cleanupGameResources() {
        if (gameManager != null) {
            gameManager.disconnect();
            gameManager = null;
        }
    }

    private void addGameButtonToClientCard() {
        if (gameBtn == null) {
            createGameButton();
        }
        clientCard.getChildren().removeIf(node -> node == gameBtn);
        int infoButtonIndex = clientCard.getChildren().indexOf(infobtn);
        if (infoButtonIndex != -1) {
            clientCard.getChildren().add(infoButtonIndex, gameBtn);
        } else {
            clientCard.getChildren().add(gameBtn);
        }
    }

    private void removeGameButtonFromClientCard() {
        if (gameBtn != null) {
            clientCard.getChildren().remove(gameBtn);
        }
    }

    @
    Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadIntroVideo();
        loadFootsteps();
        initializeCallUI();
        createOutgoingCallUI();
        setupCreateGroupDialog();
        createGameButton();
        loadEmojis();
        msgbox.heightProperty().addListener((obs, oldVal, newVal) -> {
            chatScroll.setVvalue(1.0);
        });
        geminibox.heightProperty().addListener((obs, oldVal, newVal) -> {
            geminiscrollpane.setVvalue(1.0);
        });

        geminiscrollpane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        geminibox.setStyle("-fx-background-color: transparent; -fx-padding: 10");


        chatScroll.setFitToWidth(true);
        msgbox.prefWidthProperty().bind(chatScroll.widthProperty().subtract(20));
        msgField.setOnAction(e -> {
            try {
                onSendMessage();
            } catch (Exception ex) {
                System.out.println("Error sending message: " + ex.getMessage());
            }
        });
        geminisend.setOnAction(e -> {
            try {
                System.out.println("Sending to gemini");
                onSendGemini();
            } catch (Exception ex) {
                System.out.println("Error sending message: " + ex.getMessage());
            }
        });
        startGemini();


//        Audios.playBGM();
    }


    public void startGemini() {
        String apiKey = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader("apikey.txt"));
            apiKey = reader.readLine();
            reader.close();

            if (apiKey == null || apiKey.trim().isEmpty()) {
                System.out.println("API key is empty or null");
                return;
            }

        } catch (Exception e) {
            System.out.println("Can't read key: " + e.getMessage());
            return;
        }

        this.apiClient = new GeminiApiClient(apiKey);

        System.out.println("Gemini Chat Client Started.");
    }

    public void geminiQuery(String prompt) {
        if (prompt.trim().isEmpty()) {
            System.out.println("Please enter a non-empty prompt.");
            return;
        }

        try {
            System.out.println("Sending request... (please wait)");
            String response = apiClient.generateContent(prompt);
            JSONObject jsonResponse = new JSONObject(response);

            if (jsonResponse.has("error")) {
                JSONObject error = jsonResponse.getJSONObject("error");
                System.out.println("API Error: " + error.getString("message"));
                return;
            }

            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.length() > 0) {
                    String text = parts.getJSONObject(0).getString("text");
                    System.out.println("\nResponse: " + text);
                    showChatWithGemini(text, false);
                }
            } else {
                System.out.println("No response generated.");
            }

        } catch (java.io.IOException e) {
            System.out.println("Network/API Error: " + e.getMessage());
            if (e.getMessage().contains("429")) {
                System.out.println("You're being rate limited. Please wait a few minutes before trying again.");
            }
        } catch (InterruptedException e) {
            System.out.println("Request was interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    String[] emojis = {"😄", "😆", "😂", "😁", "😅", "😉", "🥲", "🙂", "🤗", "🤔", "🫡", "😑", "🫤", "🙃", "🫠", "🤑", "😖", "😞", "😤", "😭", "😢", "🤪", "😠", "🤮", "😇", "🤭", "😈", "💩", "✌️", "️👌", "👍", "👎", "👊", "👏", "🙌", "🫶", "❤", "️💛", "💚", "💙", "🖤", "🤍", "💔", "❣️", "💞", "❤️‍🩹"};

    private void loadEmojis() {
        emojibox.getChildren().clear(); // Clear previous emojis if reloading

        int index = 0;
        int columns = 5;
        int num_of_emojis = emojis.length;

        for (int i = 0; i <= num_of_emojis / columns; i++) {
            HBox hbox = new HBox(5);
            hbox.setPadding(new Insets(2));

            for (int j = 0; j < columns && index < num_of_emojis; j++) {
                String emoji = emojis[index++];
                Button emojiBtn = new Button(emoji);

                emojiBtn.setStyle("""
                            -fx-background-color: transparent;
                            -fx-font-size: 12px;
                            -fx-cursor: hand;
                            -fx-font-family: 'Segoe UI Emoji';
                        """);
                emojiBtn.setPrefSize(50, 50);
                emojiBtn.setFocusTraversable(false); // Prevent focus on tab
                emojiBtn.setFont(Font.font("Segoe UI Emoji"));

                emojiBtn.setOnAction(e -> msgField.appendText(emoji));

                hbox.getChildren().add(emojiBtn);
            }

            emojibox.getChildren().add(hbox);
        }
    }

    private void loadIntroVideo() {
//        System.out.println(new File("src/main/resources/com/example/owlpost_2_0/Video/owlvideo.mp4").toURI().toString());
        Media media = new Media(new File("src/main/resources/com/example/owlpost_2_0/Video/owlvideo.mp4").toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        owlvideo = new MediaView(mediaPlayer);
//        owlvideo.setPreserveRatio(false);
        owlvideo.setFitWidth(1280);
        owlvideo.setFitHeight(720);

        introvideopane.getChildren().add(owlvideo);
        mediaPlayer.setOnEndOfMedia(()->{
            Animations.FadeTransition(introvideopane, false);
            Animations.FadeTransition(footstepPane, false);
        });

        mediaPlayer.setOnReady(()->{
            mediaPlayer.play();
        });
    }

    private void loadFootsteps() {
        Image leftfoot = new Image(getClass().getResourceAsStream("/com/example/owlpost_2_0/Images/Animation/left.png"));
        Image rightfoot = new Image(getClass().getResourceAsStream("/com/example/owlpost_2_0/Images/Animation/right.png"));
        ImageView currentStep = new ImageView(leftfoot);
        footstepPane.getChildren().add(currentStep);
        double startX = 50;
        double Y = 650;
        int steps = 20;
        double stepSize = 60;

        Timeline timeline = new Timeline();
        for (int i = 0; i < steps; i++) {
            final int index = i;
            KeyFrame kf = new KeyFrame(Duration.seconds(i * 0.5), e -> {
                currentStep.setImage(index % 2 == 0 ? leftfoot : rightfoot);
                currentStep.setLayoutX(startX + index * stepSize);
                currentStep.setLayoutY(Y);
                currentStep.setOpacity(1.0);

                FadeTransition fade = new FadeTransition(Duration.seconds(0.4), currentStep);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.play();
            });
            timeline.getKeyFrames().add(kf);
        }

        KeyFrame showLabel = new KeyFrame(Duration.seconds(2.0), e -> {
//            Label owlPostLabel = new Label("OwlPost");
//            owlPostLabel.setFont(Font.font("Georgia", FontWeight.EXTRA_BOLD, 36));
//            owlPostLabel.setTextFill(Color.WHITE);
//            owlPostLabel.setLayoutX(600); // Top right corner-ish
//            owlPostLabel.setLayoutY(40);
//            owlPostLabel.setOpacity(0);
//
//            animationPane.getChildren().add(owlPostLabel);
            owltext.setVisible(true);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), owltext);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1.0);

            ScaleTransition pop = new ScaleTransition(Duration.seconds(1.2), owltext);
            pop.setFromX(0.8);
            pop.setToX(1.0);
            pop.setFromY(0.8);
            pop.setToY(1.0);

            ParallelTransition appear = new ParallelTransition(fadeIn, pop);
            appear.setOnFinished(ev -> {
                playTypewriterEffect(privacytext, "Your privacy is our utmost concern", Duration.millis(40));
            });
            appear.play();
        });

        timeline.getKeyFrames().add(showLabel);
        timeline.play();
    }

    private void playTypewriterEffect(Label label, String message, Duration delayPerChar) {
        label.setVisible(true); // make sure it's visible
        Timeline timeline = new Timeline();

        for (int i = 0; i < message.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(delayPerChar.multiply(i), e -> {
                label.setText(message.substring(0, index + 1));
            });
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.play();
    }
}