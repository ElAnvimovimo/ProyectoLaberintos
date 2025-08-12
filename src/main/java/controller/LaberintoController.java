package controller;

import database.JSQLite;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import model.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class LaberintoController {
    boolean partidaCompletada;
    JSQLite jsqLite = new JSQLite();
    private int puntajeMeta;

    Jugador jugador;

    public boolean isPartidaCompletada() {
        return partidaCompletada;
    }

    private final Map<Celda, Celda> padres = new HashMap<>();
    int sizeLaberinto;
    int startRow, startCol;
    Laberinto laberinto;
    Celda padre;
    int metaRow, metaCol, pasosSolucion;
    Queue<Celda> fila = new LinkedList<>();
    @FXML
    GraphicsContext graphicsContextCanvasMaze;
    private Scene scene;
    private int jugadorFila;
    private int jugadorColumna;
    private boolean juegoEnCurso = false;
    private Celda[][] laberintoCamino;
    private boolean controlesConfigurados = false;
    private boolean cambiosSinGuardar;
    private int movimientos = 0;
    private ModoJuego modoActual = ModoJuego.NORMAL;
    private Timeline timerJuego;
    private Timeline timerPesadilla;
    private int tiempoRestante;
    @FXML
    private Label labelRacha;
    private double racha;
    private boolean partidaPausada;
    private boolean partidaReanudada;
    private double tamanioCelda;

    public void setPartidaCompletada(boolean partidaCompletada) {
        this.partidaCompletada = partidaCompletada;
    }

    @FXML
    private Label labelJugador;
    private int juegosTerminados;
    @FXML
    private Label labelPausa;
    @FXML
    private Circle metaLaberinto;
    @FXML
    private Circle jugadorActual;
    @FXML
    private Circle jugadorGenActual;
    @FXML
    private Pane paneLaberinto;
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private Canvas canvasMaze;
    @FXML
    private Button buttonNuevoLaberinto;
    @FXML
    private Label labelTiempo;
    @FXML
    private ComboBox<ModoJuego> comboBoxModoJuego;
    @FXML
    private ComboBox<Dificultad> comboBoxSizeLaberinto;

    public void setScene(Scene scene) {
        this.scene = scene;
    }
    private int partidaIdActual = -1;

    void jugarLaberinto() {
        movimientos = 0;
        comboBoxModoJuego.setDisable(true);
        comboBoxSizeLaberinto.setDisable(true);

        laberintoCamino = laberinto.getLaberinto();
        jugadorFila = startRow;
        jugadorColumna = startCol;
        juegoEnCurso = true;

        configurarControlesTeclado();
        actualizarPosicionJugador();
        buttonNuevoLaberinto.setDisable(true);
        iniciarModoJuego();

    }

    private void iniciarModoJuego() {
        detenerTimers();

        switch (modoActual) {
            case NORMAL:
                iniciarModoNormal();
                break;
            case LIBRE:
                iniciarModoLibre();
                break;
            case PESADILLA:
                iniciarModoPesadilla();
        }
    }

    private void configurarControlesTeclado() {
        if (scene != null && !controlesConfigurados) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.SPACE) {
                    e.consume();
                    togglePausa();
                    return;
                }
                if (!partidaPausada) {
                    procesarTecla(e);
                }
            });
            controlesConfigurados = true;
        }
    }

    private void iniciarModoPesadilla() {
        if (!partidaReanudada) {
            tiempoRestante = calcularTiempo(modoActual, sizeLaberinto);
        }
        actualizarLabelTiempo();

        timerJuego = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tiempoRestante--;
            actualizarLabelTiempo();

            if (tiempoRestante <= 0) {
                Platform.runLater(() -> tiempoAgotado());
            }
        }));
        timerJuego.setCycleCount(Timeline.INDEFINITE);
        timerJuego.play();

        System.out.println(":(");
    }

    private void iniciarModoLibre() {
        labelTiempo.setText("-:-");
        labelTiempo.setTextFill(Color.WHITE);
    }

    private void iniciarModoNormal() {
        if (!partidaReanudada) {
            tiempoRestante = calcularTiempo(modoActual, sizeLaberinto);
        }
        actualizarLabelTiempo();

        timerJuego = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tiempoRestante--;
            actualizarLabelTiempo();

            if (tiempoRestante <= 0) {
                timerJuego.stop();
                tiempoAgotado();
            }
        }));
        timerJuego.setCycleCount(Timeline.INDEFINITE);
        timerJuego.play();
    }

    private void detenerTimers() {
        if (timerJuego != null) {
            timerJuego.stop();
        }
        if (timerPesadilla != null) {
            timerPesadilla.stop();
        }
    }

    private void actualizarLabelTiempo() {
        if (labelTiempo != null) {
            int minutos = tiempoRestante / 60;
            int segundos = tiempoRestante % 60;
            labelTiempo.setText(String.format("%02d:%02d", minutos, segundos));
            if (modoActual == ModoJuego.PESADILLA) {
                labelTiempo.setTextFill(Color.BLACK);
            }
        }
    }

    private void tiempoAgotado() {
        detenerTimers();
        juegoEnCurso = false;

        if (modoActual == ModoJuego.PESADILLA) {
            mostrarJumpscare();
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> {
                laberintoNoCompletado();
                comboBoxModoJuego.setDisable(false);
                comboBoxSizeLaberinto.setDisable(false);
                buttonNuevoLaberinto.setDisable(false);
            });
            pause.play();

        } else {
            laberintoNoCompletado();
            comboBoxModoJuego.setDisable(false);
            comboBoxSizeLaberinto.setDisable(false);
            buttonNuevoLaberinto.setDisable(false);
        }
    }

    private void procesarTecla(javafx.scene.input.KeyEvent e) {
        if (!juegoEnCurso) {
            return;
        }
        switch (e.getCode()) {
            case I:
            case UP:
                moverJugador(-1, 0);
                break;
            case K:
            case DOWN:
                moverJugador(1, 0);
                break;
            case J:
            case LEFT:
                moverJugador(0, -1);
                break;
            case L:
            case RIGHT:
                moverJugador(0, 1);
                break;
            default:
                break;
        }
        e.consume();
    }

    private void laberintoNoCompletado() {
        puntajeMeta = 0;
        racha = (racha * juegosTerminados) + puntajeMeta;
        juegosTerminados++;
        racha = racha / juegosTerminados;

        dialogFinDePartida("Tiempo Agotado\nNo lograste completar el laberinto a tiempo.");
    }

    private void moverJugador(int deltaFila, int deltaColumna) {
        int nuevaFila = jugadorFila + deltaFila;
        int nuevaColumna = jugadorColumna + deltaColumna;
        if (nuevaFila < 1 || nuevaFila > sizeLaberinto ||
                nuevaColumna < 1 || nuevaColumna > sizeLaberinto) {
            return;
        }

        Celda celdaActual = laberintoCamino[jugadorFila][jugadorColumna];
        if (!esMovimientoValido(celdaActual, deltaFila, deltaColumna)) {
            return;
        }

        jugadorFila = nuevaFila;
        jugadorColumna = nuevaColumna;
        movimientos++;
        cambiosSinGuardar = true;
        actualizarPosicionJugador();
        if (jugadorFila == metaRow && jugadorColumna == metaCol) {

            laberintoCompletado();
        }
    }

    private void actualizarPosicionJugador() {
        jugadorActual.setCenterX((jugadorColumna - 0.5) * tamanioCelda);
        jugadorActual.setCenterY((jugadorFila - 0.5) * tamanioCelda);
    }

    private void laberintoCompletado() {
        detenerTimers();
        dialogFinDePartida("Felicidades, resolviste el laberinto en " + movimientos + " movimientos.\n" + calificacionMeta());
    }

    private String calificacionMeta() {
        resolverLaberinto();
        if (movimientos >= pasosSolucion && movimientos < pasosSolucion * 1.2) {
            puntajeMeta = 10;
        }
        if (movimientos >= pasosSolucion * 1.2 && movimientos < pasosSolucion * 1.4) {
            puntajeMeta = 8;
        }
        if (movimientos >= pasosSolucion * 1.4 && movimientos < pasosSolucion * 1.6) {
            puntajeMeta = 6;
        }
        if (movimientos >= pasosSolucion * 1.6 && movimientos < pasosSolucion * 1.8) {
            puntajeMeta = 4;
        }
        if (movimientos >= pasosSolucion * 1.8 && movimientos < pasosSolucion * 2) {
            puntajeMeta = 2;
        }
        if (movimientos > pasosSolucion * 2) {
            puntajeMeta = 0;
        }
        racha = (racha * juegosTerminados) + puntajeMeta;
        juegosTerminados++;
        racha = racha / juegosTerminados;
        jugador.setRachaPartidas(puntajeMeta);
        return "Con un puntaje de " + puntajeMeta + "/10";


    }

    private boolean esMovimientoValido(Celda celdaActual, int deltaFila, int deltaColumna) {
        if (deltaFila == -1 && celdaActual.isMuroNorte()) return false;
        if (deltaFila == 1 && celdaActual.isMuroSur()) return false;
        if (deltaColumna == -1 && celdaActual.isMuroOeste()) return false;
        return deltaColumna != 1 || !celdaActual.isMuroEste();
    }

    @FXML
    void comboBoxSizeLaberintoClicked(ActionEvent event) {
        if (comboBoxSizeLaberinto.getValue() != null) {
            sizeLaberinto = comboBoxSizeLaberinto.getSelectionModel().getSelectedItem().getValor();
        }
    }

    @FXML
    void resolverLaberinto(ActionEvent event) {
    }

    public void resolverLaberinto() {
        padres.clear();
        laberintoCamino = laberinto.getLaberinto();

        for (int row = 1; row <= sizeLaberinto; row++) {
            for (int col = 1; col <= sizeLaberinto; col++) {
                laberintoCamino[row][col].setVisitado(false);
            }
        }
        fila.clear();
        padre = laberintoCamino[startRow][startCol];
        padre.setPasos(0);
        fila.add(padre);

        while (!fila.isEmpty()) {
            padre = fila.remove();
            int row, col;
            row = padre.getFila();
            col = padre.getColumna();
            if ((padre.getFila() == metaRow) && padre.getColumna() == metaCol) {
                pasosSolucion = padre.getPasos();
                break;
            }
            laberintoCamino[row][col].setVisitado(true);

            row = padre.getFila();
            col = padre.getColumna();

            if (row > 1 && !padre.isMuroNorte() && !laberintoCamino[row - 1][col].isVisitado()) {
                Celda vecina = laberintoCamino[row - 1][col];
                vecina.setVisitado(true);
                vecina.setPasos(padre.getPasos() + 1);
                padres.put(vecina, padre);
                fila.add(vecina);
            }
            if (row < sizeLaberinto && !padre.isMuroSur() && !laberintoCamino[row + 1][col].isVisitado()) {
                Celda vecina = laberintoCamino[row + 1][col];
                vecina.setVisitado(true);
                vecina.setPasos(padre.getPasos() + 1);
                padres.put(vecina, padre);
                fila.add(vecina);
            }
            if (col < sizeLaberinto && !padre.isMuroEste() && !laberintoCamino[row][col + 1].isVisitado()) {
                Celda vecina = laberintoCamino[row][col + 1];
                vecina.setVisitado(true);
                vecina.setPasos(padre.getPasos() + 1);
                padres.put(vecina, padre);
                fila.add(vecina);
            }
            if (col > 1 && !padre.isMuroOeste() && !laberintoCamino[row][col - 1].isVisitado()) {
                Celda vecina = laberintoCamino[row][col - 1];
                vecina.setVisitado(true);
                vecina.setPasos(padre.getPasos() + 1);
                padres.put(vecina, padre);
                fila.add(vecina);
            }
        }
    }

    @FXML
    void comboBoxModoJuegoClicked(ActionEvent event) {
        if (comboBoxModoJuego.getValue() != null) {
            modoActual = comboBoxModoJuego.getValue();
            if (modoActual == ModoJuego.LIBRE) {
                labelTiempo.setText("-:-");
                labelTiempo.setTextFill(Color.WHITE);
            }
        }
    }

    private void alerta(String msg) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Alerta");
        alerta.setHeaderText(null);
        alerta.setContentText(msg);
        alerta.showAndWait();
    }

    @FXML
    void nuevoLaberinto(ActionEvent event) {
        if (!validarTamanioLaberinto()) {
            return;
        }

        calcularTamanioCelda();
        crearYGenerarLaberinto();

        dibujarLaberinto();
        colocarPuntoJugador();
        colocarPuntoMeta();
        jugadorFila = startRow;
        jugadorColumna = startCol;
        actualizarPosicionJugador();
        jugarLaberinto();

    }

    public boolean isCambiosSinGuardar() {
        return cambiosSinGuardar;
    }

    @FXML
    void initialize() {
        labelPausa.setVisible(false);
        partidaReanudada = false;
        partidaPausada = false;
        sizeLaberinto = 0;
        comboBoxModoJuego.getItems().setAll(ModoJuego.values());
        comboBoxSizeLaberinto.getItems().setAll((Dificultad.values()));
    }

    public void start(String nombreJugador, double racha, int juegosTerminados) {
        jugador = new Jugador(nombreJugador, racha, juegosTerminados);
        this.racha = racha;
        labelJugador.setText(nombreJugador);
        this.juegosTerminados = juegosTerminados;
        labelRacha.setText(String.valueOf(racha));

    }

    private void mostrarJumpscare() {
        try {
            URL imageUrl = getClass().getResource("/images/jumpscare.png");
            if (imageUrl == null) {
                System.err.println("Imagen no encontrada: /images/jumpscare.png");
                return;
            }
            Image jumpscareImage = new Image(imageUrl.toString());
            ImageView imageView = new ImageView(jumpscareImage);
            imageView.setPreserveRatio(false);
            imageView.setFitWidth(1920);
            imageView.setFitHeight(1080);

            URL soundUrl = getClass().getResource("/sounds/jumpscare.mp3");
            if (soundUrl == null) {
                System.err.println("Sonido no encontrado: /sounds/jumpscare.mp3");
            } else {
                Media sound = new Media(soundUrl.toString());
                MediaPlayer mediaPlayer = new MediaPlayer(sound);
                mediaPlayer.setStartTime(Duration.seconds(1));
                mediaPlayer.setStopTime(Duration.seconds(3));
                mediaPlayer.play();
            }
            Stage jumpscareStage = new Stage();
            jumpscareStage.initStyle(StageStyle.UNDECORATED);
            jumpscareStage.setScene(new Scene(new StackPane(imageView)));
            jumpscareStage.alwaysOnTopProperty();
            jumpscareStage.show();
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), ev -> jumpscareStage.close()));
            timeline.playFrom(Duration.millis(200));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void guardarPartida(ActionEvent actionEvent) {
        pausarJuego();
        String laberintoJson = jsqLite.convertirLaberintoAJson(this.laberinto);

        LaberintoInfo partidaParaGuardar = new LaberintoInfo(
                this.partidaIdActual,
                jugador.getNombre(),
                null,
                racha,
                "EN_CURSO",
                laberintoJson,
                sizeLaberinto,
                movimientos,
                jugadorFila,
                jugadorColumna,
                metaRow,
                metaCol,
                modoActual,
                tiempoRestante,
                juegosTerminados
        );
        int idResultado = jsqLite.guardarOActualizarPartida(partidaParaGuardar);

        if (idResultado != -1) {
            this.partidaIdActual = idResultado;
            alerta("Partida guardada en ID: " + idResultado);
            cambiosSinGuardar = false;
        } else {
            alerta("Error al guardar la partida.");
        }
    }

    private void dialogFinDePartida(String msg) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(canvasMaze.getScene().getWindow());
        dialog.setTitle("Fin de Partida");
        dialog.setHeaderText(msg);

        ButtonType botonSeguirJuego = new ButtonType("Continuar el juego", ButtonBar.ButtonData.OK_DONE);
        ButtonType botonVolverMenu = new ButtonType("Regresar al menu inicial");
        ButtonType botonSalirJuego = new ButtonType("Salir", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(botonSeguirJuego, botonVolverMenu, botonSalirJuego);
        Platform.runLater(() -> {
            Optional<ButtonType> respuesta = dialog.showAndWait();
            respuesta.ifPresent(respuestaA -> {
                if (respuestaA == botonSeguirJuego) {
                    reiniciarUI();
                    start(jugador.getNombre(), racha, juegosTerminados);
                } else if (respuestaA == botonVolverMenu) {
                    volverAlMenu();
                } else if (respuestaA == botonSalirJuego) {
                    if (cambiosSinGuardar) {
                        pausarJuego();

                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.initOwner(canvasMaze.getScene().getWindow());
                        alert.setTitle("Confirmar Cierre");
                        alert.setHeaderText("Hay cambios sin guardar");
                        alert.setContentText("¿Estás seguro de que quieres salir?");
                        Optional<ButtonType> accion = alert.showAndWait();
                        if (accion.isPresent() && accion.get() == ButtonType.OK) {
                            Platform.exit();
                        }
                    } else {
                        Platform.exit();
                    }
                }
            });
        });

    }

    private void reiniciarUI() {
        labelTiempo.setText("-:-");

        comboBoxModoJuego.setDisable(false);
        comboBoxSizeLaberinto.setDisable(false);
        buttonNuevoLaberinto.setDisable(false);


        comboBoxModoJuego.getItems().setAll(ModoJuego.values());
        comboBoxSizeLaberinto.getItems().setAll(Dificultad.values());

        comboBoxModoJuego.setValue(null);
        comboBoxSizeLaberinto.setValue(null);
        comboBoxSizeLaberinto.setPromptText("DIFICULTAD");
        comboBoxModoJuego.setPromptText("MODO");

        sizeLaberinto = 0;
        modoActual = null;
        jugadorFila = 0;
        jugadorColumna = 0;
        movimientos = 0;
        juegoEnCurso = false;
        partidaPausada = false;
        partidaReanudada = false;

        detenerTimers();
        paneLaberinto.getChildren().remove(jugadorActual);
        paneLaberinto.getChildren().remove(metaLaberinto);

        limpiarCanvas();
    }

    public void reanudarPartida(LaberintoInfo info) {
        String laberintoJson = info.getLaberintoJson();
        if (laberintoJson != null) {
            this.laberinto = new JSQLite().cargarLaberintoDesdeJSON(laberintoJson);
        } else {
            this.laberinto = null;
        }
        if (this.laberinto != null && this.laberinto.getLaberinto() != null && info.getModoJuego() != null) {
            this.laberintoCamino = laberinto.getLaberinto();
            this.partidaIdActual = info.getId();
            this.sizeLaberinto = info.getSizeLaberinto();
            this.movimientos = info.getMovimientos();
            this.jugadorFila = info.getRowJugador();
            this.jugadorColumna = info.getColJugador();
            this.metaRow = info.getRowMeta();
            this.metaCol = info.getColMeta();
            this.modoActual = info.getModoJuego();
            this.tiempoRestante = info.getTiempoRestante();
            this.racha = info.getRacha();
            this.juegosTerminados = info.getJuegosTerminados();

            calcularTamanioCelda();
            start(info.getNombreJugador(), info.getRacha(), info.getJuegosTerminados());
            this.jugador.setRachaPartidas(racha);
            redibujarLaberintoDesdeObjeto();
            actualizarLabelTiempo();

            juegoEnCurso = true;
            controlesConfigurados = false;
            configurarControlesTeclado();

            Platform.runLater(() -> {
                paneLaberinto.requestFocus();
            });
            pausarJuego();
        } else {
            partidaIdActual = info.getId();
            reiniciarUI();
            start(info.getNombreJugador(), info.getRacha(), info.getJuegosTerminados());
        }
    }

    private void redibujarLaberintoDesdeObjeto() {
        dibujarLaberinto();
        colocarPuntoJugadorReanudar();
        colocarPuntoMetaReanudar();
    }

    private void volverAlMenu() {
        try {
            Stage stage = (Stage) canvasMaze.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            stage.setScene(scene);

            stage.centerOnScreen();

            stage.show();

        } catch (IOException e) {
            System.err.println("Error al mostrar la ventana de Menu");
            e.printStackTrace();
        }
    }

    private boolean validarTamanioLaberinto() {
        if (sizeLaberinto == 0) {
            alerta("No has seleccionado un tamanio para el laberinto!");
            return false;
        }
        return true;
    }

    private void crearYGenerarLaberinto() {
        laberinto = new Laberinto(sizeLaberinto);
        laberinto.crearLaberinto();
        laberinto.recorrerLaberinto();
    }

    private void limpiarCanvas() {
        graphicsContextCanvasMaze = canvasMaze.getGraphicsContext2D();
        graphicsContextCanvasMaze.clearRect(0, 0, canvasMaze.getWidth(), canvasMaze.getHeight());

    }

    private void dibujarLaberinto() {
        if (laberinto == null || laberinto.getLaberinto() == null) {
            System.err.println("Error. Laberinto es null");
            return;
        }
        if (graphicsContextCanvasMaze == null) {
            graphicsContextCanvasMaze = canvasMaze.getGraphicsContext2D();
        }


        Color colorPrimario;
        Color colorSecundario;

        colorPrimario = Color.WHITE;
        colorSecundario = Color.BLACK;

        graphicsContextCanvasMaze = canvasMaze.getGraphicsContext2D();
        graphicsContextCanvasMaze.clearRect(0, 0, canvasMaze.getWidth(), canvasMaze.getHeight());

        double tileWidth = canvasMaze.getWidth() / sizeLaberinto;
        double tileHeight = canvasMaze.getHeight() / sizeLaberinto;


        Celda[][] matLaberinto = laberinto.getLaberinto();

        for (int fila = 1; fila <= sizeLaberinto; fila++) {
            for (int col = 1; col <= sizeLaberinto; col++) {

                Celda cel = matLaberinto[fila][col];

                double x = (col - 1) * tileWidth;
                double y = (fila - 1) * tileHeight;
                graphicsContextCanvasMaze.setFill(colorPrimario);
                graphicsContextCanvasMaze.fillRect(x + tileWidth * 0.01, y + tileHeight * 0.01, tileWidth * 0.98, tileHeight * 0.98);
                graphicsContextCanvasMaze.setStroke(colorSecundario);
                graphicsContextCanvasMaze.setLineWidth(1);

                if (cel.isMuroNorte()) {
                    graphicsContextCanvasMaze.strokeLine(x, y, x + tileWidth, y);
                }
                if (cel.isMuroSur()) {
                    graphicsContextCanvasMaze.strokeLine(x, y + tileHeight, x + tileWidth, y + tileHeight);
                }
                if (cel.isMuroEste()) {
                    graphicsContextCanvasMaze.strokeLine(x + tileWidth, y, x + tileWidth, y + tileHeight);
                }
                if (cel.isMuroOeste()) {
                    graphicsContextCanvasMaze.strokeLine(x, y, x, y + tileHeight);
                }
            }
        }
    }

    void pausarJuego() {
        if (!juegoEnCurso || partidaPausada) return;
        detenerTimers();
        partidaPausada = true;
        labelPausa.setVisible(true);
    }

    private void reanudarJuegoPausa() {
        partidaReanudada = true;
        if (!juegoEnCurso || !partidaPausada) return;
        iniciarModoJuego();
        partidaPausada = false;
        labelPausa.setVisible(false);
    }

    private void togglePausa() {
        if (partidaPausada) {
            reanudarJuegoPausa();
        } else {
            pausarJuego();
        }
    }

    void colocarPuntoJugador() {
        Random random = new Random();
        startRow = 1;
        startCol = random.nextInt(1, sizeLaberinto);
        if (jugadorActual != null) {
            paneLaberinto.getChildren().remove(jugadorActual);
        }

        Circle player = new Circle(tamanioCelda / 2.5);
        player.setFill(Color.web("2b743e"));
        player.setCenterX((startCol - 0.5) * tamanioCelda);
        player.setCenterY((startRow - 0.5) * tamanioCelda);
        paneLaberinto.getChildren().add(player);
        jugadorActual = player;
    }

    void colocarPuntoJugadorReanudar() {
        Circle player = new Circle(tamanioCelda / 2.5);
        player.setFill(Color.web("2b743e"));
        player.setCenterX((jugadorColumna - 0.5) * tamanioCelda);
        player.setCenterY((jugadorFila - 0.5) * tamanioCelda);
        paneLaberinto.getChildren().add(player);
        jugadorActual = player;
    }

    void colocarPuntoMetaReanudar() {
        Circle circleMeta = new Circle(tamanioCelda / 2.5);
        circleMeta.setFill(Color.BLACK);
        circleMeta.setCenterX((metaCol - 0.5) * tamanioCelda);
        circleMeta.setCenterY((metaRow - 0.5) * tamanioCelda);
        paneLaberinto.getChildren().add(circleMeta);
        metaLaberinto = circleMeta;
    }

    void colocarPuntoMeta() {
        Random random = new Random();
        metaRow = sizeLaberinto;
        metaCol = random.nextInt(1, sizeLaberinto);
        if (metaLaberinto != null) {
            paneLaberinto.getChildren().remove(metaLaberinto);
        }
        Circle circleMeta = new Circle(tamanioCelda / 2.5);
        circleMeta.setFill(Color.BLACK);
        circleMeta.setCenterX((metaCol - 0.5) * tamanioCelda);
        circleMeta.setCenterY((metaRow - 0.5) * tamanioCelda);
        paneLaberinto.getChildren().add(circleMeta);
        metaLaberinto = circleMeta;
    }

    void calcularTamanioCelda() {
        if (canvasMaze == null || sizeLaberinto <= 0) {
            System.err.println("Error: Canvas null o size inválido");
            tamanioCelda = 0;
            return;
        }

        double width = canvasMaze.getWidth();
        double height = canvasMaze.getHeight();

        tamanioCelda = Math.min(width, height) / sizeLaberinto;
    }

    private int calcularTiempo(ModoJuego modo, int sizeLaberinto) {
        if (modo == ModoJuego.LIBRE) return 0;

        if (modo == ModoJuego.NORMAL) {
            if (sizeLaberinto == 10) return 15;
            if (sizeLaberinto == 20) return 30;
            if (sizeLaberinto == 30) return 60;
        }

        if (modo == ModoJuego.PESADILLA) {
            if (sizeLaberinto == 10) return 7;
            if (sizeLaberinto == 20) return 10;
            if (sizeLaberinto == 30) return 20;
        }
        return 0;
    }
}