package com.example.owlpost_2_0.Controllers;

import com.example.owlpost_2_0.Client.Client;
import com.example.owlpost_2_0.Database.DatabaseHandler;
import com.example.owlpost_2_0.Email.EmailService;
import com.example.owlpost_2_0.Resources.Animations;
import com.example.owlpost_2_0.Resources.Audios;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.concurrent.Task;

public class LoginController implements Initializable {
    // buttons
    @FXML
    private Button goSortBtn;
    @FXML
    private Button chooseHouseBtn;
    @FXML
    private RadioButton gryffindor, slytherin, ravenclaw, hufflepuff;
    @FXML
    private Button loginBtn;
    @FXML
    private Button selectDP;
    @FXML
    private Button submitResetPass;
    @FXML
    private Button submitResetUser;
    @FXML
    private Button submitUser;
    @FXML
    private Button back_btn;
    @FXML
    private Button alrightbtn;
    @FXML
    private Button continuebtn;

    // panes
    @FXML
    private Pane filterpane;
    @FXML
    private Pane loginPane;
    @FXML
    private Pane signupPane;
    @FXML
    private Pane recoveryPane;
    @FXML
    private Pane sortingPane;
    @FXML
    private Pane alertpane;

    // labels
    @FXML
    private Label houseQuesResetPass;
    @FXML
    private Label houseSelection;
    @FXML
    private Label foundornotfound;
    @FXML
    private Label alertheading;
    @FXML
    private Label alertbody;

    // textfields
    @FXML
    private TextField forgotPassUser;
    @FXML
    private TextField loginUser;
    @FXML
    private TextField emailField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField secretCodeField;

    // passwords
    @FXML
    private PasswordField loginPass;
    @FXML
    private PasswordField confirmnewPass;
    @FXML
    private PasswordField newPass;
    @FXML
    private PasswordField signupPassField;

    // imageviews
    @FXML
    private ImageView patronus;
    @FXML
    private ImageView patronus_frame;
    @FXML
    private ImageView profilePic;
    @FXML
    private ImageView profilePicFrame;
    @FXML
    private ImageView HouseSelected;
    @FXML
    private ImageView hat;

    // hyperlinks
    @FXML
    private Hyperlink newUser;
    @FXML
    private Hyperlink oldUser;
    @FXML
    private Hyperlink forgotPass;

    @FXML
    private DatePicker DOBPicker;

    private String[] patronuses = {"cat", "doe", "fox", "hawk", "hippogriff", "stag", "wolf"};

    private Client client;
    private DatabaseHandler dbHandler;
    private File selectedDP;
    private String selectedHouse;
    private String selectedPatronus;
    //    private String pendingResetEmail;
    String[] houseMessages = {
            "\"Brave of heart and bold of spirit... GRYFFINDOR!\"",
            "\"Loyal, patient, and true... HUFFLEPUFF!\"",
            "\"Wit beyond measure is man's greatest treasure... RAVENCLAW!\"",
            "\"Ambitious and cunning, you'll achieve great things... SLYTHERIN!\""
    };
    String houses[] = {"Gryffindor", "Slytherine", "Hufflepuff", "Ravenclaw"};
    private int resetNum;


    public void ButtonAction(ActionEvent event) {
        Audios.playSound("spell");
        Button btn = (Button) event.getSource();
        if (btn.equals(loginBtn)) {
            HandleLogin(btn);
        }
        if (btn.equals(back_btn)) {
            showLoginPane();
        }
        if (btn.equals(submitUser)) {
            HandleRegister();
        }
        if (btn.equals(selectDP)) {
            HandleDPSelection();
        }
        if (btn.equals(submitResetUser)) {
            HandlePassResetRequest();
        }
        if (btn.equals(submitResetPass)) {
            HandlePassReset();
        }
        if (btn.equals(goSortBtn)) {
            if (client != null && client.isValidForSorting()) {
                ShowSortingPane();
            } else {
                showAlert("", "You are not enrolled yet", "Please register yourself first");
            }
        }
        if (btn.equals(chooseHouseBtn)) {
            sortingCeremony();
        }
        if (btn.equals(alrightbtn)) {
            Animations.FadeTransition(alertpane, false);
        }
        if (btn.equals(continuebtn)) {
            Animations.FadeTransition(sortingPane, false);
            Animations.FadeTransition(signupPane, true);
            Animations.TranslateLeft(loginPane);
        }
    }

    private void showAlert(String music, String title, String message) {
        if (!music.isEmpty()) {
            Audios.playSound(music);
        }
        alertheading.setWrapText(true);
        alertbody.setWrapText(true);
        alertheading.setText(title);
        alertbody.setText(message);
        Animations.FadeTransition(alertpane, true);
    }

    private void showLoginPane() {
        resetLoginForm();
        resetRecoveryForm();
        Animations.TranslateRight(recoveryPane);
    }

    public void showRecoveryPane() {
        resetLoginForm();
        resetRecoveryForm();
        Animations.TranslateRight(recoveryPane);
        recoveryPane.setVisible(true);
        Animations.TranslateLeft(recoveryPane);
    }

    public void showSignupPane() {
        resetLoginForm();
        resetSignUpForm();
        Animations.TranslateRight(loginPane);

    }

    private void ShowSortingPane() {
        Animations.FadeTransition(signupPane, false);
        Animations.FadeTransition(sortingPane, true);
    }

    public void resetLoginForm() {
        loginUser.clear();
        loginPass.clear();
    }

    public void resetSignUpForm() {
        nameField.clear();
        emailField.clear();
        signupPassField.clear();
        profilePic.setImage(null);
        if (DOBPicker != null) {
            DOBPicker.setValue(null);
        }
        selectedDP = null;
        hat.setVisible(false);
        resetHouseForm();
    }

    public void resetRecoveryForm() {
        forgotPassUser.clear();
        foundornotfound.setVisible(false);
        secretCodeField.clear();
        newPass.clear();
        confirmnewPass.clear();
        secretCodeField.setVisible(false);
        newPass.setVisible(false);
        confirmnewPass.setVisible(false);
        submitResetPass.setVisible(false);
    }

    public void resetHouseForm() {
        HouseSelected.setImage(null);
        HouseSelected.setVisible(false);
        patronus.setImage(null);
        patronus.setVisible(false);
        patronus_frame.setVisible(false);
        selectedHouse = "";
        selectedPatronus = "";
    }

    private void HandlePassReset() {
        int secretcode = Integer.parseInt(secretCodeField.getText());
        String newpassword = newPass.getText();
        String confirmpassword = confirmnewPass.getText();
        if (newpassword.isEmpty() || confirmpassword.isEmpty()) {
            showAlert("concentrate", "Concentrate Potter !", "Please fill in all fields");
            return;
        }
        if (resetNum != secretcode) {
            showAlert("knowitall", "I won't be lectured by an insufferable know-it-all !", "Secret Key doesn't match!");
            return;
        }
        if (newpassword.length() < 6) {
            showAlert("father", "You are weak! Just like your father...", "Password must be at least 6 characters long");
            return;
        }
        if (!newpassword.equals(confirmpassword)) {
            showAlert("knowitall", "I won't be lectured by an insufferable know-it-all !", "Please try again");
            return;
        }
        boolean success = dbHandler.updatePassword(client.getUsername(), newpassword);
        if (success) {
            showAlert("myfather", "Success", "Your password has been reset!");
            resetRecoveryForm();
            showLoginPane();
        }
        else {
            showAlert("", "Error", "Failed to update password in the database.");
        }
    }

    private void HandlePassResetRequest() {
        String email = forgotPassUser.getText();
        if (email.isEmpty() || !isValidEmail(email)) {
            foundornotfound.setVisible(true);
            return;
        }
        Client foundclient = dbHandler.findUserByEmail(email);
        if (foundclient == null) {
            foundornotfound.setText("Email not found!");
            foundornotfound.setVisible(true);
            return;
        }
        client = foundclient;
        // find the client with this email and copy it in current client object, for changing its password
        try {
            resetNum = EmailService.sendEmail(email);
            foundornotfound.setText("Reset code sent!");
            foundornotfound.setVisible(true);
            secretCodeField.setVisible(true);
            newPass.setVisible(true);
            confirmnewPass.setVisible(true);
            submitResetPass.setVisible(true);
        }catch (Exception e) {
            System.out.println("Try a different email " + e.getMessage());
            foundornotfound.setText("Failed to send email!");
            foundornotfound.setVisible(true);
            return;
        }
//        foundornotfound.setVisible(false);
    }

    private void HandleDPSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose your Wizard Portrait");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(selectDP.getScene().getWindow());
        if (selectedFile != null) {
            selectedDP = selectedFile;
            profilePic.setImage(new Image(selectedFile.toURI().toString()));
            profilePicFrame.setVisible(true);
            profilePic.setVisible(true);
        }
    }

    private void HandleRegister() {
        String username = nameField.getText();
        String password = signupPassField.getText();
        String email = emailField.getText();
        LocalDate dob = DOBPicker != null ? DOBPicker.getValue() : null;

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || dob == null) {
            showAlert("concentrate", "Concentrate Potter !", "Please fill in all fields");
            return;
        }
        if (!isValidEmail(email)) {
            showAlert("", "Invalid Post Address", "Please enter a valid post address");
            return;
        }
        if (dbHandler.usernameExists(username)) {
            showAlert("", "Did you take polyjuice ??🤔", "Username taken");
            return;
        }
        if (dbHandler.emailExists(email)) {
            showAlert("", "Email already registered", "This email is already in use");
            return;
        }
        int age = LocalDate.now().getYear() - dob.getYear();
        if (age < 18) {
            showAlert("", "You are too young to be a wizard", "Wait for your turn");
            return;
        }
        if (age > 25) {
            showAlert("", "You are too old to be a wizard", "Luckily we are in need of a teacher😊");
            return;
        }
        if (selectedDP == null || !selectedDP.exists()) {
            showAlert("", "You haven't chosen a wizard portrait", "Please choose one");
            return;
        }
        client = new Client(username, password, email, dob);
        if (selectedHouse == null || selectedHouse.isEmpty() || selectedPatronus == null || selectedPatronus.isEmpty()) {
            showAlert("", "Great!", "Now please complete the ceremony!");
            hat.setVisible(true);
            return;
        }
        boolean registered = dbHandler.registerUser(client, selectedDP);
        if (registered){
            showAlert("", "Welcome to Hogwarts", "You have been registered successfully. Now please hurry to the sorting ceremony !");
            resetSignUpForm();
            showLoginPane();
        }
        else {
            showAlert("myfather", "My father will hear about this!", "Something went wrong during registration.");
        }
    }

    private void sortingCeremony() {
        if (client == null) {
//            showAlert("", "Error", "No student registered for sorting!");
//            return;
            client = new Client();
        }

        int Hindex = new Random().nextInt(houses.length);
        selectedHouse = houses[Hindex];
        System.out.println("Selected house: " + selectedHouse);

        int index = new Random().nextInt(patronuses.length);
        selectedPatronus = patronuses[index];
        System.out.println("Selected patronus: " + selectedPatronus);

        patronus.setViewport(new Rectangle2D(0, 0, 108, 120));

        // Load house image with better debugging
        loadHouseImage();

        // Load patronus image with better debugging
        loadPatronusImage();

        client.setHouse(selectedHouse);
        client.setPatronus(selectedPatronus);

        chooseHouseBtn.setVisible(false);
        continuebtn.setVisible(true);
        if (selectedDP != null && selectedDP.exists()) {
            boolean registered = dbHandler.registerUser(client, selectedDP);
            if (registered) {
                System.out.println("User registered successfully after sorting!");
            } else {
                System.err.println("Failed to register user after sorting!");
            }
        }
        // Let user admire their results for 3 seconds, then show continue option
        javafx.animation.Timeline displayTimer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> {
                    showAlert("", "Welcome to " + selectedHouse.toUpperCase() + "!",
                            "Your patronus is a " + selectedPatronus + ". Click 'Alright' to continue to login.");
                })
        );
        displayTimer.play();
    }

    private void HandleLogin(Button btn) {
        String username = loginUser.getText();
        String password = loginPass.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("concentrate", "Concentrate Potter !", "Please fill in all fields");
            return;
        }
        loginBtn.setDisable(true);

        Task<Client> loginTask = new Task<Client>() {
            @Override
            protected Client call() throws Exception {
                return dbHandler.login(username, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            client = loginTask.getValue();
            loginBtn.setDisable(false);
            if (client != null) {
                showAlert("", "Welcome back " + client.getUsername() + "!", "You're enrolled at Hogwarts!");
                ToChatRoom(btn);
                Audios.stopAmbience();
            } else {
                showAlert("father", "No such wizard!", "Check your credentials again.");
            }
        });

        loginTask.setOnFailed(e -> {
            loginBtn.setDisable(false);
            showAlert("father", "No such wizard!", "Check your credentials again.");
        });

        new Thread(loginTask).start();
    }

    public void handleHyperLink(ActionEvent event) {
        Audios.playSound("spell");
        Hyperlink link = (Hyperlink) event.getSource();
        if (link.equals(forgotPass)) {
            showRecoveryPane();
        }
        if (link.equals(newUser)) {
            showSignupPane();
        }
        if (link.equals(oldUser)) {
            Animations.TranslateLeft(loginPane);
        }
    }

    private void loadHouseImage() {
        try {
            // Debug: Print current working directory and classpath
            System.out.println("Attempting to load house image for: " + selectedHouse);

            // Try multiple possible paths
            String[] possiblePaths = {
                    "/Images/LoginForm/" + selectedHouse + "-logo.png",
                    "/com/example/owlpost_2_0/Images/LoginForm/" + selectedHouse + "-logo.png",
                    "/" + selectedHouse + "-logo.png",
                    "/Images/LoginForm/" + selectedHouse.toLowerCase() + "-logo.png"
            };

            Image houseImage = null;
            String successfulPath = null;

            for (String path : possiblePaths) {
                try {
                    System.out.println("Trying path: " + path);
                    var resource = getClass().getResource(path);
                    if (resource != null) {
                        System.out.println("Resource found at: " + resource.toString());
                        houseImage = new Image(resource.toExternalForm());
                        if (houseImage != null && !houseImage.isError()) {
                            successfulPath = path;
                            break;
                        }
                    } else {
                        System.out.println("Resource not found at: " + path);
                    }
                } catch (Exception e) {
                    System.out.println("Exception trying path " + path + ": " + e.getMessage());
                }
            }

            if (houseImage != null && !houseImage.isError()) {
                System.out.println("Successfully loaded house image from: " + successfulPath);
                HouseSelected.setImage(houseImage);
                HouseSelected.setVisible(true);
//                if (Animations.class.getMethod("spinImage", javafx.scene.image.ImageView.class) != null) {
//                    Animations.spinImage(HouseSelected);
//                }
            } else {
                System.err.println("Could not load house image from any path for: " + selectedHouse);
                // Show a placeholder or text instead
                HouseSelected.setVisible(false);
            }
        } catch (Exception e) {
            System.err.println("Error in loadHouseImage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPatronusImage() {
        try {
            System.out.println("Attempting to load patronus image for: " + selectedPatronus);

            String[] possiblePatronusPaths = {
                    "/Images/Patronus/" + selectedPatronus + ".jpg",
                    "/com/example/owlpost_2_0/Images/Patronus/" + selectedPatronus + ".jpg",
                    "/Images/" + selectedPatronus + ".jpg",
                    "/Images/Patronus/" + selectedPatronus + ".png", // try PNG format too
            };

            Image patronusImage = null;
            String successfulPath = null;

            for (String path : possiblePatronusPaths) {
                try {
                    System.out.println("Trying patronus path: " + path);
                    var resource = getClass().getResource(path);
                    if (resource != null) {
                        System.out.println("Patronus resource found at: " + resource.toString());
                        patronusImage = new Image(resource.toExternalForm());
                        if (patronusImage != null && !patronusImage.isError()) {
                            successfulPath = path;
                            break;
                        }
                    } else {
                        System.out.println("Patronus resource not found at: " + path);
                    }
                } catch (Exception e) {
                    System.out.println("Exception trying patronus path " + path + ": " + e.getMessage());
                }
            }

            if (patronusImage != null && !patronusImage.isError()) {
                System.out.println("Successfully loaded patronus image from: " + successfulPath);
                patronus.setImage(patronusImage);
                patronus.setVisible(true);
                patronus_frame.setVisible(true);
            } else {
                System.err.println("Could not load patronus image from any path for: " + selectedPatronus);
                // Hide patronus elements if image can't be loaded
                patronus.setVisible(false);
                patronus_frame.setVisible(false);
            }
        } catch (Exception e) {
            System.err.println("Error in loadPatronusImage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void ToChatRoom(Button btn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/owlpost_2_0/Fxml/chatroom.fxml"));
            Parent root = loader.load();
            System.out.println("loaded");
            ChatRoomController controller = loader.getController();
            controller.getClient(client);
            Stage stage = (Stage) btn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("OwlPost");
            stage.setOnCloseRequest(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit Confirmation");
                alert.setHeaderText("Are you sure you want to exit?");
                alert.setContentText("Press OK to exit or Cancel to stay.");

                ButtonType okButton = alert.showAndWait().get();
                if (okButton == ButtonType.OK) {
                    System.exit(0);
                } else {
                    e.consume();
                }
            });
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error : " + e.getMessage());

        }
    }

    private static boolean isValidEmail(String email) {
        return (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbHandler = DatabaseHandler.getInstance();

        Animations.leftRight((signupPane));
        Animations.leftRight((loginPane));
        Animations.leftRight((sortingPane));
        Animations.leftRight((recoveryPane));
    }
}