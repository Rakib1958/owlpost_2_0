module com.example.owlpost_2_0 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires jdk.httpserver;
    requires jakarta.mail;


    opens com.example.owlpost_2_0 to javafx.fxml;
    opens com.example.owlpost_2_0.Controllers to javafx.fxml;
    exports com.example.owlpost_2_0;
}