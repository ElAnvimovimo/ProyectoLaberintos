package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
    Jugador jugador;
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
    private Set<Celda> celdasOcultas = new HashSet<>();
    private Random randomPesadilla = new Random();
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
        System.out.println("Scene configurada en LaberintoController");
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
        System.out.println("Configurando controles de teclado...");

        if (scene != null) {
            System.out.println("Configurando eventos en Scene");
            scene.setOnKeyPressed(e -> {
                System.out.println("Evento de teclado captado en Scene: " + e.getCode());
                procesarTecla(e);
            });
        } else {
            System.out.println("ERROR: Scene es null, no se pueden configurar eventos de teclado");
        }

        // Eventos de click para debug
        paneLaberinto.setOnMouseClicked(e -> {
            System.out.println("Click en pane - Scene focus");
            if (scene != null) {
                scene.getRoot().requestFocus();
            }
        });

        canvasMaze.setOnMouseClicked(e -> {
            System.out.println("Click en canvas - Scene focus");
            if (scene != null) {
                scene.getRoot().requestFocus();
            }
        });
    }

    private void iniciarModoJuego() {
        // Detener timers anteriores si existen
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
                tiempoAgotado();
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
                tiempoAgotado();
            }
        }));
        timerJuego.setCycleCount(Timeline.INDEFINITE);
        timerJuego.play();

        System.out.println("Modo Normal iniciado - Tiempo: " + modoActual.getTiempoSegundos() + " segundos");
    }

    private void actualizarLabelTiempo() {
        if (labelTiempo != null) {
            int minutos = tiempoRestante / 60;
            int segundos = tiempoRestante % 60;
            labelTiempo.setText(String.format("Tiempo: %02d:%02d", minutos, segundos));

            // Cambiar color si queda poco tiempo
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

    private void tiempoAgotado() {
        detenerTimers();
        juegoEnCurso = false;

        if (modoActual == ModoJuego.PESADILLA) {
            mostrarJumpscare();
        } else {
            // Lógica de alerta para otros modos
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Tiempo Agotado");
            alerta.setHeaderText("¡Se acabó el tiempo!");
            alerta.setContentText("No lograste completar el laberinto a tiempo.");
            alerta.showAndWait();
        }

        // Restaurar controles
        comboBoxModoJuego.setDisable(false);
        comboBoxSizeLaberinto.setDisable(false);
        buttonNuevoLaberinto.setDisable(false);
        buttonResolver.setDisable(false);
    }

    private void procesarTecla(javafx.scene.input.KeyEvent e) {
        if (!juegoEnCurso) {
            System.out.println("Juego no en curso, ignorando input. Estado: " + juegoEnCurso);
            return;
        }


        System.out.println("Procesando tecla: " + e.getCode() + " - Juego en curso: " + juegoEnCurso);

        switch (e.getCode()) {
            case I:
            case UP:
                System.out.println("Moviendo Norte");
                moverJugador(-1, 0); // Norte
                break;
            case K:
            case DOWN:
                System.out.println("Moviendo Sur");
                moverJugador(1, 0);  // Sur
                break;
            case J:
            case LEFT:
                System.out.println("Moviendo Oeste");
                moverJugador(0, -1); // Oeste
                break;
            case L:
            case RIGHT:
                System.out.println("Moviendo Este");
                moverJugador(0, 1);  // Este
                break;
            default:
                System.out.println("Tecla no reconocida: " + e.getCode());
                break;
        }
        e.consume(); // Consumir el evento
    }

    private void moverJugador(int deltaFila, int deltaColumna) {
        int nuevaFila = jugadorFila + deltaFila;
        int nuevaColumna = jugadorColumna + deltaColumna;

        // Validar límites del laberinto
        if (nuevaFila < 1 || nuevaFila > sizeLaberinto ||
                nuevaColumna < 1 || nuevaColumna > sizeLaberinto) {
            return;
        }

        // Validar muros
        Celda celdaActual = laberintoCamino[jugadorFila][jugadorColumna];
        if (!esMovimientoValido(celdaActual, deltaFila, deltaColumna)) {
            return;
        }

        // Actualizar posición
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

        // Actualizar posición del círculo del jugador
        jugadorActual.setCenterX((jugadorColumna - 0.5) * tamañoCelda);
        jugadorActual.setCenterY((jugadorFila - 0.5) * tamañoCelda);
    }

    private void laberintoCompletado() {
        juegoEnCurso = false;
        // *** Solución: Detener el timer del juego al completar el laberinto ***
        detenerTimers();

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("¡Felicidades!");
        alerta.setHeaderText("¡Has completado el laberinto!");
        alerta.setContentText("Llegaste a la meta en " + movimientos + " movimientos");
        alerta.showAndWait();

        // Habilitar botones nuevamente
        buttonNuevoLaberinto.setDisable(false);
        buttonResolver.setDisable(false);
    }

    private boolean esMovimientoValido(Celda celdaActual, int deltaFila, int deltaColumna) {
        if (deltaFila == -1 && celdaActual.isMuroNorte()) return false; // Norte
        if (deltaFila == 1 && celdaActual.isMuroSur()) return false;    // Sur
        if (deltaColumna == -1 && celdaActual.isMuroOeste()) return false; // Oeste
        if (deltaColumna == 1 && celdaActual.isMuroEste()) return false;   // Este
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
            System.out.println("Modo seleccionado: " + modoActual);
            if (modoActual == ModoJuego.LIBRE) {
                labelTiempo.setText("Tiempo: Ilimitado");
                labelTiempo.setTextFill(Color.BLACK);
            }
        }
    }


    @FXML
    void resolverLaberinto(ActionEvent event) {
        buttonResolver.setDisable(true);
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

        System.out.println(padre);

        while (!fila.isEmpty()) {
            padre = fila.remove();
            int row, col;
            row = padre.getFila();
            col = padre.getColumna();
            if ((padre.getFila() == metaRow) && padre.getColumna() == metaCol) {
                pasosSolucion = padre.getPasos();
                System.out.println("Pasos recorridos: " + pasosSolucion);
                dibujarCamino();
                break;
            }
            laberintoCamino[row][col].setVisitado(true);

            row = padre.getFila();
            col = padre.getColumna();
            System.out.println(padre);

            // Norte
            if (row > 1 && !padre.isMuroNorte() && !laberintoCamino[row - 1][col].isVisitado()) {
                Celda vecina = laberintoCamino[row - 1][col];
                vecina.setVisitado(true);
                vecina.setPasos(padre.getPasos() + 1);
                padres.put(vecina, padre);
                fila.add(vecina);
            }
            // Sur
            if (row < sizeLaberinto && !padre.isMuroSur() && !laberintoCamino[row + 1][col].isVisitado()) {
                Celda vecina = laberintoCamino[row + 1][col];
                vecina.setVisitado(true);
                vecina.setPasos(padre.getPasos() + 1);
                padres.put(vecina, padre);
                fila.add(vecina);
            }
            // Este
            if (col < sizeLaberinto && !padre.isMuroEste() && !laberintoCamino[row][col + 1].isVisitado()) {
                Celda vecina = laberintoCamino[row][col + 1];
                vecina.setVisitado(true);
                vecina.setPasos(padre.getPasos() + 1);
                padres.put(vecina, padre);
                fila.add(vecina);
            }
            // Oeste
            if (col > 1 && !padre.isMuroOeste() && !laberintoCamino[row][col - 1].isVisitado()) {
                Celda vecina = laberintoCamino[row][col - 1];
                vecina.setVisitado(true);
                vecina.setPasos(padre.getPasos() + 1);
                padres.put(vecina, padre);
                fila.add(vecina);
            }
        }
    }

    private void dibujarCamino() {
        List<Celda> camino = new ArrayList<>();
        Celda actual = laberintoCamino[metaRow][metaCol];

        while (actual != null) {
            camino.add(actual);
            actual = padres.get(actual);
        }

        Collections.reverse(camino);

        // Dibujar el camino
        GraphicsContext gc = canvasMaze.getGraphicsContext2D();
        gc.setStroke(Color.web("7cbf7b"));
        gc.setLineWidth((canvasMaze.getHeight() / sizeLaberinto) / 2.5);

        double tamañoCelda = canvasMaze.getWidth() / sizeLaberinto;

        for (int i = 0; i < camino.size() - 1; i++) {
            Celda actualCelda = camino.get(i);
            Celda siguienteCelda = camino.get(i + 1);

            double x1 = (actualCelda.getColumna() - 0.5) * tamañoCelda;
            double y1 = (actualCelda.getFila() - 0.5) * tamañoCelda;
            double x2 = (siguienteCelda.getColumna() - 0.5) * tamañoCelda;
            double y2 = (siguienteCelda.getFila() - 0.5) * tamañoCelda;

            gc.strokeLine(x1, y1, x2, y2);
        }

        System.out.println("Camino reconstruido con " + camino.size() + " celdas");
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

                // Fondo del laberinto
                gc.setFill(Color.WHITE);
                gc.fillRect(x + tileWidth * 0.01, y + tileHeight * 0.01, tileWidth * 0.98, tileHeight * 0.98);

                //Muro s
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
            imageView.setPreserveRatio(false);  // No mantener la relación de aspecto para que se estire
            imageView.setFitWidth(Screen.getPrimary().getVisualBounds().getWidth());  // Ancho de la pantalla
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
            alerta("Error al cargar el jumpscare: " + e.getMessage());
        }
    }

    public void reanudarPartida(LaberintoInfo laberintoInfo) {
    }
}