module com.example.cabiso_capstone {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.cabiso_capstone to javafx.fxml;
    exports com.example.cabiso_capstone;

    opens com.example.cabiso_capstone.controllers to javafx.fxml;
    opens com.example.cabiso_capstone.model to javafx.fxml, javafx.base;
    opens com.example.cabiso_capstone.database to javafx.fxml;
}