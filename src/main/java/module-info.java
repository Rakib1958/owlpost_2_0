module com.example.owlpost_2_0 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.owlpost_2_0 to javafx.fxml;
    exports com.example.owlpost_2_0;
}