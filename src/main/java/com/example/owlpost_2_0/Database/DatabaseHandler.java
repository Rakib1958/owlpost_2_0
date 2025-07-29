package com.example.owlpost_2_0.Database;

import com.example.owlpost_2_0.ChatRoom.ChatMessage;
import com.example.owlpost_2_0.ChatRoom.GroupChat;
import com.example.owlpost_2_0.ChatRoom.GroupMessage;
import com.example.owlpost_2_0.Client.Client;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import com.google.cloud.firestore.Query;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DatabaseHandler {
    private static DatabaseHandler instance;
    private Firestore db;
    private Storage storage;
    private String bucketName;
    private Map<String, List<GroupChat>> userGroupsCache = new ConcurrentHashMap<>();
    private Map<String, GroupChat> groupByIdCache = new ConcurrentHashMap<>();
    private Map<String, GroupChat> groupByNameCache = new ConcurrentHashMap<>();
    private long lastGroupsCacheUpdate = 0;
    private static final long CACHE_EXPIRY_TIME = 5 * 60 * 1000;

    private Map<String, Image> profileImageCache = new ConcurrentHashMap<>();

    private DatabaseHandler() {
        initializeFirebase();
    }

    public static DatabaseHandler getInstance() {
        if (instance == null) instance = new DatabaseHandler();
        return instance;
    }

    private void initializeFirebase() {
        try {
            InputStream serviceAccount = getClass().getResourceAsStream("/com/example/owlpost_2_0/db/serviceAccountKey.json");

            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(credentials)
                    .setProjectId("owlpost-8925a")
                    .setStorageBucket("owlpost-8925a.firebasestorage.app")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            db = FirestoreClient.getFirestore();
            storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId("owlpost-8925a")
                    .build()
                    .getService();
            bucketName = "owlpost-8925a.firebasestorage.app";
            FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId("owlpost-8925a")
                    .build();

        } catch (IOException e) {
            System.out.println("Firebase initialization failed: " + e.getMessage());
        }
    }

    public boolean registerUser(Client client, File profilePic) {
        if (client == null || client.getUsername() == null || client.getPassword() == null ||
                client.getEmail() == null || client.getDateofbirth() == null ||
                client.getHouse() == null || client.getPatronus() == null) {
            return false;
        }

        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", client.getUsername());
            userData.put("password", client.getPassword());
            userData.put("email", client.getEmail());
            userData.put("date_of_birth", client.getDateofbirth().toString());
            userData.put("house", client.getHouse());
            userData.put("patronus", client.getPatronus());
            userData.put("created_at", new Date());

            String profilePicUrl = uploadProfilePicture(client.getUsername(), profilePic);
            userData.put("profile_picture_url", profilePicUrl);

            ApiFuture<WriteResult> future = db.collection("users").document(client.getUsername()).set(userData);
            future.get();

            return true;
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public Client login(String username, String password) {
        try {
            ApiFuture<DocumentSnapshot> future = db.collection("users").document(username).get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                String storedPassword = document.getString("password");
                if (password.equals(storedPassword)) {
                    Client client = new Client();
                    client.setUsername(document.getString("username"));
                    client.setPassword(document.getString("password"));
                    client.setEmail(document.getString("email"));
                    client.setDateofbirth(LocalDate.parse(document.getString("date_of_birth")));
                    client.setHouse(document.getString("house"));
                    client.setPatronus(document.getString("patronus"));
                    client.setProfilePicturePath(document.getString("profile_picture_url"));

                    return client;
                }
            }
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
        return null;
    }

    public boolean updatePassword(String username, String newPassword) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("password", newPassword);
            updates.put("updated_at", new Date());

            ApiFuture<WriteResult> future = db.collection("users").document(username).update(updates);
            future.get();
            return true;
        } catch (Exception e) {
            System.out.println("Password update failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateProfilePicture(String username, File newPic) {
        try {
            String newProfilePicUrl = uploadProfilePicture(username, newPic);

            Map<String, Object> updates = new HashMap<>();
            updates.put("profile_picture_url", newProfilePicUrl);
            updates.put("updated_at", new Date());

            ApiFuture<WriteResult> future = db.collection("users").document(username).update(updates);
            future.get();
            removeFromProfileImageCache(username);

            return true;
        } catch (Exception e) {
            System.out.println("Profile picture update failed: " + e.getMessage());
            return false;
        }
    }

    public Image getProfilePicture(String username) {
        if (profileImageCache.containsKey(username)) {
            System.out.println("Loading profile image from cache for: " + username);
            return profileImageCache.get(username);
        }

        System.out.println("Loading profile image from Firebase for: " + username);
        try {
            ApiFuture<DocumentSnapshot> future = db.collection("users").document(username).get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                String profilePicUrl = document.getString("profile_picture_url");
                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                    BlobId blobId = BlobId.of(bucketName, profilePicUrl);
                    Blob blob = storage.get(blobId);

                    if (blob != null) {
                        byte[] content = blob.getContent();
                        Image image = new Image(new ByteArrayInputStream(content));
                        profileImageCache.put(username, image);
                        System.out.println("Cached profile image for: " + username);

                        return image;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch profile picture for " + username + ": " + e.getMessage());
        }
        return null;
    }

    public void clearProfileImageCache() {
        profileImageCache.clear();
        System.out.println("Profile image cache cleared");
    }

    public void removeFromProfileImageCache(String username) {
        profileImageCache.remove(username);
        System.out.println("Removed " + username + " from profile image cache");
    }



    private String uploadProfilePicture(String username, File file) {
        if (file == null || !file.exists()) return null;

        try {
            byte[] fileData = fileToByteArray(file);
            String fileName = "profile_pictures/" + username;

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("image/jpeg")
                    .build();

            storage.create(blobInfo, fileData);

            return fileName;
        } catch (Exception e) {
            System.out.println("Profile picture upload failed: " + e.getMessage());
            return null;
        }
    }

    public boolean updateOnlineStatus(String username, boolean isOnline) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("is_online", isOnline);
            updates.put("last_seen", new Date());

            ApiFuture<WriteResult> future = db.collection("users").document(username).update(updates);
            future.get();
            return true;
        } catch (Exception e) {
            System.out.println("Online status update failed: " + e.getMessage());
            return false;
        }
    }

    private byte[] fileToByteArray(File file) {
        if (file == null || !file.exists()) return null;
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            System.out.println("File read error: " + e.getMessage());
            return null;
        }
    }

    public boolean usernameExists(String username) {
        try {
            ApiFuture<DocumentSnapshot> future = db.collection("users").document(username).get();
            DocumentSnapshot document = future.get();
            return document.exists();
        } catch (Exception e) {
            System.err.println("Username check failed: " + e.getMessage());
            return false;
        }
    }

    public Client findUserByEmail(String email) {
        try {
            ApiFuture<QuerySnapshot> future = db.collection("users")
                    .whereEqualTo("email", email)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (!documents.isEmpty()) {
                DocumentSnapshot document = documents.get(0);
                Client client = new Client();
                client.setUsername(document.getString("username"));
                client.setPassword(document.getString("password"));
                client.setEmail(document.getString("email"));
                client.setDateofbirth(LocalDate.parse(document.getString("date_of_birth")));
                client.setHouse(document.getString("house"));
                client.setPatronus(document.getString("patronus"));
                client.setProfilePicturePath(document.getString("profile_picture_url"));

                return client;
            }
        } catch (Exception e) {
            System.out.println("Find user by email failed: " + e.getMessage());
        }
        return null;
    }

    public boolean emailExists(String email) {
        try {
            ApiFuture<QuerySnapshot> future = db.collection("users")
                    .whereEqualTo("email", email)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            return !documents.isEmpty();
        } catch (Exception e) {
            System.err.println("Email check failed: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if (FirebaseApp.getApps().size() > 0) {
                FirebaseApp.getInstance().delete();
            }
        } catch (Exception e) {
            System.out.println("Firebase shutdown failed: " + e.getMessage());
        }
    }

    public void updateOnlineStatusAsync(String username, boolean isOnline, Runnable onComplete) {
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> updates = new HashMap<>();
                updates.put("is_online", isOnline);
                updates.put("last_seen", new Date());

                ApiFuture<WriteResult> future = db.collection("users").document(username).update(updates);
                future.get();

                if (onComplete != null) {
                    Platform.runLater(onComplete);
                }
            } catch (Exception e) {
                System.out.println("Online status update failed: " + e.getMessage());
            }
        });
    }

    public void saveChatMessageAsync(ChatMessage msg, Runnable onComplete) {
        CompletableFuture.runAsync(() -> {
            boolean success = saveChatMessage(msg); // Your existing method
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
        });
    }

    public void loadUsersAsync(Consumer<List<Client>> callback) {
        CompletableFuture.supplyAsync(() -> {
            return loadUsers(); // Your existing method
        }).thenAccept(users -> {
            Platform.runLater(() -> callback.accept(users));
        });
    }

    public void loadChatHistoryAsync(String user1, String user2, Consumer<List<ChatMessage>> callback) {
        CompletableFuture.supplyAsync(() -> {
            return loadChatHistory(user1, user2);
        }).thenAccept(messages -> {
            Platform.runLater(() -> callback.accept(messages));
        });
    }

    public boolean saveChatMessage(ChatMessage msg) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("sender", msg.getSender());
            messageData.put("receiver", msg.getReceiver());
            messageData.put("is_file", msg.isFile());
            messageData.put("timestamp", new Date());

            String conversationId = msg.getSender().compareTo(msg.getReceiver()) < 0 ?
                    msg.getSender() + "_" + msg.getReceiver() :
                    msg.getReceiver() + "_" + msg.getSender();
            messageData.put("conversation_id", conversationId);

            if (msg.isFile()) {
                String fileUrl = uploadChatFile(msg.getSender(), msg.getReceiver(),
                        msg.getFileName(), msg.getFileData());
                messageData.put("file_name", msg.getFileName());
                messageData.put("file_url", fileUrl);
                messageData.put("content", null);
            } else {
                messageData.put("content", msg.getContent());
                messageData.put("file_name", null);
                messageData.put("file_url", null);
            }

            ApiFuture<DocumentReference> future = db.collection("chat_history").add(messageData);
            future.get();

            return true;
        } catch (Exception e) {
            System.out.println("Error saving chat message: " + e.getMessage());
            return false;
        }
    }

    public List<ChatMessage> loadChatHistory(String user1, String user2) {
        List<ChatMessage> messages = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future1 = db.collection("chat_history")
                    .whereEqualTo("sender", user1)
                    .whereEqualTo("receiver", user2)
                    .get();

            ApiFuture<QuerySnapshot> future2 = db.collection("chat_history")
                    .whereEqualTo("sender", user2)
                    .whereEqualTo("receiver", user1)
                    .get();

            List<DocumentSnapshot> allDocs = new ArrayList<>();
            allDocs.addAll(future1.get().getDocuments());
            allDocs.addAll(future2.get().getDocuments());
            allDocs.sort((doc1, doc2) -> {
                Date timestamp1 = doc1.getDate("timestamp");
                Date timestamp2 = doc2.getDate("timestamp");
                if (timestamp1 == null || timestamp2 == null) return 0;
                return timestamp1.compareTo(timestamp2);
            });

            for (DocumentSnapshot document : allDocs) {
                String sender = document.getString("sender");
                String receiver = document.getString("receiver");
                Boolean isFileObj = document.getBoolean("is_file");
                boolean isFile = isFileObj != null ? isFileObj : false;

                ChatMessage msg;
                if (isFile) {
                    String fileName = document.getString("file_name");
                    String fileUrl = document.getString("file_url");
                    byte[] fileData = downloadChatFile(fileUrl);
                    msg = new ChatMessage(sender, receiver, fileName, fileData);
                } else {
                    String content = document.getString("content");
                    msg = new ChatMessage(sender, receiver, content != null ? content : "");
                }
                messages.add(msg);
            }

        } catch (Exception e) {
            System.out.println("Error loading chat history: " + e.getMessage());
        }

        return messages;
    }

    public List<Client> loadUsers() {
        List<Client> users = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = db.collection("users").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (DocumentSnapshot document : documents) {
                Client client = new Client();
                client.setUsername(document.getString("username"));
                client.setPassword(document.getString("password"));
                client.setEmail(document.getString("email"));
                client.setDateofbirth(LocalDate.parse(document.getString("date_of_birth")));
                client.setHouse(document.getString("house"));
                client.setPatronus(document.getString("patronus"));
                client.setProfilePicturePath(document.getString("profile_picture_url"));
                Boolean isOnlineObj = document.getBoolean("is_online");
                client.setOnline(isOnlineObj != null ? isOnlineObj : false);
                Date lastSeenDate = document.getDate("last_seen");
                client.setLastSeen(lastSeenDate);
                users.add(client);
            }
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
        }

        return users;
    }

    private String uploadChatFile(String sender, String receiver, String fileName, byte[] fileData) {
        try {
            String filePath = "chat_files/" + sender + "_" + receiver + "_" + System.currentTimeMillis() + "_" + fileName;

            BlobId blobId = BlobId.of(bucketName, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            storage.create(blobInfo, fileData);
            return filePath;
        } catch (Exception e) {
            System.out.println("Chat file upload failed: " + e.getMessage());
            return null;
        }
    }

    private byte[] downloadChatFile(String fileUrl) {
        try {
            BlobId blobId = BlobId.of(bucketName, fileUrl);
            Blob blob = storage.get(blobId);

            if (blob != null) {
                return blob.getContent();
            }
        } catch (Exception e) {
            System.out.println("Chat file download failed: " + e.getMessage());
        }
        return null;
    }

    public boolean createGroup(GroupChat groupChat) {
        try {
            Map<String, Object> groupData = new HashMap<>();
            groupData.put("group_id", groupChat.getGroupId());
            groupData.put("group_name", groupChat.getGroupName());
            groupData.put("group_description", groupChat.getGroupDescription());
            groupData.put("creator_username", groupChat.getCreatorUsername());
            groupData.put("members", new ArrayList<>(groupChat.getMembers()));
            groupData.put("group_image_path", groupChat.getGroupImagePath());
            groupData.put("created_at", groupChat.getCreatedAt());
            groupData.put("last_activity", groupChat.getLastActivity());
            groupData.put("is_active", groupChat.isActive());

            ApiFuture<WriteResult> future = db.collection("groups").document(groupChat.getGroupId()).set(groupData);
            future.get();
            for (String member : groupChat.getMembers()) {
                invalidateGroupCaches(member);
            }
            invalidateGroupCaches(groupChat.getCreatorUsername());

            return true;
        } catch (Exception e) {
            System.out.println("Group creation failed: " + e.getMessage());
            return false;
        }
    }

    public List<GroupChat> loadUserGroups(String username) {
        if (userGroupsCache.containsKey(username) &&
                (System.currentTimeMillis() - lastGroupsCacheUpdate) < CACHE_EXPIRY_TIME) {
            System.out.println("Loading user groups from cache for: " + username);
            return new ArrayList<>(userGroupsCache.get(username));
        }
        System.out.println("Loading user groups from database for: " + username);
        List<GroupChat> userGroups = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = db.collection("groups")
                    .whereArrayContains("members", username)
                    .whereEqualTo("is_active", true)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (DocumentSnapshot document : documents) {
                GroupChat group = documentToGroupChat(document);
                if (group != null) {
                    userGroups.add(group);
                }
            }
            userGroups.sort((g1, g2) -> {
                Date date1 = g1.getLastActivity();
                Date date2 = g2.getLastActivity();
                if (date1 == null || date2 == null) return 0;
                return date2.compareTo(date1);
            });
            userGroupsCache.put(username, new ArrayList<>(userGroups));
            lastGroupsCacheUpdate = System.currentTimeMillis();
            System.out.println("Cached user groups for: " + username);
        } catch (Exception e) {
            System.out.println("Error loading user groups: " + e.getMessage());
        }

        return userGroups;
    }

    public void clearGroupCaches() {
        userGroupsCache.clear();
        groupByIdCache.clear();
        groupByNameCache.clear();
        lastGroupsCacheUpdate = 0;
        System.out.println("All group caches cleared");
    }



    public void invalidateGroupCaches(String username) {
        userGroupsCache.remove(username);
        groupByNameCache.entrySet().removeIf(entry -> entry.getKey().startsWith(username + ":"));
        System.out.println("Invalidated group caches for: " + username);
    }

    public void invalidateGroupCache(String groupId) {
        groupByIdCache.remove(groupId);
        groupByNameCache.entrySet().removeIf(entry -> {
            GroupChat group = entry.getValue();
            return group != null && group.getGroupId().equals(groupId);
        });
        System.out.println("Invalidated cache for group: " + groupId);
    }

    public GroupChat getGroupById(String groupId) {
        if (groupByIdCache.containsKey(groupId)) {
            System.out.println("Loading group from ID cache: " + groupId);
            return groupByIdCache.get(groupId);
        }

        System.out.println("Loading group from database: " + groupId);
        try {
            ApiFuture<DocumentSnapshot> future = db.collection("groups").document(groupId).get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                GroupChat group = documentToGroupChat(document);
                if (group != null) {
                    groupByIdCache.put(groupId, group);
                    System.out.println("Cached group by ID: " + groupId);
                }
                return group;
            }
        } catch (Exception e) {
            System.out.println("Error getting group by ID: " + e.getMessage());
        }
        return null;
    }

    public GroupChat getGroupByName(String username, String groupName) {
        String cacheKey = username + ":" + groupName;
        if (groupByNameCache.containsKey(cacheKey)) {
            System.out.println("Loading group from name cache: " + groupName);
            return groupByNameCache.get(cacheKey);
        }
        List<GroupChat> userGroups = loadUserGroups(username);
        GroupChat foundGroup = userGroups.stream()
                .filter(group -> group.getGroupName().equals(groupName))
                .findFirst()
                .orElse(null);
        if (foundGroup != null) {
            groupByNameCache.put(cacheKey, foundGroup);
            System.out.println("Cached group by name: " + groupName);
        }

        return foundGroup;
    }

    public boolean updateGroup(GroupChat groupChat) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("group_name", groupChat.getGroupName());
            updates.put("group_description", groupChat.getGroupDescription());
            updates.put("members", new ArrayList<>(groupChat.getMembers()));
            updates.put("group_image_path", groupChat.getGroupImagePath());
            updates.put("last_activity", groupChat.getLastActivity());
            updates.put("is_active", groupChat.isActive());

            ApiFuture<WriteResult> future = db.collection("groups").document(groupChat.getGroupId()).update(updates);
            future.get();
            invalidateGroupCache(groupChat.getGroupId());
            for (String member : groupChat.getMembers()) {
                invalidateGroupCaches(member);
            }
            return true;
        } catch (Exception e) {
            System.out.println("Group update failed: " + e.getMessage());
            return false;
        }
    }

    public boolean addMemberToGroup(String groupId, String username) {
        try {
            DocumentReference groupRef = db.collection("groups").document(groupId);
            ApiFuture<WriteResult> future = groupRef.update("members", FieldValue.arrayUnion(username));
            future.get();
            invalidateGroupCache(groupId);
            invalidateGroupCaches(username);
            return true;
        } catch (Exception e) {
            System.out.println("Failed to add member to group: " + e.getMessage());
            return false;
        }
    }

    public boolean removeMemberFromGroup(String groupId, String username) {
        try {
            DocumentReference groupRef = db.collection("groups").document(groupId);
            ApiFuture<WriteResult> future = groupRef.update("members", FieldValue.arrayRemove(username));
            future.get();
            invalidateGroupCache(groupId);
            invalidateGroupCaches(username);
            return true;
        } catch (Exception e) {
            System.out.println("Failed to remove member from group: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteGroup(String groupId) {
        try {
            // Mark group as inactive instead of deleting
            Map<String, Object> updates = new HashMap<>();
            updates.put("is_active", false);
            updates.put("last_activity", new Date());

            ApiFuture<WriteResult> future = db.collection("groups").document(groupId).update(updates);
            future.get();
            return true;
        } catch (Exception e) {
            System.out.println("Group deletion failed: " + e.getMessage());
            return false;
        }
    }



    public boolean saveGroupMessage(GroupMessage msg) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("message_id", msg.getMessageId());
            messageData.put("group_id", msg.getGroupId());
            messageData.put("sender_username", msg.getSenderUsername());
            messageData.put("content", msg.getContent());
            messageData.put("timestamp", msg.getTimestamp());
            messageData.put("is_file", msg.isFile());
            messageData.put("message_type", msg.getMessageType().toString());

            if (msg.isFile()) {
                String fileUrl = uploadGroupChatFile(msg.getGroupId(), msg.getSenderUsername(),
                        msg.getFileName(), msg.getFileData());
                messageData.put("file_name", msg.getFileName());
                messageData.put("file_url", fileUrl);
            } else {
                messageData.put("file_name", null);
                messageData.put("file_url", null);
            }

            ApiFuture<DocumentReference> future = db.collection("group_messages").add(messageData);
            DocumentReference docRef = future.get();
            msg.setMessageId(docRef.getId());
            updateGroupLastActivity(msg.getGroupId());

            return true;
        } catch (Exception e) {
            System.out.println("Error saving group message: " + e.getMessage());
            return false;
        }
    }

    public List<GroupMessage> loadGroupChatHistory(String groupId) {
        List<GroupMessage> messages = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = db.collection("group_messages")
                    .whereEqualTo("group_id", groupId)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (DocumentSnapshot document : documents) {
                GroupMessage msg = documentToGroupMessage(document);
                if (msg != null) {
                    messages.add(msg);
                }
            }

            messages.sort((msg1, msg2) -> {
                Date timestamp1 = msg1.getTimestamp();
                Date timestamp2 = msg2.getTimestamp();
                if (timestamp1 == null || timestamp2 == null) return 0;
                return timestamp1.compareTo(timestamp2);
            });

        } catch (Exception e) {
            System.out.println("Error loading group chat history: " + e.getMessage());
        }

        return messages;
    }

    public void saveGroupMessageAsync(GroupMessage msg, Runnable onComplete) {
        CompletableFuture.runAsync(() -> {
            boolean success = saveGroupMessage(msg);
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
        });
    }

    public void loadGroupChatHistoryAsync(String groupId, Consumer<List<GroupMessage>> callback) {
        CompletableFuture.supplyAsync(() -> {
            return loadGroupChatHistory(groupId);
        }).thenAccept(messages -> {
            Platform.runLater(() -> callback.accept(messages));
        });
    }

    public void loadUserGroupsAsync(String username, Consumer<List<GroupChat>> callback) {
        if (userGroupsCache.containsKey(username) &&
                (System.currentTimeMillis() - lastGroupsCacheUpdate) < CACHE_EXPIRY_TIME) {
            System.out.println("Using cached groups for async call: " + username);
            Platform.runLater(() -> callback.accept(new ArrayList<>(userGroupsCache.get(username))));
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            return loadUserGroups(username);
        }).thenAccept(groups -> {
            Platform.runLater(() -> callback.accept(groups));
        });
    }

// Helper Methods

    private GroupChat documentToGroupChat(DocumentSnapshot document) {
        try {
            GroupChat group = new GroupChat();
            group.setGroupId(document.getString("group_id"));
            group.setGroupName(document.getString("group_name"));
            group.setGroupDescription(document.getString("group_description"));
            group.setCreatorUsername(document.getString("creator_username"));
            group.setGroupImagePath(document.getString("group_image_path"));
            group.setCreatedAt(document.getDate("created_at"));
            group.setLastActivity(document.getDate("last_activity"));

            Boolean isActive = document.getBoolean("is_active");
            group.setActive(isActive != null ? isActive : true);

            // Convert members list
            List<String> membersList = (List<String>) document.get("members");
            if (membersList != null) {
                group.setMembers(new HashSet<>(membersList));
            }

            return group;
        } catch (Exception e) {
            System.out.println("Error converting document to GroupChat: " + e.getMessage());
            return null;
        }
    }

    private GroupMessage documentToGroupMessage(DocumentSnapshot document) {
        try {
            GroupMessage msg = new GroupMessage();
            msg.setMessageId(document.getId());
            msg.setGroupId(document.getString("group_id"));
            msg.setSenderUsername(document.getString("sender_username"));
            msg.setContent(document.getString("content"));
            msg.setTimestamp(document.getDate("timestamp"));

            Boolean isFile = document.getBoolean("is_file");
            msg.setFile(isFile != null ? isFile : false);

            String messageTypeStr = document.getString("message_type");
            if (messageTypeStr != null) {
                try {
                    msg.setMessageType(GroupMessage.MessageType.valueOf(messageTypeStr));
                } catch (IllegalArgumentException e) {
                    msg.setMessageType(GroupMessage.MessageType.TEXT);
                }
            }

            if (msg.isFile()) {
                msg.setFileName(document.getString("file_name"));
                String fileUrl = document.getString("file_url");
                if (fileUrl != null) {
                    byte[] fileData = downloadChatFile(fileUrl);
                    msg.setFileData(fileData);
                }
            }

            return msg;
        } catch (Exception e) {
            System.out.println("Error converting document to GroupMessage: " + e.getMessage());
            return null;
        }
    }

    private void updateGroupLastActivity(String groupId) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("last_activity", new Date());

            db.collection("groups").document(groupId).update(updates);
        } catch (Exception e) {
            System.out.println("Error updating group last activity: " + e.getMessage());
        }
    }

    private String uploadGroupChatFile(String groupId, String senderUsername, String fileName, byte[] fileData) {
        try {
            String filePath = "group_chat_files/" + groupId + "/" + senderUsername + "_" +
                    System.currentTimeMillis() + "_" + fileName;

            BlobId blobId = BlobId.of(bucketName, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            storage.create(blobInfo, fileData);
            return filePath;
        } catch (Exception e) {
            System.out.println("Group chat file upload failed: " + e.getMessage());
            return null;
        }
    }

    public Set<String> getGroupMembers(String groupId) {
        try {
            ApiFuture<DocumentSnapshot> future = db.collection("groups").document(groupId).get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                List<String> membersList = (List<String>) document.get("members");
                return membersList != null ? new HashSet<>(membersList) : new HashSet<>();
            }
        } catch (Exception e) {
            System.out.println("Error getting group members: " + e.getMessage());
        }
        return new HashSet<>();
    }

    public List<String> loadGroupMembers(String groupId) {
        Set<String> membersSet = getGroupMembers(groupId);
        return new ArrayList<>(membersSet);
    }
}