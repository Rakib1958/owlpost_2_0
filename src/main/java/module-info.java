module com.example.owlpost_2_0 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;
    requires java.sql;


    opens com.example.owlpost_2_0 to javafx.fxml;
    opens com.example.owlpost_2_0.Controllers to javafx.fxml;
    exports com.example.owlpost_2_0;
}