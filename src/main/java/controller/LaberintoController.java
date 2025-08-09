package controller;

import database.JSQLite;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import model.*;

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
    int sizeLaberinto, colLaberinto;
    int startRow, startCol;
    Laberinto laberinto;
    Celda padre;
    int metaRow, metaCol, pasosSolucion;
    Queue<Celda> fila = new LinkedList<>();
    private Scene scene;
    private int jugadorFila;
    private int jugadorColumna;
    private boolean juegoEnCurso = false;
    private Celda[][] laberintoCamino;
    private Map<Celda, Celda> padres = new HashMap<>();
    private boolean cambiosSinGuardar;
    private int movimientos = 0;
    private ModoJuego modoActual = ModoJuego.NORMAL;
    private Timeline timerJuego;
    private Timeline timerPesadilla;
    private int tiempoRestante;

    public void setPartidaCompletada(boolean partidaCompletada) {
        this.partidaCompletada = partidaCompletada;
    }

    @FXML
    private Label labelJugador;
    @FXML
    private Label labelTiempo;
    @FXML
    private Button buttonJugar;
    @FXML
    private Button buttonResolver;
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
    private ComboBox<Integer> comboBoxSizeLaberinto;
    @FXML
    private ComboBox<ModoJuego> comboBoxModoJuego;

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    @FXML
    void jugarLaberinto(ActionEvent event) {
        comboBoxModoJuego.setDisable(true);
        comboBoxSizeLaberinto.setDisable(true);
        if (laberinto == null) {
            alerta("Primero genera un laberinto");
            return;
        }
        laberintoCamino = laberinto.getLaberinto();
        jugadorFila = startRow;
        jugadorColumna = startCol;
        juegoEnCurso = true;

        configurarControlesTeclado();
        actualizarPosicionJugador();
        buttonResolver.setDisable(true);
        buttonNuevoLaberinto.setDisable(true);
        iniciarModoJuego();
    }

    private void configurarControlesTeclado() {
        if (scene != null) {
            scene.setOnKeyPressed(e -> {
                procesarTecla(e);
            });
        }
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

    private void iniciarModoPesadilla() {
        tiempoRestante = modoActual.getTiempoSegundos();
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
        labelTiempo.setText("Tiempo: Ilimitado");
        labelTiempo.setTextFill(Color.BLACK);
    }

    private void iniciarModoNormal() {
        tiempoRestante = modoActual.getTiempoSegundos();
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

    private void actualizarLabelTiempo() {
        if (labelTiempo != null) {
            int minutos = tiempoRestante / 60;
            int segundos = tiempoRestante % 60;
            labelTiempo.setText(String.format("Tiempo: %02d:%02d", minutos, segundos));
            if (tiempoRestante <= 10) {
                labelTiempo.setTextFill(Color.RED);
            } else if (tiempoRestante <= 30) {
                labelTiempo.setTextFill(Color.ORANGE);
            } else {
                labelTiempo.setTextFill(Color.BLACK);
            }
        }
    }

    private void detenerTimers() {
        if (timerJuego != null) {
            timerJuego.stop();
        }
        if (timerPesadilla != null) {
            timerPesadilla.stop();
        }
    }

    public void alertaNoCompletado() {
        laberintoNoCompletado();
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Tiempo Agotado");
        alerta.setHeaderText("¡Se acabó el tiempo!");
        alerta.setContentText("No lograste completar el laberinto a tiempo.");
        alerta.show();

    }
    private void tiempoAgotado() {
        detenerTimers();
        juegoEnCurso = false;

        if (modoActual == ModoJuego.PESADILLA) {
            mostrarJumpscare();
            alertaNoCompletado();
        } else {
            alertaNoCompletado();
        }
        comboBoxModoJuego.setDisable(false);
        comboBoxSizeLaberinto.setDisable(false);
        buttonNuevoLaberinto.setDisable(false);
        buttonResolver.setDisable(false);
    }

    private void laberintoNoCompletado() {
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
        actualizarPosicionJugador();
        if (jugadorFila == metaRow && jugadorColumna == metaCol) {

            laberintoCompletado();
        }
    }

    private void actualizarPosicionJugador() {
        double tamañoCelda = canvasMaze.getWidth() / sizeLaberinto;
        jugadorActual.setCenterX((jugadorColumna - 0.5) * tamañoCelda);
        jugadorActual.setCenterY((jugadorFila - 0.5) * tamañoCelda);
    }

    private void laberintoCompletado() {
        juegoEnCurso = false;
        detenerTimers();

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Felicidades!");
        alerta.setHeaderText("Acabaste el laberinto!");
        alerta.setContentText("Llegaste a la meta en " + movimientos + " movimientos\n" + calificacionMeta());
        alerta.showAndWait();

        buttonNuevoLaberinto.setDisable(false);
        buttonResolver.setDisable(false);
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
        return "Con un puntaje de " + puntajeMeta + "/10";
    }

    private boolean esMovimientoValido(Celda celdaActual, int deltaFila, int deltaColumna) {
        if (deltaFila == -1 && celdaActual.isMuroNorte()) return false;
        if (deltaFila == 1 && celdaActual.isMuroSur()) return false;
        if (deltaColumna == -1 && celdaActual.isMuroOeste()) return false;
        if (deltaColumna == 1 && celdaActual.isMuroEste()) return false;
        return true;
    }


    @FXML
    void comboBoxSizeLaberintoClicked(ActionEvent event) {
        Object evento = event.getSource();

        if (evento.equals(comboBoxSizeLaberinto)) {
            sizeLaberinto = comboBoxSizeLaberinto.getSelectionModel().getSelectedItem();
            colLaberinto = sizeLaberinto;
        }
    }

    @FXML
    void comboBoxModoJuegoClicked(ActionEvent event) {
        if (comboBoxModoJuego.getValue() != null) {
            modoActual = comboBoxModoJuego.getValue();
            //System.out.println("Modo seleccionado: " + modoActual);
            if (modoActual == ModoJuego.LIBRE) {
                labelTiempo.setText("Tiempo: Ilimitado");
                labelTiempo.setTextFill(Color.BLACK);
            }
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
            //System.out.println(padre);

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
    void nuevoLaberinto(ActionEvent event) {
        if (sizeLaberinto == 0) {
            alerta("No has seleccionado un tamaño para el laberinto!");
            return;

        }
        buttonResolver.setDisable(false);
        Random random = new Random();
        laberinto = new Laberinto(sizeLaberinto, colLaberinto);
        laberinto.crearLaberinto();
        laberinto.recorrerLaberinto();

        GraphicsContext gc = canvasMaze.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasMaze.getWidth(), canvasMaze.getHeight());
        Celda[][] matLaberinto = laberinto.getLaberinto();
        Celda cellAux = laberinto.getCeldaInicial();
        double tamañoCelda = canvasMaze.getWidth() / sizeLaberinto;

        //Mostrar punto verde de la meta del laberinto
        startRow = 1;
        startCol = random.nextInt(1, sizeLaberinto);
        if (jugadorActual != null) {
            paneLaberinto.getChildren().remove(jugadorActual);
        }
        Circle player = new Circle(tamañoCelda / 2.5);
        player.setFill(Color.web("2b743e"));
        player.setCenterX((startCol - 0.5) * tamañoCelda);
        player.setCenterY((startRow - 0.5) * tamañoCelda);
        paneLaberinto.getChildren().add(player);
        jugadorActual = player;

        //Mostrar punto negro de la meta del laberinto
        metaRow = sizeLaberinto;
        metaCol = random.nextInt(1, sizeLaberinto);
        if (metaLaberinto != null) {
            paneLaberinto.getChildren().remove(metaLaberinto);
        }
        Circle circleMeta = new Circle(tamañoCelda / 2.5);
        circleMeta.setFill(Color.BLACK);
        circleMeta.setCenterX((metaCol - 0.5) * tamañoCelda);
        circleMeta.setCenterY((metaRow - 0.5) * tamañoCelda);
        paneLaberinto.getChildren().add(circleMeta);
        metaLaberinto = circleMeta;

        double tileWidth = canvasMaze.getWidth() / sizeLaberinto;
        double tileHeight = canvasMaze.getHeight() / sizeLaberinto;

        for (int fila = 1; fila <= sizeLaberinto; fila++) {
            for (int col = 1; col <= sizeLaberinto; col++) {
                Celda cel = matLaberinto[fila][col];

                double x = (col - 1) * tileWidth;
                double y = (fila - 1) * tileHeight;
                gc.setFill(Color.WHITE);
                gc.fillRect(x + tileWidth * 0.01, y + tileHeight * 0.01, tileWidth * 0.98, tileHeight * 0.98);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);

                if (cel.isMuroNorte()) {
                    gc.strokeLine(x, y, x + tileWidth, y);
                }
                if (cel.isMuroSur()) {
                    gc.strokeLine(x, y + tileHeight, x + tileWidth, y + tileHeight);
                }
                if (cel.isMuroEste()) {
                    gc.strokeLine(x + tileWidth, y, x + tileWidth, y + tileHeight);
                }
                if (cel.isMuroOeste()) {
                    gc.strokeLine(x, y, x, y + tileHeight);
                }
            }
        }
        buttonJugar.setDisable(false);
        juegoEnCurso = false;
        jugadorFila = startRow;
        jugadorColumna = startCol;
        actualizarPosicionJugador();

    }

    private void alerta(String msg) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Alerta");
        alerta.setHeaderText(null);
        alerta.setContentText(msg);
        alerta.showAndWait();
    }

    @FXML
    void initialize() {
        buttonResolver.setDisable(true);
        buttonJugar.setDisable(true);
        sizeLaberinto = 0;
        colLaberinto = 0;
        ArrayList<Integer> listaSize = new ArrayList<>();
        Collections.addAll(listaSize, 10, 20, 30);
        comboBoxSizeLaberinto.getItems().addAll(listaSize);
        comboBoxModoJuego.getItems().setAll(ModoJuego.values());

    }

    public boolean isCambiosSinGuardar() {
        return cambiosSinGuardar;
    }

    public void start(String nombreJugador) {
        jugador = new Jugador(nombreJugador, 0);
        labelJugador.setText(nombreJugador);
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
            imageView.setFitWidth(Screen.getPrimary().getVisualBounds().getWidth());
            imageView.setFitHeight(Screen.getPrimary().getVisualBounds().getHeight());

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
            jumpscareStage.setFullScreen(true);
            jumpscareStage.show();
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), ev -> jumpscareStage.close()));
            timeline.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reanudarPartida(LaberintoInfo laberintoInfo) {
    }

    public void guardarPartida(ActionEvent actionEvent) {
        if (laberinto == null || jugador == null) {
            alerta("No hay un laberinto o jugador para guardar");
            return;
        }
        int idLaberinto = jsqLite.guardarLaberinto(laberinto);
        boolean completada = (jugadorFila == metaRow && jugadorColumna == metaCol);

        Partida partida = new Partida(
                jugador,
                modoActual,
                sizeLaberinto,
                movimientos,
                tiempoRestante,
                completada
        );


        jsqLite.insertarPartida(partida);
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Partida guardada");
        alerta.setHeaderText(null);
        alerta.setContentText("La partida se ha guardado correctamente");
        alerta.showAndWait();

        cambiosSinGuardar = false;

    }
}