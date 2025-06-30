package com.example.owlpost_2_0.Controllers;

import com.example.owlpost_2_0.Client.Client;
import com.example.owlpost_2_0.Resources.Animations;
import com.example.owlpost_2_0.Resources.Audios;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

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
    private Label Doesntmatch;
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
//    @FXML
//    private ImageView HuflLogo;
//    @FXML
//    private ImageView RavenLogo;
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
    private ImageView slytLogo;
    @FXML
    private ImageView HuflLogo;
    @FXML
    private ImageView RavenLogo;
    @FXML
    private ImageView gryfLogo;

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

    private void sortingCeremony() {
    }

    private void showAlert(String s, String youAreNotEnrolledYet, String pleaseRegisterYourselfFirst) {
    }

    private void ShowSortingPane() {
    }

    private void HandlePassReset() {
    }

    private void HandlePassResetRequest() {
    }

    private void HandleDPSelection() {
    }

    private void HandleRegister() {
    }

    private void showLoginPane() {
    }

    public void handleHyperLink(ActionEvent event) {

    }

    public void isHouseButton(ActionEvent event) {

    }

    private void HandleLogin(Button btn) {
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
