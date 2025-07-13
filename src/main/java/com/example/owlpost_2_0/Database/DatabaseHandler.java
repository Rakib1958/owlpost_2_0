package com.example.owlpost_2_0.Database;

import com.example.owlpost_2_0.ChatRoom.ChatMessage;
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
import javafx.scene.image.Image;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class DatabaseHandler {
    private static DatabaseHandler instance;
    private Firestore db;
    private Storage storage;
    private String bucketName;

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
            return true;
        } catch (Exception e) {
            System.out.println("Profile picture update failed: " + e.getMessage());
            return false;
        }
    }

    public Image getProfilePicture(String username) {
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
                        return new Image(new ByteArrayInputStream(content));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch profile picture: " + e.getMessage());
        }
        return null;
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

    public boolean saveChatMessage(ChatMessage msg) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("sender", msg.getSender());
            messageData.put("receiver", msg.getReceiver());
            messageData.put("is_file", msg.isFile());
            messageData.put("timestamp", new Date());

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
            ApiFuture<QuerySnapshot> future = db.collection("chat_history").get();
            List<QueryDocumentSnapshot> allDocuments = future.get().getDocuments();

            List<DocumentSnapshot> relevantDocuments = new ArrayList<>();
            for (DocumentSnapshot document : allDocuments) {
                String sender = document.getString("sender");
                String receiver = document.getString("receiver");

                if ((sender.equals(user1) && receiver.equals(user2)) ||
                        (sender.equals(user2) && receiver.equals(user1))) {
                    relevantDocuments.add(document);
                }
            }

            relevantDocuments.sort((doc1, doc2) -> {
                Date timestamp1 = doc1.getDate("timestamp");
                Date timestamp2 = doc2.getDate("timestamp");
                if (timestamp1 == null || timestamp2 == null) return 0;
                return timestamp1.compareTo(timestamp2);
            });

            for (DocumentSnapshot document : relevantDocuments) {
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
            e.printStackTrace();
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
}