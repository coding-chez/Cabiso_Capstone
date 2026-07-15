module com.example.cabiso_capstone {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.cabiso_capstone to javafx.fxml;
    exports com.example.cabiso_capstone;
}