package com.example.owlpost_2_0.Database;

import com.example.owlpost_2_0.Client.Client;
import javafx.scene.image.Image;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:owlpost.db";
    private static DatabaseHandler instance;
    private Connection connection;

    private DatabaseHandler() {
        initializeDatabase();
    }

    public static DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (Exception e) {
            System.out.println("Database initialization failed: " + e.getMessage());
        }
    }

    private void createTables() {
        String createUserTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY MANAGEMENT,
                    username TEXT UNIQUE NOT NILL,
                    password TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    date_of_birth DATE NOT NULL,
                    house TEXT,
                    patronus TEXT,
                    profile_picture BLOB
                )
                """;
//        String createPasswordResetTable = """
//                CREATE TABLE IF NOT EXISTS password_reset_tokens (
//                                id INTEGER PRIMARY KEY AUTOINCREMENT,
//                                email TEXT NOT NULL,
//                                reset_token TEXT NOT NULL,
//                                is_used INTEGER DEFAULT 0,
//                                FOREIGN KEY (email) REFERENCES users (email) ON DELETE CASCADE
//                            )
//                """;
        try (Statement stmt = connection.createStatement()){
            stmt.execute(createUserTable);
//            stmt.execute(createPasswordResetTable);
        }catch (Exception e) {
            System.out.println("Table creation failed: " + e.getMessage());
        }
    }

    public boolean registerUser(Client client, File profilePicture) {
        String sql = """
                INSERT INFO users (username, password, email, date_of_birth, house, patronus, profile_picture)
                VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement pstmt = connection.prepareStatement()){
            pstmt.setString(1, client.getUsername());
            pstmt.setString(2, client.getPassword());
            pstmt.setString(3, client.getEmail());
            pstmt.setString(4, Date.valueOf(client.getDateofbirth()));
            pstmt.setString(5, client.getHouse()));
            pstmt.setString(6, client.getPatronus()));

            if (profilePicture != null && profilePicture.exists()) {
                byte[] imageBytes = fileToByteArray(profilePicture);
                pstmt.setByte(7, imageBytes);
            }else {
                pstmt.setNull(7, Types.BLOB);
            }
        }catch (Exception e) {
            System.out.println("User registration failed: " + e.getMessage());
            return false;
        }
    }

    public Client loginUser(String username, String password) {
        String sql = """
            SELECT username, password, email, date_of_birth, house, patronus, profile_picture 
            FROM users WHERE username = ? AND password = ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In production, hash and compare!

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Client client = new Client();
                client.setUsername(rs.getString("username"));
                client.setPassword(rs.getString("password"));
                client.setEmail(rs.getString("email"));
                client.setDateofbirth(rs.getDate("date_of_birth").toLocalDate());
                client.setHouse(rs.getString("house"));
                client.setPatronus(rs.getString("patronus"));

                return client;
            }
        } catch (SQLException e) {
            System.err.println("Login failed: " + e.getMessage());
        }
        return null;
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

    // Check if email exists
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

    public Image getUserProfilePicture(String username) {
        String sql = "SELECT profile_picture FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                byte[] imageBytes = rs.getBytes("profile_picture");
                if (imageBytes != null) {
                    return new Image(new ByteArrayInputStream(imageBytes));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get profile picture: " + e.getMessage());
        }
        return null;
    }

    public String generatePasswordResetToken(String email) {
        // First check if email exists
        if (!emailExists(email)) {
            return null;
        }

        // Generate a random 6-digit recovery key
        String resetToken = String.format("%06d", (int)(Math.random() * 1000000));

        // Store the token in database
        String sql = "INSERT INTO password_reset_tokens (email, reset_token) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, resetToken);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return resetToken;
            }
        } catch (SQLException e) {
            System.err.println("Failed to generate reset token: " + e.getMessage());
        }
        return null;
    }

    public boolean verifyResetToken(String email, String token) {
        String sql = "SELECT COUNT(*) FROM password_reset_tokens WHERE email = ? AND reset_token = ? AND is_used = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, token);

            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Failed to verify reset token: " + e.getMessage());
            return false;
        }
    }

    // Reset password with token
    public boolean resetPasswordWithToken(String email, String token, String newPassword) {
        // First verify the token
        if (!verifyResetToken(email, token)) {
            return false;
        }

        // Update password
        String updatePasswordSql = "UPDATE users SET password = ? WHERE email = ?";
        String markTokenUsedSql = "UPDATE password_reset_tokens SET is_used = 1 WHERE email = ? AND reset_token = ?";

        try (PreparedStatement pstmt1 = connection.prepareStatement(updatePasswordSql);
             PreparedStatement pstmt2 = connection.prepareStatement(markTokenUsedSql)) {

            // Update password
            pstmt1.setString(1, newPassword); // Hash this in production!
            pstmt1.setString(2, email);
            int passwordUpdated = pstmt1.executeUpdate();

            // Mark token as used
            pstmt2.setString(1, email);
            pstmt2.setString(2, token);
            pstmt2.executeUpdate();

            return passwordUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Password reset failed: " + e.getMessage());
            return false;
        }
    }

    // Clean up old/used tokens (optional - call periodically)
    public void cleanupOldTokens() {
        String sql = "DELETE FROM password_reset_tokens WHERE is_used = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to cleanup old tokens: " + e.getMessage());
        }
    }

    public List<Client> getAllUsers() {
        List<Client> users = new ArrayList<>();
        String sql = "SELECT username, email, house, patronus FROM users ORDER BY username";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Client client = new Client();
                client.setUsername(rs.getString("username"));
                client.setEmail(rs.getString("email"));
                client.setHouse(rs.getString("house"));
                client.setPatronus(rs.getString("patronus"));
                users.add(client);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get all users: " + e.getMessage());
        }
        return users;
    }

    // Update user's profile picture
    public boolean updateProfilePicture(String username, File newProfilePicture) {
        String sql = "UPDATE users SET profile_picture = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (newProfilePicture != null && newProfilePicture.exists()) {
                byte[] imageBytes = fileToByteArray(newProfilePicture);
                pstmt.setBytes(1, imageBytes);
            } else {
                pstmt.setNull(1, Types.BLOB);
            }
            pstmt.setString(2, username);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Profile picture update failed: " + e.getMessage());
            return false;
        }
    }

    private byte[] fileToByteArray(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            System.err.println("Failed to convert file to byte array: " + e.getMessage());
            return null;
        }
    }

    // Close database connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database connection: " + e.getMessage());
        }
    }

    // Test database connection
    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
