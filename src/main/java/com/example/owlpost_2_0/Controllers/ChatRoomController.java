package com.example.owlpost_2_0.Controllers;

import com.example.owlpost_2_0.ChatRoom.ChatMessage;
import com.example.owlpost_2_0.Client.ChatClient;
import com.example.owlpost_2_0.Client.Client;
import com.example.owlpost_2_0.Database.DatabaseHandler;
import com.example.owlpost_2_0.Resources.Audios;
//import com.example.owlpost_2_0.Server.AudioCallHandler;
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
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
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

    // miscelleneous
    private Client client;
    private ChatClient chatClient;
    private String currentReceiver = null;
    private boolean inCall = false;
//    private AudioCallHandler callHandler;
    private String[] BGs = {"morning", "day", "evening", "night"};
    private Timer BackgroundTimer;

    // message sending methods
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
            ChatMessage msg = new ChatMessage(client.getUsername(), currentReceiver, content); // you'll define currentReceiver
            chatClient.sendMessage(msg);
            msgField.clear();


            DatabaseHandler.getInstance().saveChatMessage(msg);
//            showMessageInChat(msg);
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

                DatabaseHandler.getInstance().saveChatMessage(msg);
                showMessageInChat(msg);

            } catch (Exception e) {
                System.out.println("Error sending file: " + e.getMessage());
            }
        }
    }

//    private void onCallClicked() {
//        if (!inCall) {
//            try {
//                callHandler = new AudioCallHandler();
//                callHandler.startcall("localhost", 5000);
//                audiocall.setStyle("-fx-text-fill: #ffffff");
//                inCall = true;
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//            }
//        } else {
//            callHandler.endcall();
//            audiocall.setStyle("-fx-text-fill: #000000");
//            inCall = false;
//        }
//    }

    private void handleIncomingMsg(ChatMessage msg) {
        if (!msg.getSender().equals(client.getUsername())){
            DatabaseHandler.getInstance().saveChatMessage(msg);
        }
        if (currentReceiver != null &&
                (msg.getSender().equals(currentReceiver) || msg.getReceiver().equals(currentReceiver))) {
            Platform.runLater(() -> showMessageInChat(msg));
        }

    }

    private void showMessageInChat(ChatMessage msg) {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(3));
        hBox.setAlignment(msg.getSender().equals(client.getUsername()) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        // configure the message according to its type
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
//        ChatHistory.saveMessage(msg.getSender(), msg.getReceiver(), msg);
    }

    private boolean shouldDisplay(ChatMessage msg) {
        if ((msg.getSender().equals(currentReceiver) && msg.getReceiver().equals(client.getUsername())) || (msg.getSender().equals(client.getUsername()) && msg.getReceiver().equals(currentReceiver))) {
            return true;
        }
        return false;
    }

    private Node buildTextBubble(ChatMessage msg) {
        Label label = new Label(msg.getContent());

        label.setWrapText(true);
        label.setMaxWidth(300);
        if (msg.getSender().equals(client.getUsername())) {
            // Your messages - darker green with white text and shadow
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
            // Others' messages - white with dark text and stronger shadow
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
        // Enhanced styling for file buttons
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

        // Hover effect
        downloadBtn.setOnMouseEntered(e -> {
            downloadBtn.setStyle("-fx-background-color: #2980b9; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 12px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 4, 0, 2, 2); " +
                    "-fx-padding: 10 15; " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-radius: 15; " +
                    "-fx-border-color: #1f618d; " +
                    "-fx-border-width: 2; " +
                    "-fx-cursor: hand;");
        });

        downloadBtn.setOnMouseExited(e -> {
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
        });

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
        if (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".gif")) {
            return true;
        }
        return false;
    }

    // button configs
    public void ButtonAction(ActionEvent event) {
        Audios.playSound("spell");
        Button btn = (Button) event.getSource();
        if (btn.equals(sendMsg)) {
            try {
                onSendMessage();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        if (btn.equals(sendImg)) {
            onSendFile();
        }
        if (btn.equals(audiocall)) {
//            onCallClicked();
        }
    }

    private Image loadProfileImage(String profilePicturePath) {
        if (profilePicturePath == null) {
            // Return default image if no profile picture
            return new Image(getClass().getResource("/com/example/chatapp/Images/default-profile.png").toExternalForm());
        }

        if (profilePicturePath.startsWith("BLOB:")) {
            // Extract username from BLOB identifier
            String username = profilePicturePath.substring(5);
            Image image = DatabaseHandler.getInstance().getProfilePicture(username);
            if (image != null) {
                return image;
            }
            // Return default if BLOB data is null
            return new Image(getClass().getResource("/com/example/chatapp/Images/default-profile.png").toExternalForm());
        } else {
            // Handle regular file path
            try {
                return new Image(profilePicturePath);
            } catch (Exception e) {
                System.out.println("Error loading image from path: " + e.getMessage());
                return new Image(getClass().getResource("/com/example/chatapp/Images/default-profile.png").toExternalForm());
            }
        }
    }

    // initialize the clients other than the initialize method
    public void getClient(Client client) {
        this.client = client;
        System.out.println("Got client");

        Image userProfileImage = loadProfileImage(client.getProfilePicturePath());
        userImage.setImage(userProfileImage);
        userIdLabel.setText(client.getUsername());

        setCircularImage(userImage, userImageClip, userProfileImage);

        ImageView backgroundImageView = new ImageView();
        backgroundImageView.setImage(new Image(getClass().getResource("/com/example/chatapp/Images/slytherin.gif").toExternalForm()));
        backgroundImageView.setFitWidth(319);
        backgroundImageView.setFitHeight(630);
        backgroundImageView.setLayoutY(100);
        backgroundImageView.setPreserveRatio(false);

        leftbase.getChildren().add(0, backgroundImageView);
        leftpane.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-background: transparent; " +
                        "-fx-background-insets: 0; " +
                        "-fx-padding: 0;"
        );

        // Make sure VBox is also transparent
        friendslist.setStyle("-fx-background-color: transparent;");
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
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(5));

        ImageView img = new ImageView(new Image(c.getProfilePicturePath()));
        img.setFitHeight(48);
        img.setFitWidth(48);
        Circle clip = new Circle(24, 24, 24);
        Image friendImage = loadProfileImage(c.getProfilePicturePath());
        setCircularImage(img, clip, friendImage);

        Label name = new Label(c.getUsername());
        card.getChildren().addAll(img, name);
        return card;
    }

    private void styleFriendCard(HBox card, Client c) {
        card.setOnMouseClicked(e -> {
            currentReceiver = c.getUsername();
            msgbox.getChildren().clear();
            loadChatHistory(client.getUsername(), currentReceiver);

            clientImage.setImage(new Image(c.getProfilePicturePath()));
            clientIdLabel.setText(c.getUsername());

            updateFriendCardStyle(card);
        });

        // Hover effects
        card.setOnMouseEntered(e -> {
            if (!c.getUsername().equals(currentReceiver)) {
                card.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;");
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
        List<ChatMessage> messages = DatabaseHandler.getInstance().loadChatHistory(sender, receiver);
        msgbox.getChildren().clear();
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
            String BGPath = "/com/example/chatapp/Images/Backgrounds/" + TimeBasedBG() + ".gif";
            Image image = new Image(getClass().getResource(BGPath).toExternalForm());

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
        msgbox.heightProperty().addListener((obs, oldVal, newVal) -> {
            chatScroll.setVvalue(1.0);
        });

        msgField.setOnAction(e -> {
            try {
                onSendMessage();
            } catch (Exception ex) {
                System.out.println("Error sending message: " + ex.getMessage());
            }
        });

        Audios.playBGM();

    }


}
