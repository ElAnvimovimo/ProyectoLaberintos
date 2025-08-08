package controller;

import app.App;
import database.JSQLite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.LaberintoInfo;

import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PartidasGuardadasController {

    private final JSQLite jsqLite = new JSQLite();
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private TableColumn<LaberintoInfo, String> colEstado;
    @FXML
    private TableColumn<LaberintoInfo, String> colFecha;
    @FXML
    private TableColumn<LaberintoInfo, Integer> colID;
    @FXML
    private TableColumn<LaberintoInfo, String> colJugador;
    @FXML
    private TableColumn<LaberintoInfo, Double> colRacha;
    @FXML
    private TableView<LaberintoInfo> tableViewLaberintos;

    @FXML
    void initialize() {
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colID.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colID.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colID.setCellValueFactory(new PropertyValueFactory<>("racha"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        cargarDatos();
    }

    private void cargarDatos() {
        List<LaberintoInfo> laberintoInfos = jsqLite.listarLaberintosInfo();
        ObservableList<LaberintoInfo> lista = FXCollections.observableArrayList(laberintoInfos);
        tableViewLaberintos.setItems(lista);
    }

    void tablaClicked(MouseEvent event) {
        LaberintoInfo laberintoInfo = tableViewLaberintos.getSelectionModel().getSelectedItem();
        final Node node = (Node) event.getSource();
        final Stage stages = (Stage) node.getScene().getWindow();
        stages.close();

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("tablero.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 454, 504);
            Stage stage = new Stage();
            stage.setScene(scene);

            stage.centerOnScreen();
            stage.setResizable(false);
            //stage.setAlwaysOnTop(true);
            LaberintoController laberintoController = fxmlLoader.getController();
            laberintoController.reanudarPartida(laberintoInfo);

            final Node nodes = (Node) event.getSource();
            final Stage stagez = (Stage) nodes.getScene().getWindow();
            stagez.close();

            stage.setOnCloseRequest(windowEvent -> {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void tablaClicked(javafx.scene.input.MouseEvent event) {
        LaberintoInfo laberintoInfo = tableViewLaberintos.getSelectionModel().getSelectedItem();
        final Node node = (Node) event.getSource();
        final Stage stages = (Stage) node.getScene().getWindow();
        stages.close();

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("tablero.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 454, 504);
            Stage stage = new Stage();
            stage.setScene(scene);

            stage.centerOnScreen();
            stage.setResizable(false);
            //stage.setAlwaysOnTop(true);
            LaberintoController laberintoController = fxmlLoader.getController();
            laberintoController.reanudarPartida(laberintoInfo);

            final Node nodes = (Node) event.getSource();
            final Stage stagez = (Stage) nodes.getScene().getWindow();
            stagez.close();

            stage.setOnCloseRequest(windowEvent -> {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

