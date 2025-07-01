package com.example.owlpost_2_0.Database;

import com.example.owlpost_2_0.Client.Client;
import javafx.scene.image.Image;

import java.io.*;
import java.sql.*;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:owlpost.db";
    private static DatabaseHandler instance;
    private Connection connection;

    private DatabaseHandler() {
        connect();
        createUserTable();
    }

    public static DatabaseHandler getInstance() {
        if (instance == null) instance = new DatabaseHandler();
        return instance;
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
        } catch (Exception e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    private void createUserTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    date_of_birth TEXT,
                    house TEXT,
                    patronus TEXT,
                    profile_picture BLOB
                );
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Table creation failed: " + e.getMessage());
        }
    }

    public boolean registerUser(Client client, File profilePic) {
        if (client == null || client.getUsername() == null || client.getPassword() == null ||
                client.getEmail() == null || client.getDateofbirth() == null ||
                client.getHouse() == null || client.getPatronus() == null) {
            return false;
        }
        String sql = """
                INSERT INTO users (username, password, email, date_of_birth, house, patronus, profile_picture)
                VALUES (?, ?, ?, ?, ?, ?, ?);
                """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, client.getUsername());
            pstmt.setString(2, client.getPassword());
            pstmt.setString(3, client.getEmail());
            pstmt.setString(4, client.getDateofbirth().toString());
            pstmt.setString(5, client.getHouse());
            pstmt.setString(6, client.getPatronus());
            pstmt.setBytes(7, fileToByteArray(profilePic));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public Client login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Client client = new Client();
                client.setUsername(rs.getString("username"));
                client.setPassword(rs.getString("password"));
                client.setEmail(rs.getString("email"));
                client.setDateofbirth(Date.valueOf(rs.getString("date_of_birth")).toLocalDate());
                client.setHouse(rs.getString("house"));
                client.setPatronus(rs.getString("patronus"));
                return client;
            }
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
        return null;
    }

    public boolean updatePassword(String username, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Password update failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateProfilePicture(String username, File newPic) {
        String sql = "UPDATE users SET profile_picture = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBytes(1, fileToByteArray(newPic));
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Profile picture update failed: " + e.getMessage());
            return false;
        }
    }

    public Image getProfilePicture(String username) {
        String sql = "SELECT profile_picture FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                byte[] imgBytes = rs.getBytes("profile_picture");
                if (imgBytes != null) {
                    return new Image(new ByteArrayInputStream(imgBytes));
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch profile picture: " + e.getMessage());
        }
        return null;
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
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Username check failed: " + e.getMessage());
            return false;
        }
    }

    public Client findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Client client = new Client();
                client.setUsername(rs.getString("username"));
                client.setPassword(rs.getString("password"));
                client.setEmail(rs.getString("email"));
                client.setDateofbirth(Date.valueOf(rs.getString("date_of_birth")).toLocalDate());
                client.setHouse(rs.getString("house"));
                client.setPatronus(rs.getString("patronus"));
                return client;
            }
        } catch (SQLException e) {
            System.out.println("Find user by email failed: " + e.getMessage());
        }
        return null;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Email check failed: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.out.println("Connection close failed: " + e.getMessage());
        }
    }
}
