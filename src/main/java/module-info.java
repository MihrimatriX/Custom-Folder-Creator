module com.creator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.jsoup;
    requires image4j;
    requires com.sun.jna;

    opens com.creator to javafx.fxml;
    exports com.creator;
}