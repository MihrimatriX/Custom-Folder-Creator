module com.creator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.jsoup;
    requires image4j;
    requires org.apache.poi.ooxml;
    requires com.sun.jna;
    requires com.google.gson;

    opens com.creator to javafx.fxml;
    exports com.creator;
}