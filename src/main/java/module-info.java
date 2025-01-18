module com.creator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.jsoup;
    requires com.sun.jna;
    requires com.google.gson;
    requires org.apache.poi.ooxml;
    requires image4j;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.media;

    opens com.creator to javafx.fxml, javafx.graphics, javafx.base, javafx.web, javafx.media;
    exports com.creator;
}