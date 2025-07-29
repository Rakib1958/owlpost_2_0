package com.example.owlpost_2_0.Controllers;

import com.example.owlpost_2_0.ChatRoom.ChatMessage;
import com.example.owlpost_2_0.ChatRoom.GroupChat;
import com.example.owlpost_2_0.ChatRoom.GroupMessage;
import com.example.owlpost_2_0.Client.ChatClient;
import com.example.owlpost_2_0.Client.Client;
import com.example.owlpost_2_0.Database.DatabaseHandler;
import com.example.owlpost_2_0.Resources.Audios;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.Files.readAllBytes;

public class GroupChatController implements Initializable {

    @FXML private VBox groupsList;
    @FXML private VBox groupChatBox;
    @FXML private ScrollPane groupChatScroll;
    @FXML private TextField groupMessageField;
    @FXML private Button sendGroupMessage;
    @FXML private Button sendGroupFile;
    @FXML private Button createGroupBtn;
    @FXML private Label currentGroupLabel;
    @FXML private Label groupMembersLabel;
    @FXML private ImageView currentGroupImage;

    private Client currentUser;
    private ChatClient chatClient;
    private GroupChat currentGroup;
    private DatabaseHandler dbHandler;

    // Group Creation Dialog Components
    private Dialog<GroupChat> createGroupDialog;
    private TextField groupNameField;
    private TextArea groupDescriptionField;
    private ListView<String> availableUsersListView;
    private ListView<String> selectedMembersListView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbHandler = DatabaseHandler.getInstance();
        setupUI();
        setupCreateGroupDialog();
    }

    private void setupUI() {
        groupChatBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            groupChatScroll.setVvalue(1.0);
        });

        groupChatScroll.setFitToWidth(true);
        groupChatBox.prefWidthProperty().bind(groupChatScroll.widthProperty().subtract(20));

        groupMessageField.setOnAction(e -> {
            try {
                sendGroupMessage();
            } catch (Exception ex) {
                System.out.println("Error sending group message: " + ex.getMessage());
            }
        });

        // Style the components
        groupsList.setStyle("-fx-background-color: rgba(0,0,0,0.1); -fx-background-radius: 10; -fx-padding: 10;");
        groupChatScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        groupChatBox.setStyle("-fx-background-color: transparent; -fx-padding: 10");
    }

    public void setUserAndChatClient(Client user, ChatClient chatClient) {
        this.currentUser = user;
        this.chatClient = chatClient;
        loadUserGroups();
    }

    @FXML
    private void createGroup(ActionEvent event) {
        Audios.playSound("spell");
        showCreateGroupDialog();
    }

    @FXML
    private void sendGroupMessage(ActionEvent event) {
        try {
            sendGroupMessage();
        } catch (Exception e) {
            System.out.println("Error sending group message: " + e.getMessage());
        }
    }

    @FXML
    private void sendGroupFile(ActionEvent event) {
        Audios.playSound("spell");
        if (currentGroup == null || chatClient == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File to Send to Group");
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                byte[] fileData = readAllBytes(selectedFile.toPath());
                GroupMessage groupMsg = new GroupMessage(currentGroup.getGroupId(),
                        currentUser.getUsername(), selectedFile.getName(), fileData);

                // Send to server (you'll need to modify ChatMessage to handle group messages)
                // For now, we'll save to database and show in UI
                showGroupMessageInChat(groupMsg);
                dbHandler.saveGroupMessageAsync(groupMsg, null);

            } catch (Exception e) {
                System.out.println("Error sending group file: " + e.getMessage());
            }
        }
    }

    // In GroupChatController.java, modify sendGroupMessage:
    private void sendGroupMessage() throws Exception {
        if (chatClient == null || currentUser == null || currentGroup == null) {
            System.err.println("Cannot send group message: missing required components");
            showAlert("Error", "Unable to send message. Please try again.");
            return;
        }

        String content = groupMessageField.getText().trim();
        if (content.isEmpty()) return;

        try {
            GroupMessage groupMsg = new GroupMessage(currentGroup.getGroupId(),
                    currentUser.getUsername(), content);

            groupMessageField.clear();
            showGroupMessageInChat(groupMsg);

            // Save to database
            dbHandler.saveGroupMessageAsync(groupMsg, () -> {
                System.out.println("Group message saved to database");
            });

            // Send through server for real-time broadcasting to group members
            String groupReceiver = "GROUP:" + currentGroup.getGroupId();
            ChatMessage serverMsg = new ChatMessage(currentUser.getUsername(), groupReceiver, content);
            chatClient.sendMessage(serverMsg);

        } catch (Exception e) {
            System.err.println("Error sending group message: " + e.getMessage());
            showAlert("Error", "Failed to send message: " + e.getMessage());
        }
    }

    private void setupCreateGroupDialog() {
        createGroupDialog = new Dialog<>();
        createGroupDialog.setTitle("Create New Group");
        createGroupDialog.setHeaderText("Create a new group chat");

        // Create dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        groupNameField = new TextField();
        groupNameField.setPromptText("Group name");
        groupNameField.setPrefWidth(200);

        groupDescriptionField = new TextArea();
        groupDescriptionField.setPromptText("Group description (optional)");
        groupDescriptionField.setPrefRowCount(3);
        groupDescriptionField.setPrefWidth(200);

        Label membersLabel = new Label("Add Members:");
        membersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Available users list
        availableUsersListView = new ListView<>();
        availableUsersListView.setPrefHeight(150);
        availableUsersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Selected members list
        selectedMembersListView = new ListView<>();
        selectedMembersListView.setPrefHeight(150);

        // Buttons to move users between lists
        VBox buttonBox = new VBox(10);
        Button addMemberBtn = new Button("Add →");
        Button removeMemberBtn = new Button("← Remove");

        addMemberBtn.setOnAction(e -> {
            List<String> selected = availableUsersListView.getSelectionModel().getSelectedItems();
            for (String user : selected) {
                if (!selectedMembersListView.getItems().contains(user)) {
                    selectedMembersListView.getItems().add(user);
                }
            }
        });

        removeMemberBtn.setOnAction(e -> {
            List<String> selected = new ArrayList<>(selectedMembersListView.getSelectionModel().getSelectedItems());
            selectedMembersListView.getItems().removeAll(selected);
        });

        buttonBox.getChildren().addAll(addMemberBtn, removeMemberBtn);
        buttonBox.setAlignment(Pos.CENTER);

        // Layout
        grid.add(new Label("Group Name:"), 0, 0);
        grid.add(groupNameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(groupDescriptionField, 1, 1);
        grid.add(membersLabel, 0, 2);
        grid.add(new Label("Available Users:"), 0, 3);
        grid.add(availableUsersListView, 0, 4);
        grid.add(buttonBox, 1, 4);
        grid.add(new Label("Selected Members:"), 2, 3);
        grid.add(selectedMembersListView, 2, 4);

        createGroupDialog.getDialogPane().setContent(grid);

        // Add buttons
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        createGroupDialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Enable/disable create button based on input
        Node createButton = createGroupDialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        groupNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.trim().isEmpty());
        });

        // Convert result
        createGroupDialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String groupName = groupNameField.getText().trim();
                String description = groupDescriptionField.getText().trim();

                if (!groupName.isEmpty()) {
                    String groupId = UUID.randomUUID().toString();
                    GroupChat newGroup = new GroupChat(groupId, groupName, currentUser.getUsername());
                    if (!description.isEmpty()) {
                        newGroup.setGroupDescription(description);
                    }

                    // Add selected members
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
        if (currentUser == null) return;

        // Load available users
        Task<List<Client>> loadUsersTask = new Task<List<Client>>() {
            @Override
            protected List<Client> call() throws Exception {
                return dbHandler.loadUsers();
            }
        };

        loadUsersTask.setOnSucceeded(e -> {
            List<Client> allUsers = loadUsersTask.getValue();
            availableUsersListView.getItems().clear();
            selectedMembersListView.getItems().clear();

            // Add all users except current user to available list
            for (Client user : allUsers) {
                if (!user.getUsername().equals(currentUser.getUsername())) {
                    availableUsersListView.getItems().add(user.getUsername());
                }
            }

            // Show dialog
            Optional<GroupChat> result = createGroupDialog.showAndWait();
            result.ifPresent(this::createGroupInDatabase);
        });

        new Thread(loadUsersTask).start();
    }

    private void createGroupInDatabase(GroupChat groupChat) {
        Task<Boolean> createGroupTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return dbHandler.createGroup(groupChat);
            }
        };

        createGroupTask.setOnSucceeded(e -> {
            if (createGroupTask.getValue()) {
                System.out.println("Group created successfully: " + groupChat.getGroupName());

                // Add system message for group creation
                GroupMessage systemMsg = new GroupMessage(groupChat.getGroupId(),
                        "Group '" + groupChat.getGroupName() + "' created by " + currentUser.getUsername(),
                        GroupMessage.MessageType.GROUP_CREATED);
                dbHandler.saveGroupMessageAsync(systemMsg, null);

                // Refresh groups list
                loadUserGroups();

                // Select the newly created group
                Platform.runLater(() -> selectGroup(groupChat));
            } else {
                showAlert("Error", "Failed to create group. Please try again.");
            }
        });

        createGroupTask.setOnFailed(e -> {
            System.out.println("Failed to create group: " + createGroupTask.getException().getMessage());
            showAlert("Error", "Failed to create group: " + createGroupTask.getException().getMessage());
        });

        new Thread(createGroupTask).start();
    }

    private void loadUserGroups() {
        if (currentUser == null) return;

        Task<List<GroupChat>> loadGroupsTask = new Task<List<GroupChat>>() {
            @Override
            protected List<GroupChat> call() throws Exception {
                return dbHandler.loadUserGroups(currentUser.getUsername());
            }
        };

        loadGroupsTask.setOnSucceeded(e -> {
            List<GroupChat> userGroups = loadGroupsTask.getValue();
            Platform.runLater(() -> displayGroups(userGroups));
        });

        loadGroupsTask.setOnFailed(e -> {
            System.out.println("Failed to load groups: " + loadGroupsTask.getException().getMessage());
        });

        new Thread(loadGroupsTask).start();
    }

    private void displayGroups(List<GroupChat> groups) {
        groupsList.getChildren().clear();

        for (GroupChat group : groups) {
            HBox groupCard = createGroupCard(group);
            groupsList.getChildren().add(groupCard);
        }
    }

    private HBox createGroupCard(GroupChat group) {
        HBox card = new HBox(10);
        card.setPrefWidth(300);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10));

        // Group image (placeholder for now)
        ImageView groupImg = new ImageView();
        groupImg.setFitWidth(40);
        groupImg.setFitHeight(40);
        Circle clip = new Circle(20);
        groupImg.setClip(clip);
        groupImg.setImage(loadDefaultGroupImage());

        // Group info
        VBox groupInfo = new VBox(2);
        Label groupName = new Label(group.getGroupName());
        groupName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label memberCount = new Label(group.getMemberCount() + " members");
        memberCount.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");

        groupInfo.getChildren().addAll(groupName, memberCount);

        card.getChildren().addAll(groupImg, groupInfo);

        // Click handler
        card.setOnMouseClicked(e -> selectGroup(group));

        // Hover effects
        card.setOnMouseEntered(e -> {
            if (!group.equals(currentGroup)) {
                card.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10; -fx-cursor: hand;");
            }
        });

        card.setOnMouseExited(e -> {
            if (!group.equals(currentGroup)) {
                card.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 10; -fx-cursor: hand;");
            }
        });

        return card;
    }

    void selectGroup(GroupChat group) {
        currentGroup = group;

        // Update UI
        currentGroupLabel.setText(group.getGroupName());
        groupMembersLabel.setText(group.getMemberCount() + " members");
        currentGroupImage.setImage(loadDefaultGroupImage());

        // Update group card styles
        updateGroupCardStyles();

        // Load group chat history
        loadGroupChatHistory(group.getGroupId());
    }

    private void updateGroupCardStyles() {
        groupsList.getChildren().forEach(node -> {
            node.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 10; -fx-cursor: hand;");
        });

        // Find and highlight current group card
        // This is a simplified approach - in a real app, you'd want to store references
        for (Node node : groupsList.getChildren()) {
            if (node instanceof HBox) {
                HBox card = (HBox) node;
                VBox info = (VBox) card.getChildren().get(1);
                Label nameLabel = (Label) info.getChildren().get(0);
                if (currentGroup != null && nameLabel.getText().equals(currentGroup.getGroupName())) {
                    card.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-background-radius: 10; -fx-cursor: hand;");
                    break;
                }
            }
        }
    }

    void loadGroupChatHistory(String groupId) {
        groupChatBox.getChildren().clear();

        // Show loading message
        Label loadingLabel = new Label("Loading group chat history...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-style: italic;");
        HBox loadingBox = new HBox(loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        groupChatBox.getChildren().add(loadingBox);

        Task<List<GroupMessage>> loadChatTask = new Task<List<GroupMessage>>() {
            @Override
            protected List<GroupMessage> call() throws Exception {
                return dbHandler.loadGroupChatHistory(groupId);
            }
        };

        loadChatTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                groupChatBox.getChildren().clear();
                List<GroupMessage> messages = loadChatTask.getValue();

                if (messages != null && !messages.isEmpty()) {
                    for (GroupMessage msg : messages) {
                        showGroupMessageInChat(msg);
                    }
                } else {
                    Label noMessagesLabel = new Label("No messages yet. Start the conversation!");
                    noMessagesLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-style: italic;");
                    HBox noMsgBox = new HBox(noMessagesLabel);
                    noMsgBox.setAlignment(Pos.CENTER);
                    groupChatBox.getChildren().add(noMsgBox);
                }
            });
        });

        loadChatTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                groupChatBox.getChildren().clear();
                Label errorLabel = new Label("Failed to load group chat history. Please try again.");
                errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-style: italic;");
                HBox errorBox = new HBox(errorLabel);
                errorBox.setAlignment(Pos.CENTER);
                groupChatBox.getChildren().add(errorBox);
            });
        });

        new Thread(loadChatTask).start();
    }

    public void showGroupMessageInChat(GroupMessage msg) {
        VBox messageContainer = new VBox(5);
        messageContainer.setPadding(new Insets(5));

        // Show sender name for group messages (except for current user's messages)
        if (!msg.getSenderUsername().equals(currentUser.getUsername()) && !msg.isSystemMessage()) {
            Label senderLabel = new Label(msg.getSenderUsername());
            senderLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px; -fx-font-weight: bold;");
            messageContainer.getChildren().add(senderLabel);
        }

        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(3));

        Node messageNode;
        if (msg.isSystemMessage()) {
            messageNode = buildSystemMessageBubble(msg);
            messageBox.setAlignment(Pos.CENTER);
        } else {
            if (msg.isFile()) {
                if (msg.isImageMessage()) {
                    messageNode = buildGroupImageBubble(msg);
                } else {
                    messageNode = buildGroupFileBubble(msg);
                }
            } else {
                messageNode = buildGroupTextBubble(msg);
            }

            messageBox.setAlignment(msg.getSenderUsername().equals(currentUser.getUsername()) ?
                    Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        }

        messageBox.getChildren().add(messageNode);
        messageContainer.getChildren().add(messageBox);

        groupChatBox.getChildren().add(messageContainer);
        groupChatScroll.setVvalue(1.0);
    }

    private Node buildGroupTextBubble(GroupMessage msg) {
        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(12));
        bubble.maxWidthProperty().bind(groupChatScroll.widthProperty().multiply(0.6));

        Label contentLabel = new Label(msg.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: " +
                (msg.getSenderUsername().equals(currentUser.getUsername()) ? "white" : "#2c3e50") +
                "; -fx-font-weight: bold;");

        Label timeLabel = new Label(msg.getFormattedTimestamp());
        timeLabel.setStyle("-fx-text-fill: " +
                (msg.getSenderUsername().equals(currentUser.getUsername()) ? "#cccccc" : "#666666") +
                "; -fx-font-size: 10px;");

        bubble.getChildren().addAll(contentLabel, timeLabel);

        if (msg.getSenderUsername().equals(currentUser.getUsername())) {
            bubble.setStyle("-fx-background-color: #128C7E; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 1, 1); " +
                    "-fx-background-radius: 18; " +
                    "-fx-border-radius: 18; " +
                    "-fx-border-color: rgba(255,255,255,0.3); " +
                    "-fx-border-width: 2;");
        } else {
            bubble.setStyle("-fx-background-color: white; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 4, 0, 2, 2); " +
                    "-fx-background-radius: 18; " +
                    "-fx-border-radius: 18; " +
                    "-fx-border-color: #34495e; " +
                    "-fx-border-width: 2;");
        }

        return bubble;
    }

    private Node buildGroupFileBubble(GroupMessage msg) {
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
        return downloadBtn;
    }

    private Node buildGroupImageBubble(GroupMessage msg) {
        VBox imageContainer = new VBox(5);
        imageContainer.setPadding(new Insets(5));

        Image img = new Image(new java.io.ByteArrayInputStream(msg.getFileData()));
        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 1, 1);");

        Label timeLabel = new Label(msg.getFormattedTimestamp());
        timeLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 10px;");

        imageContainer.getChildren().addAll(imageView, timeLabel);

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

    private Node buildSystemMessageBubble(GroupMessage msg) {
        Label systemLabel = new Label(msg.getContent());
        systemLabel.setStyle("-fx-text-fill: #888888; " +
                "-fx-font-style: italic; " +
                "-fx-font-size: 12px; " +
                "-fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 8 12;");
        systemLabel.setWrapText(true);
        return systemLabel;
    }

    private Image loadDefaultGroupImage() {
        try {
            URL url = getClass().getResource("/com/example/owlpost_2_0/Images/default-group.png");
            if (url == null) {
                // Fallback to profile image if group image doesn't exist
                url = getClass().getResource("/com/example/owlpost_2_0/Images/default-profile.png");
            }
            if (url == null) {
                // Create a simple colored rectangle as fallback
                return createDefaultGroupIcon();
            }
            return new Image(url.toExternalForm());
        } catch (Exception e) {
            System.out.println("Error loading group image: " + e.getMessage());
            return createDefaultGroupIcon();
        }
    }

    private Image createDefaultGroupIcon() {
        // Create a simple 40x40 colored image as fallback
        return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==");
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


    public void refreshGroups() {
        loadUserGroups();
    }

    public GroupChat getCurrentGroup() {
        return currentGroup;
    }

    // Add this method to GroupChatController.java

    public void sendGroupMessageFromMainChat(String content) {
        if (chatClient == null || currentUser == null || currentGroup == null) {
            System.err.println("Cannot send group message: missing required components");
            showAlert("Error", "Unable to send message. Please try again.");
            return;
        }

        try {
            GroupMessage groupMsg = new GroupMessage(currentGroup.getGroupId(),
                    currentUser.getUsername(), content);

            // Show in main chat area (since we're in group chat mode)
            showGroupMessageInChat(groupMsg);

            // Save to database
            dbHandler.saveGroupMessageAsync(groupMsg, () -> {
                System.out.println("Group message saved to database");
            });

            // Send through server for real-time broadcasting to group members
            String groupReceiver = "GROUP:" + currentGroup.getGroupId();
            ChatMessage serverMsg = new ChatMessage(currentUser.getUsername(), groupReceiver, content);
            chatClient.sendMessage(serverMsg);

        } catch (Exception e) {
            System.err.println("Error sending group message: " + e.getMessage());
            showAlert("Error", "Failed to send message: " + e.getMessage());
        }
    }

    // Add this method to handle incoming group messages when not in the active group view
    public void handleIncomingGroupMessage(GroupMessage msg) {
        // This method handles group messages that arrive when the group is not currently selected
        // You might want to show notifications, update unread counts, etc.

        if (currentGroup != null && msg.getGroupId().equals(currentGroup.getGroupId())) {
            // If this group is currently selected, show the message
            Platform.runLater(() -> showGroupMessageInChat(msg));
        } else {
            // Handle notification for non-active groups
            System.out.println("New message in group: " + msg.getGroupId() + " from " + msg.getSenderUsername());
            // You could add notification logic here
        }
    }
}