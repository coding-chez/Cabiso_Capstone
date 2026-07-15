module com.example.cabiso_capstone {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.cabiso_capstone to javafx.fxml;
    exports com.example.cabiso_capstone;

    opens com.example.cabiso_capstone.controllers to javafx.fxml;
    opens com.example.cabiso_capstone.model to javafx.fxml;
    opens com.example.cabiso_capstone.service to javafx.fxml;
    //opens com.example.cabiso_capstone.data to javafx.fxml;
    //opens com.example.cabiso_capstone.exception to javafx.fxml;
    //opens com.example.cabiso_capstone.util to javafx.fxml;
}