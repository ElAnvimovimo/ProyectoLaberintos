module mx.edu.utch.proyectolaberintos {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires javafx.media;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires com.fasterxml.jackson.databind;
    requires javafx.graphics;
    requires javafx.base;
    requires com.google.gson;

    exports app;
    opens app to javafx.fxml;
    exports controller;
    opens controller to javafx.fxml;
    exports database;
    opens database to javafx.base;
    opens model to javafx.base, com.google.gson;
    exports model;
}