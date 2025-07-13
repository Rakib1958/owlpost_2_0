module com.example.owlpost_2_0 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires jdk.httpserver;
    requires jakarta.mail;
    requires java.desktop;
    requires opencv;
    requires com.google.auth.oauth2;
    requires firebase.admin;
    requires google.cloud.firestore;
    requires google.cloud.storage;
    requires com.google.api.apicommon;


    opens com.example.owlpost_2_0 to javafx.fxml;
    opens com.example.owlpost_2_0.Controllers to javafx.fxml;
    exports com.example.owlpost_2_0;
}