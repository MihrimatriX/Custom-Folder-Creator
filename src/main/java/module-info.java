module com.mover.foldercreator {
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.sun.jna;
    requires javafx.web;
    requires jdk.jsobject;
    requires org.jsoup;
    requires java.desktop;
    requires image4j;

    opens com.mover to javafx.fxml;
    exports com.mover;
}