package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class comoJugarController {

    @FXML
    Label labelCierre;

    @FXML
    void labelCierreClicked(MouseEvent event) {
        final Node node = (Node) event.getSource();
        final Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }

    @FXML
    void labelCierreEntered(MouseEvent event) {
        labelCierre.setStyle("-fx-text-fill:red");
    }

    @FXML
    void labelCierreExited(MouseEvent event) {
        labelCierre.setStyle("-fx-text-fill:black");
    }
}
