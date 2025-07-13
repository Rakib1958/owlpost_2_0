module com.example.owlpost_2_0 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires jdk.httpserver;
    requires jakarta.mail;
    requires opencv;
    requires com.google.auth.oauth2;
    requires firebase.admin;
    requires google.cloud.firestore;
    requires google.cloud.storage;
    requires com.google.api.apicommon;
    requires org.slf4j;
    requires com.google.auth;
    requires google.cloud.core;
    requires javafx.swing;


    opens com.example.owlpost_2_0 to javafx.fxml;
    opens com.example.owlpost_2_0.Controllers to javafx.fxml;
    exports com.example.owlpost_2_0;
}