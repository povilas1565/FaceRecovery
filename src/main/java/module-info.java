module com.agora.facerecovery {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.ooxml;


    opens com.agora.facerecovery to javafx.fxml;
    exports com.agora.facerecovery;
    exports com.agora.facerecovery.model;
    opens com.agora.facerecovery.model to javafx.fxml;
    exports com.agora.facerecovery.controller;
    opens com.agora.facerecovery.controller to javafx.fxml;
    exports com.agora.facerecovery.utils;
    opens com.agora.facerecovery.utils to javafx.fxml;
}