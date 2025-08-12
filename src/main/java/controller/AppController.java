package controller;

import app.App;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class AppController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    public Button buttonJugar;

    @FXML
    private Button buttonCargarPartida;

    @FXML
    private Button buttonComoJugar;

    @FXML
    private Label labelCierre;

    @FXML
    void labelCierreClicked(MouseEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    void labelCierreEntered(MouseEvent event) {
        labelCierre.setStyle("-fx-text-fill:red");
    }

    @FXML
    void labelCierreExited(MouseEvent event) {
        labelCierre.setStyle("-fx-text-fill:white");
    }

    @FXML
    private TextField textFieldJugador;

    @FXML
    void iniciarJuego(ActionEvent event) {
        String nombreJugador = textFieldJugador.getText();
        final Node node = (Node) event.getSource();
        final Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
        cargarVentanaLaberinto(nombreJugador);
    }

    public void cargarVentanaLaberinto(String nombreJugador) {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/laberinto.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 640, 640);
            Stage stage = new Stage();
            stage.setScene(scene);

            stage.centerOnScreen();
            stage.setResizable(false);

            LaberintoController laberintoController = fxmlLoader.getController();
            laberintoController.setScene(scene);
            laberintoController.start(nombreJugador, 0.0, 0);

            stage.setOnCloseRequest(windowEvent -> {
                laberintoController.pausarJuego();
                if (laberintoController.isCambiosSinGuardar()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmar Cierre");
                    alert.setHeaderText("Hay cambios sin guardar");
                    alert.setContentText("¿Estás seguro de que quieres salir?");
                    Optional<ButtonType> accion = alert.showAndWait();
                    if (accion.isPresent() && accion.get() == ButtonType.CANCEL) {
                        windowEvent.consume();
                    }
                }
            });
            stage.show();
            scene.getRoot().requestFocus();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void cargarPartida(ActionEvent event) {
        final Node node = (Node) event.getSource();
        final Stage stages = (Stage) node.getScene().getWindow();
        stages.setAlwaysOnTop(false);

        FXMLLoader loader = new FXMLLoader(App.class.getResource("/partidasGuardadas.fxml"));
        try {
            Scene scene = new Scene(loader.load(), 600, 400);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void infoComoJugar(ActionEvent event) {
        final Node node = (Node) event.getSource();
        final Stage stages = (Stage) node.getScene().getWindow();
        stages.setAlwaysOnTop(false);

        FXMLLoader loader = new FXMLLoader(App.class.getResource("/comoJugar.fxml"));
        try {
            Scene scene = new Scene(loader.load(), 600, 500);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void initialize() {
        textFieldJugador.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1 == true && !textFieldJugador.getText().isBlank()) {
                textFieldJugador.setText("");
            }
        });
    }

}
